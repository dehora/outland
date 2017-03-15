package outland.cluster.node;

import com.google.common.base.Joiner;
import io.grpc.StatusRuntimeException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.cluster.proto.DisconnectMessage;
import outland.cluster.proto.ForwardJoinMessage;
import outland.cluster.proto.Imok;
import outland.cluster.proto.JoinMessage;
import outland.cluster.proto.NeighborMessage;
import outland.cluster.proto.NeighbourReplyMessage;
import outland.cluster.proto.Node;
import outland.cluster.proto.Response;
import outland.cluster.proto.Rouk;
import outland.cluster.proto.ShuffleMessage;
import outland.cluster.proto.ShuffleReplyMessage;

class ClusterCoordinator implements ServiceHandler {

  private static final Logger logger = LoggerFactory.getLogger("outland-cluster");
  private static final Logger viewLogger = LoggerFactory.getLogger("outland-view");

  private static final int ACTIVE_RANDOM_WALK_LENGTH = 6; // paper: 6
  private static final int PASSIVE_RANDOM_WALK_LENGTH = 3; // paper: 3
  private static final int SHUFFLE_ACTIVE = 3; // paper: 3
  private static final int SHUFFLE_PASSIVE = 4; // paper: 4
  private static final int SHUFFLE_RANDOM_WALK_LENGTH = 6; // paper: ?
  private static final int DEADLINE_RUOK = 2;

  private final String id;
  private final ClusterMessageSender messageSender;
  private final List<String> seeds;
  private final Node localNode;
  private final ClusterOverlay overlay;
  private final ActiveViewChecker activeViewChecker;
  private final AtomicLong activeRepairTick = new AtomicLong(0L);

  ClusterCoordinator(String id,
      ClusterMessageSender sender,
      Node node,
      ClusterOverlay overlay,
      List<String> seeds
  ) {
    this.id = id;
    this.messageSender = sender;
    this.seeds = seeds;
    this.localNode = node;
    this.overlay = overlay;
    activeViewChecker = new ActiveViewChecker(id, this);
  }

  @Override public Response handleJoinRequest(JoinMessage request) {
    final Node node = request.getSender();
    logger.debug("id={} handle_join_request mid={} sender={}", id, request.getId(), node.getId());

    if (localNode.equals(node)) {
      logger.warn("id={} handle_join_request refusing self join mid={} sender={}", id,
          request.getId(), node.getId());
      return Response.newBuilder().setRequestId(request.getId()).setSender(localNode).build();
    }

    addActiveNode(node);
    /*
    not in protocol. give the joiner some nodes. this helps where a lot of nodes join at
    once and get cycled out of the active view before they can be shuffled with. if a
    joined goes into passive quickly it might get stuck with just the seed for a while.
     */
    sendShuffleRequestTo(node);
    getActiveViewWithout(node).forEach(n -> sendForwardJoin(n, node, ACTIVE_RANDOM_WALK_LENGTH));
    return Response.newBuilder().setRequestId(request.getId()).setSender(localNode).build();
  }

