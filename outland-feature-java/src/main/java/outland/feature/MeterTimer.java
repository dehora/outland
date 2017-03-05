package outland.feature;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import java.util.function.Supplier;

interface MeterTimer {

  default <T> T metric(Timer timer, Meter meter, Supplier<T> supplier) {
    Timer.Context time = timer.time();
    try {
      meter.mark();
      return supplier.get();
    } finally {
      time.stop();
    }
  }
}

