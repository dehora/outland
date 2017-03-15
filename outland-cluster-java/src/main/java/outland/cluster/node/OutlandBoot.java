package outland.cluster.node;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;
import org.slf4j.LoggerFactory;
import outland.cluster.proto.Node;

class OutlandBoot {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger("outland");
  private static final int PASSIVE_VIEW_WORKER_DELAY = 30;
  private static final int ACTIVE_VIEW_WORKER_DELAY = 30;

  private final String id;
  private final List<String> seeds;
  private final ExecutorService senderExecutorService;
  private final Node localNode;
  private final ClusterCoordinator clusterCoordinator;
  private final GrpcServiceWorker serviceWorker;
  private final ClusterMessageSender sender;
  private ViewWorker passiveViewWorker;
  private ViewWorker activeViewWorker;
  private ThreadFactory threadFactory;
  private ExecutorService executorService;

  OutlandBoot(String id, String hostnamePort, List<String> seeds) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(hostnamePort);
    Preconditions.checkNotNull(seeds);
    Preconditions.checkArgument(seeds.size() > 0, "Please supply non-empty seed list");

    this.id = id;
    String[] split = hostnamePort.split(":");
    String hostname = split[0];
    int port = Integer.parseInt(split[1]);
    this.seeds = seeds;
    localNode = Node.newBuilder().setHost(hostname).setPort(port).setId(id).build();
    senderExecutorService = Executors.newFixedThreadPool(4);
    final EventBus senderBus = new AsyncEventBus("tolkan-bus", senderExecutorService);
    final ClusterOverlay clusterOverlay = new ClusterOverlay(id, localNode);
    sender = new ClusterMessageSender(id, senderBus);
    clusterCoordinator = new ClusterCoordinator(id, sender, localNode, clusterOverlay, seeds);
    final Supplier<Void> passive = () -> {
      clusterCoordinator.sendShuffleRequest();
      return null;
    };
    passiveViewWorker = new ViewWorker("passive-view", id, passive, PASSIVE_VIEW_WORKER_DELAY, 1);
    final Supplier<Void> active = () -> {
      clusterCoordinator.runActiveRepairCheck();
      return null;
    };
    activeViewWorker = new ViewWorker("active-view", id, active, ACTIVE_VIEW_WORKER_DELAY, 1);
    threadFactory = new ThreadFactoryBuilder().setNameFormat("tolkan-" + id).build();
    executorService = Executors.newFixedThreadPool(2, threadFactory);
    serviceWorker = new GrpcServiceWorker(id, hostname, port, clusterCoordinator);
  }

  void start() {
    passiveViewWorker.start();
    activeViewWorker.start();
    executorService.submit(serviceWorker);
  }

  void stop() {
    logger.info("id={} stopping_node", localNode.getId());

    // todo: send disconnects

    if (passiveViewWorker != null) {
      passiveViewWorker.stop();
    }

    if (activeViewWorker != null) {
      activeViewWorker.stop();
    }

    if (senderExecutorService != null) {
      ExecutorServiceShutdownTask.shutdown(senderExecutorService);
    }

    serviceWorker.stop();

    sender.shutdown(); // close open channels
  }
}