  @Override public Response handleForwardJoinRequest(ForwardJoinMessage request) {
    final Node sender = request.getSender();
    final Node joiner = request.getJoiner();
    logger.debug("id={} handle_forward_join_request mid={} sender={} joiner={}", id,
        request.getId(), sender.getId(), joiner.getId());

    if (localNode.equals(sender)) {
      logger.warn("id={} handle_forward_join_request self same as sender mid={} sender={}", id,
          request.getId(), sender.getId());
    }

    if (localNode.equals(joiner)) {
      logger.debug("id={} handle_forward_join_request self same as joiner mid={} sender={}", id,
          request.getId(), sender.getId());
    }

    if (request.getTtl() == 0 || activeViewIsEmpty()) {
      if (localNode.equals(joiner)) {
        // protocol doesn't cover this scenario, pass it along without bumping the arwl
        logger.debug(
            "id={} handle_forward_join_request self same as joiner mid={} sender={} ttl={} making dummy hop",
            id, request.getId(), sender.getId(), request.getTtl());
        getNonMatchingRandomNodeFromActiveView(sender)
            .ifPresent(n -> sendForwardJoin(n, joiner, (request.getTtl())));
      } else {
        addActiveNode(joiner);
      }
    } else {
      if (request.getTtl() == PASSIVE_RANDOM_WALK_LENGTH
          // protocol doesn't cover this scenario; don't add self to passive
          && !localNode.equals(joiner)) {
        addNodePassiveView(joiner);
      }
      getNonMatchingRandomNodeFromActiveView(sender)
          .ifPresent(n -> sendForwardJoin(n, joiner, (request.getTtl() - 1)));
    }

    return Response.newBuilder().setRequestId(request.getId()).setSender(localNode).build();
  }

  @Override public Response handleDisconnectRequest(DisconnectMessage request) {
    final Node sender = request.getSender();
    final String requestId = request.getId();
    logger.debug("id={} handle_disconnect_request mid={} sender={}", this.id, requestId,
        sender.getId());

    if (localNode.equals(sender)) {
      logger.warn("id={} handle_disconnect_request refusing self mid={} sender={}", id,
          request.getId(), sender.getId());
      return Response.newBuilder().setRequestId(request.getId()).setSender(localNode).build();
    }

    moveFromActiveToPassiveView(sender);
    return Response.newBuilder().setRequestId(requestId).setSender(localNode).build();
  }

  @Override public NeighbourReplyMessage handleNeighborRequest(NeighborMessage request) {
    final Node sender = request.getSender();
    logger.debug("id={} handle_neighbor_request mid={} sender={} prio={}", this.id, request.getId(),
        sender.getId(), request.getPriority());

    if (localNode.equals(sender)) {
      logger.warn("id={} handle_neighbor_request refusing self mid={} sender={}", id,
          request.getId(), sender.getId());
      return NeighbourReplyMessage.newBuilder()
          .setSender(localNode)
          .setId(messageId())
          .setCorrelationId(request.getId())
          .setResponse(NeighbourReplyMessage.Response.reject)
          .build();
    }

    NeighbourReplyMessage.Response response = NeighbourReplyMessage.Response.accept;

    if (request.getPriority().equals(NeighborMessage.Priority.high)) {
      addActiveNode(sender);
    } else {
      if (activeViewIsFull()) {
        response = NeighbourReplyMessage.Response.reject;
      } else {
        addActiveNode(sender);
      }
    }

    return NeighbourReplyMessage.newBuilder()
        .setSender(localNode)
        .setId(messageId())
        .setCorrelationId(request.getId())
        .setResponse(response)
        .build();
  }

