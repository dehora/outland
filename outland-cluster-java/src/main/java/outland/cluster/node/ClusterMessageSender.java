package outland.cluster.node;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
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
import outland.cluster.proto.OutlandServiceGrpc;
import outland.cluster.proto.OutlandServiceGrpc.OutlandServiceBlockingStub;

class ClusterMessageSender {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger("tolkan-sender");
  private static final int DEADLINE_DISCONNECT = 8;
  private static final int DEADLINE_FWD_JOIN = 62;
  private static final int DEADLINE_SHUFFLE_REQUEST = 62;
  private static final int DEADLINE_JOIN = 4;
  private static final int DEADLINE_SHUFFLE_REPLY = 16;
  private static final int DEADLINE_NEIGHBOR_REQUEST = 16;

  private final ChannelManager channelManager;
  private final String id;
  private final EventBus senderBus;

  ClusterMessageSender(String id, EventBus bus) {
    this.id = id;
    channelManager = new ChannelManager(id);
    this.senderBus = bus;
    bus.register(this);
  }

  Response joinRequest(Node contact, JoinMessage message) {
    return sendJoinRequest(new JoinRequest(contact, message));
  }

  void forwardJoinRequest(Node contact, ForwardJoinMessage message) {
    senderBus.post(new ForwardJoin(contact, message));
  }

  void disconnectRequest(Node contact, DisconnectMessage message) {
    senderBus.post(new Disconnect(contact, message));
  }

  void shuffleRequest(Node contact, ShuffleMessage message) {
    senderBus.post(new ShuffleRequest(contact, message));
  }

  void shuffleReply(Node contact, ShuffleReplyMessage message) {
    senderBus.post(new ShuffleReply(contact, message));
  }

  NeighbourReplyMessage neighborRequest(Node contact, NeighborMessage message) {
    logger.debug("id={} send_neighbor_request mid={} contact={}", id, message.getId(),
        contact.getId());
    return getBlockingStubFor(contact, DEADLINE_NEIGHBOR_REQUEST).neighborRequest(message);
  }

  Imok rouk(Node contact, Rouk message, int deadline) {
    logger.debug("id={} send_rouk mid={} contact={}", id, message.getId(), contact.getId());
    return getBlockingStubFor(contact, deadline).ruok(message);
  }

  void closeConnection(Node node) {
    channelManager.shutdownChannel(node);
  }

  private Response sendJoinRequest(JoinRequest joinRequest) {
    final Node seed = joinRequest.contact;
    final JoinMessage msg = joinRequest.message;
    final Response response = getBlockingStubFor(seed, DEADLINE_JOIN).joinRequest(msg);
    Node contact = response.getSender();
    logger.debug("id={} sent_join_request mid={} contact={}", id, msg.getId(), contact.getId());
    channelManager.shutdownChannel(seed); // don't cache seed nodes, we don't have an id yet
    return response;
  }

  @Subscribe
  void sendForwardJoin(ForwardJoin forwardJoin) {
    final Node contact = forwardJoin.contact;
    final ForwardJoinMessage msg = forwardJoin.message;
    getBlockingStubFor(contact, DEADLINE_FWD_JOIN).forwardJoinRequest(msg);
    logger.debug("id={} sent_forward_join mid={} contact={}", id, msg.getId(), contact.getId());
  }

  @Subscribe
  void sendDisconnectRequest(Disconnect message) {
    final Node contact = message.contact;
    final DisconnectMessage msg = message.message;
    getBlockingStubFor(contact, DEADLINE_DISCONNECT).disconnectRequest(msg);
    logger.debug("id={} sent_disconnect mid={} contact={} sent", id, msg.getId(), contact.getId());
    channelManager.shutdownChannel(contact);
  }

  @Subscribe
  void sendShuffleRequest(ShuffleRequest message) {
    try {
      getBlockingStubFor(message.contact, DEADLINE_SHUFFLE_REQUEST).shuffleRequest(message.message);
      logger.debug("id={} send_shuffle_request mid={} contact={}", id, message.message.getId(),
          message.contact.getId());
    } catch (StatusRuntimeException e) {
      logger.warn("id={} shuffle_request_failed contact={} err={}", id, message.contact.getId(),
          e.getMessage());
    }
  }

  @Subscribe
  void sendShuffleReply(ShuffleReply message) {
    try {
      getBlockingStubFor(message.contact, DEADLINE_SHUFFLE_REPLY).shuffleReply(message.message);
      logger.debug("id={} sent_shuffle_reply mid={} contact={} ", id, message.message.getId(),
          message.contact.getId());
    } catch (StatusRuntimeException e) {
      logger.warn("id={} shuffle_reply_failed contact={} err={}", id, message.contact.getId(),
          e.getMessage());
    }
  }

  void shutdown() {
    channelManager.shutdown();
  }

