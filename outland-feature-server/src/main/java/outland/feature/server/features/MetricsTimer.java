package outland.feature.server.features;

import com.codahale.metrics.Timer;
import java.util.function.Supplier;

public interface MetricsTimer {

  default <T> T timed(Timer timer, Supplier<T> supplier) {
    Timer.Context time = timer.time();
    try {
      return supplier.get();
    } finally {
      time.stop();
    }
  }
}
