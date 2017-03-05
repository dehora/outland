package outland.feature.server.hystrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Map hystrix configuration into dropwizard. See {@link HystrixCommandProperties} for an
 * explanation of the properties and where the defaults below are taken from.
 */
public class HystrixConfiguration {

  @NotEmpty
  @JsonProperty
  public String hystrixCommandGroupKey;

  @JsonProperty
  public int circuitBreakerRequestVolumeThreshold = 20;

  @JsonProperty
  public int circuitBreakerSleepWindowInMilliseconds = 5000;

  @JsonProperty
  public Boolean circuitBreakerEnabled = true;

  @JsonProperty
  public int circuitBreakerErrorThresholdPercentage = 50;

  @JsonProperty
  public Boolean circuitBreakerForceOpen = false;

  @JsonProperty
  public Boolean circuitBreakerForceClosed = false;

  @JsonProperty
  public int executionTimeoutInMilliseconds = 1000;

  @JsonProperty
  public boolean executionTimeoutEnabled = true;

  @JsonProperty
  public HystrixCommandProperties.ExecutionIsolationStrategy executionIsolationStrategy =
      HystrixCommandProperties.ExecutionIsolationStrategy.THREAD;

  @JsonProperty
  public Integer executionIsolationThreadTimeoutInMilliseconds = 1000;

  @JsonProperty
  public Boolean executionIsolationThreadInterruptOnTimeout = true;

  @JsonProperty
  public boolean executionIsolationThreadInterruptOnFutureCancel = false;

  @JsonProperty
  public String executionIsolationThreadPoolKeyOverride;

  @JsonProperty
  public Integer executionIsolationSemaphoreMaxConcurrentRequests = 10;

  @JsonProperty
  public Integer fallbackIsolationSemaphoreMaxConcurrentRequests = 10;

  @JsonProperty
  public Boolean fallbackEnabled = true;

  @JsonProperty
  public int metricsRollingStatisticalWindowInMilliseconds = 8000;

  @JsonProperty
  public int metricsRollingPercentileWindowBuckets = 6;

  @JsonProperty
  public int metricsRollingPercentileBucketSize = 100;

  @JsonProperty
  public boolean metricsRollingPercentileEnabled = true;

  @JsonProperty
  public int metricsRollingStatisticalWindowBuckets = 10;

  @JsonProperty
  public int metricsHealthSnapshotIntervalInMilliseconds = 500;

  @JsonProperty
  public int metricsRollingPercentileWindowInMilliseconds = 60000;

  @JsonProperty
  public Boolean requestLogEnabled = true;

  @JsonProperty
  public Boolean requestCacheEnabled = true;

  @JsonProperty
  public long sleepWindowInMillis = 3000;

  @JsonProperty
  public ThreadPoolConfiguration threadpool = new ThreadPoolConfiguration();

