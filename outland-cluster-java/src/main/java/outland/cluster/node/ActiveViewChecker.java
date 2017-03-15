package outland.cluster.node;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.cluster.proto.Imok;
import outland.cluster.proto.Node;

class ActiveViewChecker {

  private static final Logger logger = LoggerFactory.getLogger("tolkan-ruok");

  private final ScheduledThreadPoolExecutor ruokExecutor;
  private final String id;
  private final ClusterCoordinator clusterCoordinator;
  private final Map<String, RuokRunnable> runnables = Maps.newHashMap();

  ActiveViewChecker(String id, ClusterCoordinator clusterCoordinator) {
    this.id = id;
    this.clusterCoordinator = clusterCoordinator;
    final ThreadFactory tf =
        new ThreadFactoryBuilder().setNameFormat("tolkan-ruok-" + this.id).build();
    ruokExecutor = new ScheduledThreadPoolExecutor(2, tf);
  }

  void stopChecking(final Node contactNode) {
    if (runnables.containsKey(contactNode.getId())) {
      runnables.get(contactNode.getId()).setFailed(true);
      runnables.remove(contactNode.getId());
    }
  }

  void startChecking(final Node contact) {

    synchronized (this) {
      if (runnables.containsKey(contact.getId())) {
        if (runnables.containsKey(contact.getId()) && !runnables.get(contact.getId()).isFailed()) {
          logger.debug("id={} already running checks for {}", id, contact.getId());
          return;
        }
      }

      final RuokRunnable runnable = new RuokRunnable(id, clusterCoordinator, contact);
      runnables.put(contact.getId(), runnable);
      logger.debug("id={} runnables={}", id, runnables.size());
      runnable.runWith(ruokExecutor, 1, 1, TimeUnit.SECONDS);
      ruokExecutor.purge();
    }
  }

  private static class RuokRunnable implements Runnable {

    private final String id;
    private final ClusterCoordinator clusterCoordinator;
    private final Node contactNode;
    private volatile boolean failed = false;
    private volatile ScheduledFuture<?> myFuture;

    RuokRunnable(String id, ClusterCoordinator clusterCoordinator, Node contactNode) {
      this.id = id;
      this.clusterCoordinator = clusterCoordinator;
      this.contactNode = contactNode;
    }

    void runWith(ScheduledThreadPoolExecutor executor, long initial, long every, TimeUnit unit) {
      myFuture = executor.scheduleAtFixedRate(this, initial, every, unit);
    }

    boolean isFailed() {
      return failed;
    }

    void setFailed(boolean failed) {
      this.failed = failed;
    }

    @Override public void run() {

      if (failed) {
        boolean interrupted = false;
        try {
          while (myFuture == null) {
            try {
              Thread.sleep(8);
            } catch (InterruptedException e) {
              interrupted = true;
            }
            logger.debug("id={} cancelling run for contact={}", id, contactNode.getId());
            myFuture.cancel(true);
          }
        } finally {
          if (interrupted) {
            Thread.currentThread().interrupt();
          }
        }
      } else {
        final Optional<Imok> imok = clusterCoordinator.sendRouk(contactNode);
        if (!imok.isPresent()) {
          failed = true;
          logger.info("id={} failure detected for contact={}", id, contactNode.getId());
        }
      }
    }
  }
}
