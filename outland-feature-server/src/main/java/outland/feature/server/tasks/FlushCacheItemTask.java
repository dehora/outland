package outland.feature.server.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;
import javax.inject.Inject;
import outland.feature.server.features.FeatureCache;
import outland.feature.server.features.FeatureStorage;

public class FlushCacheItemTask extends Task {

  private final FeatureCache featureCache;
  private final FeatureStorage featureStorage;

  @Inject
  public FlushCacheItemTask(FeatureCache featureCache, FeatureStorage featureStorage) {
    super("FlushCacheItemTask");
    this.featureCache = featureCache;
    this.featureStorage = featureStorage;
  }

  @Override public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output)
      throws Exception {

    final String appKey = parameters.get("ns_key").asList().get(0);
    final String featureKey = parameters.get("feature_key").asList().get(0);
    final String id = parameters.get("id").asList().get(0);

    featureCache.flushCache(appKey, featureKey, id);
  }
}