  @Override public Response handleShuffleRequest(ShuffleMessage request) {
    final String requestId = request.getId();
    final Node sender = request.getSender();
    final Node origin = request.getOrigin();
    final int nextTtl = request.getTtl() - 1;

    if (localNode.equals(sender)) {
      logger.warn("id={} handle_shuffle_request refusing self mid={} sender={}", id,
          request.getId(), sender.getId());
    }

    if (logger.isDebugEnabled()) {
      logger.debug("id={} handle_shuffle_request mid={} sender={} origin={} ttl={} list={}",
          id, request.getId(), sender.getId(), origin.getId(), nextTtl,
          Joiner.on(",")
              .join(request.getExchangeListList()
                  .stream()
                  .map(Node::getId)
                  .collect(Collectors.toList())));
    }

    if (nextTtl > 0 && getActiveViewSize() > 1
        /*
        protocol does not cover this scenario, but shuffling with yourself seems limited.
        instead don't bump the ttl and pass it along to another node
         */
        || localNode.equals(origin)) {
      int ttl = nextTtl;
      if (localNode.equals(origin)) {
        logger.debug("id={} handle_shuffle_request self is same as origin={} making dummy hop", id,
            origin.getId());
        ttl = request.getTtl();
      }

      final Optional<Node> node = getNonMatchingRandomNodeFromActiveView(sender);
      if (node.isPresent()) {
        final Node contactNode = node.get();

        // nb: don't reset the message id
        final ShuffleMessage nextMessage = request.toBuilder()
            .setTtl(ttl)
            .setSender(localNode)
            .build();
        messageSender.shuffleRequest(contactNode, nextMessage);
      } else {
        logger.warn(
            "id={} handle_shuffle_request active_view_empty, calling neighbor from overlay {}", id,
            overlay.toString());
        sendNeighborRequest();
      }
    } else {
      final List<Node> randomNodesFromPassiveView =
          getRandomNodesFromPassiveView(request.getExchangeListCount());

      final ShuffleReplyMessage shuffleReplyMessage = ShuffleReplyMessage.newBuilder()
          .setId(messageId())
          .setSender(localNode)
          .setOriginalMessageId(requestId)
          .addAllExchangeList(randomNodesFromPassiveView)
          .build();

      messageSender.shuffleReply(origin, shuffleReplyMessage);
      updatePassiveViewFromShuffle(request.getExchangeListList());
    }
    return Response.newBuilder().setRequestId(requestId).setSender(localNode).build();
  }

  @Override public Response handleShuffleReply(ShuffleReplyMessage request) {
    final String requestId = request.getId();
    final Node sender = request.getSender();

    if (localNode.equals(sender)) {
      logger.warn("id={} handle_shuffle_reply refusing self mid={} sender={}", id, request.getId(),
          sender.getId());
      return Response.newBuilder().setRequestId(request.getId()).setSender(localNode).build();
    }

    logger.debug("id={} handle_shuffle_reply mid={} sender={}. Lush!", id, request.getId(),
        sender.getId());
    updatePassiveViewFromShuffle(request.getExchangeListList());
    return Response.newBuilder().setRequestId(requestId).setSender(localNode).build();
  }

  @Override public Imok handleRuok(Rouk request) {
    final Node sender = request.getSender();

    if (localNode.equals(sender)) {
      logger.warn("id={} handle_ruok refusing self mid={} sender={}", id, request.getId(),
          sender.getId());
    }

    logger.debug("id={} ruok_received mid={} sender={}", id, request.getId(), sender.getId());
    return Imok.newBuilder()
        .setId(messageId())
        .setCid(request.getId())
        .setSender(localNode)
        .build();
  }

  @Override public void logViews() {
    overlay.logViews();
  }

  // bootstrap seeds
  void initialize() {
    seeds.forEach(this::tryJoinSeed);
  }

  Optional<Imok> sendRouk(Node contactNode) {
    try {
      final Rouk rouk = Rouk.newBuilder().setId(messageId()).setSender(localNode).build();
      final long start = System.currentTimeMillis();
      final Imok imok = messageSender.rouk(contactNode, rouk, DEADLINE_RUOK);
      logger.debug("id={} imok_received mid={} contactNode={} deadline={}, time={}", id,
          rouk.getId(), contactNode.getId(), 2, (System.currentTimeMillis() - start));
      return Optional.of(imok);
    } catch (StatusRuntimeException e) {
      logger.warn("id={} failure_detected contact={} err={}", id, contactNode.getId(),
          e.getMessage());
      handleActiveFailure(contactNode);
      return Optional.empty();
    }
  }

  void runActiveRepairCheck() {
    /*
    not in protocol, running this in the background to keep the active view flush
    seems to make sense for smaller clusters.
     */
    if (activeRepairTick.incrementAndGet() % 10 == 0) {
      viewLogger.info("id={} overlay {}", id, overlay.toString());
    }

    if (activeViewIsEmpty()) {
      logger.warn("id={} active_repair active_view_empty, calling neighbor from overlay {}", id,
          overlay.toString());
      sendNeighborRequest();
    }
  }

