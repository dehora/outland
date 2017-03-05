package outland.feature.server.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;
import javax.inject.Inject;
import outland.feature.server.features.FeatureCache;

public class FlushCacheTask extends Task {

  private final FeatureCache featureCache;

  @Inject
  public FlushCacheTask(FeatureCache featureCache) {
    super("FlushCacheTask");
    this.featureCache = featureCache;
  }

  @Override public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output)
      throws Exception {
    featureCache.flushAll();
  }
}
