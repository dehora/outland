package outland.feature;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.util.Optional;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

class FeatureStoreMetered implements FeatureStore, MeterTimer {

  private static final String METRICS_PATH = "outland.feature.client.store";

  private final FeatureStore featureStore;
  private final Timer putTimer;
  private final Meter putMeter;
  private final Timer findTimer;
  private final Meter findMeter;
  private final Timer findAllTimer;
  private final Meter findAllMeter;
  private final Timer flushTimer;
  private final Meter flushMeter;
  private final Timer flushAllTimer;
  private final Meter flushAllMeter;

  FeatureStoreMetered(FeatureStore featureStore, MetricsContext metricsContext) {
    this.featureStore = featureStore;

    MetricRegistry metrics = metricsContext.metrics();
    String context = metricsContext.context();

    this.putTimer = metrics.timer(MetricRegistry.name(
        context + "." + METRICS_PATH, "putTimer"));
    this.putMeter = metrics.meter(MetricRegistry.name(
        context + "." + METRICS_PATH, "putMeter"));
    this.findTimer = metrics.timer(MetricRegistry.name(
        metricsContext + "." + METRICS_PATH, "findTimer"));
    this.findMeter = metrics.meter(MetricRegistry.name(
        context + "." + METRICS_PATH, "findMeter"));
    this.findAllTimer = metrics.timer(MetricRegistry.name(
        context + "." + METRICS_PATH, "findAllTimer"));
    this.findAllMeter = metrics.meter(MetricRegistry.name(
        context + "." + METRICS_PATH, "findAllMeter"));
    this.flushTimer = metrics.timer(MetricRegistry.name(
        context + "." + METRICS_PATH, "flushTimer"));
    this.flushMeter = metrics.meter(MetricRegistry.name(
        context + "." + METRICS_PATH, "flushMeter"));
    this.flushAllTimer = metrics.timer(MetricRegistry.name(
        context + "." + METRICS_PATH, "flushAllTimer"));
    this.flushAllMeter = metrics.meter(MetricRegistry.name(
        context + "." + METRICS_PATH, "flushAllMeter"));
  }

  @Override public Void put(Feature feature) {
    return metric(putTimer, putMeter, () -> featureStore.put(feature));
  }

  @Override public Optional<Feature> find(String group, String key) {
    return metric(findTimer, findMeter, () -> featureStore.find(group, key));
  }

  @Override public FeatureCollection findAll(String group) {
    return metric(findAllTimer, findAllMeter, () -> featureStore.findAll(group));
  }

  @Override public Void remove(String group, String featureKey) {
    return metric(flushTimer, flushMeter, () -> featureStore.remove(group, featureKey));
  }

  @Override public Void removeAll() {
    return metric(flushAllTimer, flushAllMeter, featureStore::removeAll);
  }

  @Override public void close() {
    featureStore.close();
  }
}