  private OutlandServiceBlockingStub getBlockingStubFor(Node contactNode, int deadline) {
    return OutlandServiceGrpc.newBlockingStub(channelManager.getChannel(contactNode))
        .withDeadlineAfter(deadline, TimeUnit.SECONDS);
  }

  private static final class ChannelManager {

    private static final org.slf4j.Logger logger =
        LoggerFactory.getLogger("tolkan-channel-manager");

    final RemovalListener<Node, ManagedChannel> removalListener;
    private final LoadingCache<Node, ManagedChannel> channels;

    private final String id;
    private AtomicLong stubRequestCounter = new AtomicLong(0);
    private AtomicLong stubRemoveCounter = new AtomicLong(0);

    ChannelManager(String id) {
      this.id = id;
      removalListener = buildRemovalListener();
      channels = buildChannelCache(removalListener);
    }

    ManagedChannel getChannel(final Node node) {
      if (logger.isTraceEnabled()) {
        logger.trace("id={} channel data, nodes={} stats={}", id,
            channels.asMap()
                .keySet()
                .stream()
                .map(Node::getId)
                .collect(Collectors.toList())
                .toString(),
            channels.stats().toString());
      }

      if (logger.isDebugEnabled()) {
        logger.debug("id={} get_managed_channel for {} stub stub_removals={} stub_count={}", id,
            node.getId(), stubRemoveCounter.get(), stubRequestCounter.incrementAndGet());
      }

      return channels.getUnchecked(node);
    }

    void shutdownChannel(final Node node) {
      if (logger.isTraceEnabled()) {
        logger.trace("id={} channel_data, nodes={} stats={}", id,
            channels.asMap()
                .keySet()
                .stream()
                .map(Node::getId)
                .collect(Collectors.toList())
                .toString(),
            channels.stats().toString());
      }

      if (logger.isDebugEnabled()) {
        logger.debug("id={} remove_managed_channel node={} stub stub_removals={} stub_count={}", id,
            node.getId(), stubRemoveCounter.incrementAndGet(), stubRequestCounter.get());
      }

      channels.invalidate(node);
    }

    void shutdown() {
      if (logger.isInfoEnabled()) {
        logger.info("id={} channel_data, nodes={} stats={}", id,
            channels.asMap()
                .keySet()
                .stream()
                .map(Node::getId)
                .collect(Collectors.toList())
                .toString(),
            channels.stats().toString());
      }
      channels.invalidateAll();
    }

    private LoadingCache<Node, ManagedChannel> buildChannelCache(
        RemovalListener<Node, ManagedChannel> removalListener) {
      return CacheBuilder.newBuilder()
          .removalListener(removalListener)
          .recordStats()
          .build(
              new CacheLoader<Node, ManagedChannel>() {
                public ManagedChannel load(Node node) {
                  return createChannel(node);
                }
              });
    }

    private RemovalListener<Node, ManagedChannel> buildRemovalListener() {
      return removal -> {
        final ManagedChannel channel = removal.getValue();
        if (channel != null) {
          logger.debug("removing channel {}", channel);
          try {
            if (!channel.awaitTermination(3, TimeUnit.SECONDS)) {
              channel.shutdownNow();
              if (!channel.awaitTermination(3, TimeUnit.SECONDS)) {
                logger.warn("id={} channel did not shutdown cleanly {}", channel);
              }
            }
          } catch (InterruptedException e) {
            channel.shutdownNow();
            Thread.currentThread().interrupt();
          }
        }
      };
    }

    private ManagedChannel createChannel(Node node) {
      logger.debug("id={} create_channel contact={} {}:{}", id, node.getId(), node.getHost(),
          node.getPort());
      return ManagedChannelBuilder
          .forAddress(node.getHost(), node.getPort())
          .usePlaintext(true)
          .build();
    }
  }

  private static class JoinRequest {
    Node contact;
    JoinMessage message;

    JoinRequest(Node contact, JoinMessage message) {
      this.contact = contact;
      this.message = message;
    }
  }

  private static class ForwardJoin {
    Node contact;
    ForwardJoinMessage message;

    ForwardJoin(Node contact, ForwardJoinMessage message) {
      this.contact = contact;
      this.message = message;
    }
  }

  private static class Disconnect {
    Node contact;
    DisconnectMessage message;

    Disconnect(Node contact, DisconnectMessage message) {
      this.contact = contact;
      this.message = message;
    }
  }

  private static class ShuffleRequest {
    Node contact;
    ShuffleMessage message;

    ShuffleRequest(Node contact, ShuffleMessage message) {
      this.contact = contact;
      this.message = message;
    }
  }

  private static class ShuffleReply {
    Node contact;
    ShuffleReplyMessage message;

    ShuffleReply(Node contact, ShuffleReplyMessage message) {
      this.contact = contact;
      this.message = message;
    }
  }
}