  void sendShuffleRequest() {
    // hpv says nothing about this node being outside the shuffle list, so pick anything
    final Optional<Node> maybe = getRandomNodeFromActiveView();

    if (!maybe.isPresent()) {
      // not in protocol, but seems legit to freak out
      logger.warn("id={} shuffle_request active_view_empty, calling neighbor from overlay {}", id,
          overlay.toString());
      sendNeighborRequest();
      return;
    }

    sendShuffleRequestTo(maybe.get());
  }

  private void sendShuffleRequestTo(Node node) {
    final List<Node> exchangeList = getActiveViewShuffleNodes();
    exchangeList.addAll(getPassiveViewShuffleNodes());
    exchangeList.add(localNode);
    Collections.shuffle(exchangeList);
    final ShuffleMessage request = ShuffleMessage.newBuilder()
        .addAllExchangeList(exchangeList)
        .setId(messageId())
        .setOrigin(localNode)
        .setSender(localNode)
        .setTtl(SHUFFLE_RANDOM_WALK_LENGTH)
        .build();
    logger.debug("id={} shuffle_request exchange_size={} contact={}", id, exchangeList.size(),
        node.getId());
    messageSender.shuffleRequest(node, request);
  }

  private void handleActiveFailure(Node contactNode) {
    logger.warn("id={} active_connection_failed contact={} closing connection, moving to passive",
        id, contactNode.getId());
    messageSender.closeConnection(contactNode);
    moveFromActiveToPassiveView(contactNode);
    sendNeighborRequestExcluding(contactNode);
  }

  private void tryJoinSeed(String seed) {
    logger.info("id={} joining_seed {}", id, seed);

    final Response response =
        messageSender.joinRequest(buildSeedNode(seed), JoinMessage.newBuilder()
            .setSender(localNode)
            .setId(messageId())
            .build());
    addActiveNode(response.getSender());
    logger.info("id={} seed_responded {} overlay {}", id, response.getSender().getId(),
        overlay.toString());
  }

  private void sendNeighborRequestExcluding(Node node) {
    sendNeighbourRequestUsing(() -> getNonMatchingRandomNodeFromPassiveView(node));
  }

  private void sendNeighborRequest() {
    sendNeighbourRequestUsing(this::getRandomNodeFromPassiveView);
  }

  private void sendNeighbourRequestUsing(Supplier<Optional<Node>> supp) {
    boolean done = false;
    final int maxAttempts = 8;
    int attempts = 0;
    while (!done && attempts < maxAttempts) { // todo: use an exp/backoff
      final Optional<Node> maybe = supp.get();
      attempts++;
      if (!maybe.isPresent()) {
        logger.warn("id={} neighbor_request passive_view_empty", id);
        // todo: fall back to seeds and/or sleep a bit?
      } else {
        final Node contact = maybe.get();
        NeighborMessage.Priority priority = NeighborMessage.Priority.low;
        if (activeViewIsEmpty()) {
          logger.warn("id={} neighbor_request active_view_empty, setting prio to high", id);
          priority = NeighborMessage.Priority.high;
        }

        logger.debug("id={} neighbor_request contact={} prio={} {}/{}", id, contact.getId(),
            priority, attempts, maxAttempts);
        final NeighborMessage message = NeighborMessage.newBuilder()
            .setId(messageId())
            .setSender(localNode)
            .setPriority(priority)
            .build();
        NeighbourReplyMessage reply = null;
        try {
          reply = messageSender.neighborRequest(contact, message);
        } catch (StatusRuntimeException e) {
          logger.warn("id={} neighbor_request failed contact={} err={}", id, contact.getId(),
              e.getMessage());
        }

        if (reply != null) {
          final NeighbourReplyMessage.Response response = reply.getResponse();
          if (response.equals(NeighbourReplyMessage.Response.accept)) {
            logger.debug("id={} neighbor_request accepted contact={} prio={} {}/{}", id,
                contact.getId(), priority, attempts, maxAttempts);
            addActiveNode(reply.getSender());
            done = true;
          } else {
            logger.debug("id={} neighbor_request rejected contact={} prio={} {}/{}", id,
                contact.getId(), priority, attempts, maxAttempts);
          }
        }
      }
    }
  }

