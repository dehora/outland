package outland.feature.server.features;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixEventType;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.server.hystrix.HystrixConfiguration;

import static outland.feature.server.StructLog.kvp;

class RedisCacheCommand<T> extends HystrixCommand implements MetricsTimer {
  private static final Logger logger = LoggerFactory.getLogger(DefaultFeatureService.class);

  private final String commandName;
  private final Supplier<T> cacheAction;
  private final Supplier<T> fallbackAction;
  private final Timer timer;
  private final Meter meter;
  private final Meter failMeter;

  RedisCacheCommand(
      String commandName,
      Supplier<T> cacheAction,
      Supplier<T> fallbackAction,
      HystrixConfiguration hystrixConfiguration,
      MetricRegistry metrics
  ) {
    super(hystrixConfiguration.getHystrixCommandSetter());
    this.commandName = commandName;
    this.cacheAction = cacheAction;
    this.fallbackAction = fallbackAction;
    this.timer = metrics.timer(MetricRegistry.name(this.getClass(), commandName + ".timer"));
    this.meter = metrics.meter(MetricRegistry.name(this.getClass(), commandName + ".meter"));
    this.failMeter =
        metrics.meter(MetricRegistry.name(this.getClass(), commandName + ".failMeter"));
  }

  protected T run() throws Exception {
    meter.mark();
    return timed(timer, cacheAction);
  }

  @Override
  protected T getFallback() {
    failMeter.mark();

    final List<HystrixEventType> events = getExecutionEvents();

    if (isFailure(events)) {
      final Throwable throwable = getFailedExecutionException();
      logger.warn("{}", kvp("command_name", commandName,
          "fallback_event", "error",
          "time_millis", this.getExecutionTimeInMilliseconds(),
          "err", "[" + throwable.getMessage() + "]"
      ), throwable);
    }

    if (events.contains(HystrixEventType.TIMEOUT)) {
      warn(commandName, HystrixEventType.TIMEOUT.name());
    }

    if (events.contains(HystrixEventType.SHORT_CIRCUITED)) {
      warn(commandName, HystrixEventType.SHORT_CIRCUITED.name());
    }

    if (events.contains(HystrixEventType.BAD_REQUEST)) {
      warn(commandName, HystrixEventType.BAD_REQUEST.name());
    }

    return fallbackAction.get();
  }

  private void warn(String commandName, String event) {
    logger.warn("{}", kvp("command_name", commandName,
        "fallback_event", event,
        "time_millis", this.getExecutionTimeInMilliseconds()
    ));
  }

  private boolean isFailure(List<HystrixEventType> eventTypes) {
    return eventTypes.contains(HystrixEventType.EXCEPTION_THROWN)
        || eventTypes.contains(HystrixEventType.FAILURE);
  }
}
