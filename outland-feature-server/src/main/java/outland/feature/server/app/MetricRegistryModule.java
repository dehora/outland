package outland.feature.server.app;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;

/**
 * Make {@link MetricRegistry} available for injection.<p>
 */
public class MetricRegistryModule extends AbstractModule {

  private final MetricRegistry metricsRegistry;

  public MetricRegistryModule(MetricRegistry metricsRegistry) {
    this.metricsRegistry = metricsRegistry;
  }

  @Override
  protected void configure() {
    bind(MetricRegistry.class).toInstance(metricsRegistry);
  }
}
