package outland.feature;

import com.codahale.metrics.MetricRegistry;
import okhttp3.OkHttpClient;

class OkHttpResourceProvider implements ResourceProvider {

  private final OkHttpClient okHttpClient;
  private final ContentSupport contentSupport;
  private final MetricRegistry metricRegistry;

  OkHttpResourceProvider(OkHttpClient okHttpClient, ContentSupport contentSupport,
      MetricRegistry registry) {

    this.okHttpClient = okHttpClient;
    this.contentSupport = contentSupport;
    this.metricRegistry = registry;
  }

  public Resource newResource() {
    return new OkHttpResource(okHttpClient, contentSupport, metricRegistry);
  }
}
