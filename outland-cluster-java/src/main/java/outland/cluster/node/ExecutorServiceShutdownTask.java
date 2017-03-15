package outland.cluster.node;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorServiceShutdownTask {

  private static final Logger logger = LoggerFactory.getLogger("outland");

  public static void shutdown(ExecutorService executorService) {
    executorService.shutdown();

    try {
      if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
          logger.warn("ExecutorService did not shutdown cleanly {}", executorService);
        }
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