  private void sendForwardJoin(Node contactNode, Node joiner, int ttl) {
    messageSender.forwardJoinRequest(contactNode, ForwardJoinMessage.newBuilder()
        .setTtl(ttl)
        .setSender(localNode)
        .setId(messageId())
        .setJoiner(joiner)
        .buildPartial());
  }

  private String messageId() {
    return String.format("%016x", ThreadLocalRandom.current().nextLong());
  }

  private Node buildSeedNode(String seed) {
    final String[] split = seed.split(":");
    return Node.newBuilder()
        .setHost(split[0])
        .setPort(Integer.parseInt(split[1]))
        .build();
  }

  private void sendDisconnect(Node contactNode) {
    messageSender.disconnectRequest(contactNode,
        DisconnectMessage.newBuilder()
            .setId(messageId())
            .setSender(localNode)
            .buildPartial());
  }

  private void updatePassiveViewFromShuffle(List<Node> shuffleList) {
    shuffleList.stream()
        .filter(node -> !localNode.equals(node))
        .forEach(this::addNodePassiveView);
  }

  private void addActiveNode(Node node) {
    if (activeViewIsFull()) {
      overlay.tryDropRandomNodeFromActiveView().ifPresent(this::sendDisconnect);
    }

    overlay.addToActiveView(node);
    activeViewChecker.startChecking(node);
  }

  private void addNodePassiveView(Node node) {
    if (!localNode.equals(node)
        && !overlay.activeViewContains(node)
        && !overlay.passiveViewContains(node)) {
      if (overlay.passiveViewIsFull()) {
        logger.debug("id={} making room for {} will dropRandomNodeFromPassiveView", id,
            node.getId());
        overlay.dropRandomNodeFromPassiveView();
      }
      overlay.addToPassiveView(node);
      logger.debug("id={} addNodePassiveView view {}", id, overlay.toString());
    }
  }

  private Set<Node> getActiveViewWithout(Node joiner) {
    return overlay.getActiveViewWithout(joiner);
  }

  private boolean activeViewIsEmpty() {
    return overlay.activeViewIsEmpty();
  }

  private Optional<Node> getNonMatchingRandomNodeFromActiveView(Node node) {
    return overlay.getNonMatchingRandomNodeFromActiveView(node);
  }

  private Optional<Node> getNonMatchingRandomNodeFromPassiveView(Node node) {
    return overlay.getNonMatchingRandomNodeFromPassiveView(node);
  }

  private void moveFromActiveToPassiveView(Node sender) {
    overlay.moveFromActiveToPassiveView(sender);
  }

  private boolean activeViewIsFull() {
    return overlay.activeViewIsFull();
  }

  private Optional<Node> getRandomNodeFromPassiveView() {
    return overlay.getRandomNodeFromPassiveView();
  }

  private int getActiveViewSize() {
    return overlay.getActiveViewSize();
  }

  private List<Node> getRandomNodesFromPassiveView(int num) {
    return overlay.getRandomNodesFromPassiveView(num);
  }

  private Optional<Node> getRandomNodeFromActiveView() {
    return overlay.getRandomNodeFromActiveView();
  }

  private List<Node> getPassiveViewShuffleNodes() {
    return overlay.getRandomNodesFromActiveView(SHUFFLE_PASSIVE);
  }

  private List<Node> getActiveViewShuffleNodes() {
    return overlay.getRandomNodesFromActiveView(SHUFFLE_ACTIVE);
  }
}
