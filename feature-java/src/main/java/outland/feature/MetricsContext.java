package outland.feature;

import com.codahale.metrics.MetricRegistry;

class MetricsContext {

  private String context;
  private MetricRegistry metrics;

  public MetricsContext(String context, MetricRegistry metrics) {
    this.context = context;
    this.metrics = metrics;
  }

  public String context() {
    return context;
  }

  public MetricRegistry metrics() {
    return metrics;
  }
}
