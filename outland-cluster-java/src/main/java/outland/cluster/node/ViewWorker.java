package outland.cluster.node;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ViewWorker implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger("outland");

  private final String id;
  private final String name;
  private final int delay;
  private final int every;
  private final Supplier<Void> clusterCoordinator;
  private final ScheduledExecutorService executor;

  ViewWorker(String name, String id, Supplier<Void> viewCommand, int delay, int every) {
    this.name = name;
    this.id = id;
    this.clusterCoordinator = viewCommand;
    executor = Executors.newScheduledThreadPool(1,
        new ThreadFactoryBuilder().setNameFormat(name + "-" + id).build());
    this.delay = delay;
    this.every = every;
  }

  void start() {
    logger.debug("id={} scheduling {} worker every {}s delay {}s", id, name, every, delay);
    executor.scheduleAtFixedRate(this, delay, every, TimeUnit.SECONDS);
  }

  void stop() {
    ExecutorServiceShutdownTask.shutdown(executor);
  }

  @Override public void run() {
    logger.debug("id={} woke_up, running {} check", id, name);
    clusterCoordinator.get();
  }
}
