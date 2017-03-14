package outland.feature.server.features;

import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
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

public class DynamoDbCommand<R> extends HystrixCommand<R> implements MetricsTimer {
  private static final Logger logger = LoggerFactory.getLogger(DynamoDbCommand.class);

  private final String commandName;
  private final Supplier<R> storageAction;
  private final Supplier<R> fallbackAction;
  private final Timer timer;
  private final Meter meter;
  private final Meter failMeter;
  private final Meter throughputFailMeter;

  public DynamoDbCommand(
      String commandName,
      Supplier<R> storageAction,
      Supplier<R> fallbackAction,
      HystrixConfiguration hystrixConfiguration,
      MetricRegistry metrics
  ) {
    super(hystrixConfiguration.getHystrixCommandSetter());
    this.commandName = commandName;
    this.storageAction = storageAction;
    this.fallbackAction = fallbackAction;
    this.timer = metrics.timer(MetricRegistry.name(this.getClass(), commandName + ".timer"));
    this.meter = metrics.meter(MetricRegistry.name(this.getClass(), commandName + ".meter"));
    this.failMeter =
        metrics.meter(MetricRegistry.name(this.getClass(), commandName + ".failMeter"));
    this.throughputFailMeter =
        metrics.meter(MetricRegistry.name(this.getClass(), commandName + ".throughputFailMeter"));
  }

  @Override protected R run() throws Exception {
    meter.mark();
    return timed(timer, storageAction);
  }

  @Override protected R getFallback() {
    failMeter.mark();

    final List<HystrixEventType> events = getExecutionEvents();

    if (isFailure(events)) {
      final Throwable throwable = getFailedExecutionException();

      if (throwable instanceof ProvisionedThroughputExceededException) {
        // todo: figure out how to handle this via fallbackAction and retries
        throughputFailMeter.mark();
        logger.error("{}",
            kvp("command_name", commandName,
                "fallback_event", "ProvisionedThroughputExceededException",
                "time_millis", this.getExecutionTimeInMilliseconds(),
                "err", "[" + throwable.getMessage() + "]"
            ),
            throwable);
      } else {

        logger.warn("{}",
            kvp("command_name", commandName,
                "fallback_event", "error",
                "time_millis", this.getExecutionTimeInMilliseconds(),
                "err", "[" + throwable.getMessage() + "]"
            ),
            throwable);
      }
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

  private boolean isFailure(List<HystrixEventType> eventTypes) {
    return eventTypes.contains(HystrixEventType.EXCEPTION_THROWN)
        || eventTypes.contains(HystrixEventType.FAILURE);
  }

  private void warn(String commandName, String event) {
    logger.warn("{}", kvp(
        "command_name", commandName,
        "fallback_event", event,
        "time", this.getExecutionTimeInMilliseconds()
    ));
  }
}