  public HystrixCommand.Setter getHystrixCommandSetter() {
    return HystrixCommand.Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(hystrixCommandGroupKey))
        .andCommandPropertiesDefaults(getHystrixCommandProperties())
        .andThreadPoolPropertiesDefaults(threadpool.getHystrixThreadPoolPropertiesSetter());
  }

  public HystrixCommandProperties.Setter getHystrixCommandProperties() {
    return HystrixCommandProperties.Setter()
        .withRequestLogEnabled(
            requestLogEnabled)
        .withCircuitBreakerEnabled(
            circuitBreakerEnabled)
        .withCircuitBreakerErrorThresholdPercentage(
            circuitBreakerErrorThresholdPercentage)
        .withCircuitBreakerForceClosed(
            circuitBreakerForceClosed)
        .withCircuitBreakerForceOpen(
            circuitBreakerForceOpen)
        .withCircuitBreakerRequestVolumeThreshold(
            circuitBreakerRequestVolumeThreshold)
        .withCircuitBreakerSleepWindowInMilliseconds(
            circuitBreakerSleepWindowInMilliseconds)
        .withExecutionTimeoutInMilliseconds(
            executionTimeoutInMilliseconds)
        .withExecutionTimeoutEnabled(
            executionTimeoutEnabled)
        .withExecutionIsolationThreadInterruptOnFutureCancel(
            executionIsolationThreadInterruptOnFutureCancel)
        .withExecutionIsolationSemaphoreMaxConcurrentRequests(
            executionIsolationSemaphoreMaxConcurrentRequests)
        .withExecutionIsolationStrategy(
            executionIsolationStrategy)
        .withExecutionIsolationThreadInterruptOnTimeout(
            executionIsolationThreadInterruptOnTimeout)
        // deprecated
        //.withExecutionIsolationThreadTimeoutInMilliseconds(
        //    executionIsolationThreadTimeoutInMilliseconds)
        .withFallbackEnabled(
            fallbackEnabled)
        .withFallbackIsolationSemaphoreMaxConcurrentRequests(
            fallbackIsolationSemaphoreMaxConcurrentRequests)
        .withMetricsHealthSnapshotIntervalInMilliseconds(
            metricsHealthSnapshotIntervalInMilliseconds)
        .withMetricsRollingPercentileBucketSize(
            metricsRollingPercentileBucketSize)
        .withMetricsRollingPercentileEnabled(
            metricsRollingPercentileEnabled)
        .withMetricsRollingPercentileWindowBuckets(
            metricsRollingPercentileWindowBuckets)
        .withMetricsRollingPercentileWindowInMilliseconds(
            metricsRollingPercentileWindowInMilliseconds)
        .withMetricsRollingStatisticalWindowBuckets(
            metricsRollingPercentileWindowBuckets)
        .withMetricsRollingStatisticalWindowBuckets(
            metricsRollingStatisticalWindowBuckets)
        .withMetricsRollingStatisticalWindowInMilliseconds(
            metricsRollingStatisticalWindowInMilliseconds)
        .withRequestCacheEnabled(
            requestCacheEnabled)
        .withRequestLogEnabled(
            requestLogEnabled)
        ;
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hystrixCommandGroupKey", hystrixCommandGroupKey)
        .add("circuitBreakerRequestVolumeThreshold", circuitBreakerRequestVolumeThreshold)
        .add("circuitBreakerSleepWindowInMilliseconds", circuitBreakerSleepWindowInMilliseconds)
        .add("circuitBreakerEnabled", circuitBreakerEnabled)
        .add("circuitBreakerErrorThresholdPercentage", circuitBreakerErrorThresholdPercentage)
        .add("circuitBreakerForceOpen", circuitBreakerForceOpen)
        .add("circuitBreakerForceClosed", circuitBreakerForceClosed)
        .add("executionTimeoutInMilliseconds", executionTimeoutInMilliseconds)
        .add("executionTimeoutEnabled", executionTimeoutEnabled)
        .add("executionIsolationStrategy", executionIsolationStrategy)
        .add("executionIsolationThreadTimeoutInMilliseconds",
            executionIsolationThreadTimeoutInMilliseconds)
        .add("executionIsolationThreadInterruptOnTimeout",
            executionIsolationThreadInterruptOnTimeout)
        .add("executionIsolationThreadInterruptOnFutureCancel",
            executionIsolationThreadInterruptOnFutureCancel)
        .add("executionIsolationThreadPoolKeyOverride", executionIsolationThreadPoolKeyOverride)
        .add("executionIsolationSemaphoreMaxConcurrentRequests",
            executionIsolationSemaphoreMaxConcurrentRequests)
        .add("fallbackIsolationSemaphoreMaxConcurrentRequests",
            fallbackIsolationSemaphoreMaxConcurrentRequests)
        .add("fallbackEnabled", fallbackEnabled)
        .add("metricsRollingStatisticalWindowInMilliseconds",
            metricsRollingStatisticalWindowInMilliseconds)
        .add("metricsRollingPercentileWindowBuckets", metricsRollingPercentileWindowBuckets)
        .add("metricsRollingPercentileBucketSize", metricsRollingPercentileBucketSize)
        .add("metricsRollingPercentileEnabled", metricsRollingPercentileEnabled)
        .add("metricsRollingStatisticalWindowBuckets", metricsRollingStatisticalWindowBuckets)
        .add("metricsHealthSnapshotIntervalInMilliseconds",
            metricsHealthSnapshotIntervalInMilliseconds)
        .add("metricsRollingPercentileWindowInMilliseconds",
            metricsRollingPercentileWindowInMilliseconds)
        .add("requestLogEnabled", requestLogEnabled)
        .add("requestCacheEnabled", requestCacheEnabled)
        .add("sleepWindowInMillis", sleepWindowInMillis)
        .add("threadpool", threadpool)
        .toString();
  }

  public static class ThreadPoolConfiguration {

    @JsonProperty
    public int coreSize = 10;

    @JsonProperty
    public int maximumSize = 10;

    @JsonProperty
    public int keepAliveTimeMinutes = 1;

    @JsonProperty
    public int maxQueueSize = -1;

    @JsonProperty
    public int queueSizeRejectionThreshold = 5;

    @JsonProperty
    public int metricsRollingStatisticalWindowInMilliseconds;

    @JsonProperty
    public int metricsRollingStatisticalWindowBuckets = 10;

    public HystrixThreadPoolProperties.Setter getHystrixThreadPoolPropertiesSetter() {
      return HystrixThreadPoolProperties.Setter()
          .withCoreSize(
              coreSize)
          .withKeepAliveTimeMinutes(
              keepAliveTimeMinutes)
          .withMaxQueueSize(
              maxQueueSize)
          .withMetricsRollingStatisticalWindowBuckets(
              metricsRollingStatisticalWindowBuckets)
          //.withMetricsRollingStatisticalWindowInMilliseconds(
          //    metricsRollingStatisticalWindowInMilliseconds)
          ;
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("coreSize", coreSize)
          .add("maximumSize", maximumSize)
          .add("keepAliveTimeMinutes", keepAliveTimeMinutes)
          .add("maxQueueSize", maxQueueSize)
          .add("queueSizeRejectionThreshold", queueSizeRejectionThreshold)
          .add("metricsRollingStatisticalWindowInMilliseconds",
              metricsRollingStatisticalWindowInMilliseconds)
          .add("metricsRollingStatisticalWindowBuckets", metricsRollingStatisticalWindowBuckets)
          .toString();
    }
  }
}
