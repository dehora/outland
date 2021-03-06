package outland.feature;

import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Feature;

class HttpCacheLoader extends CacheLoader<String, FeatureRecord> {

  private static final Logger logger = LoggerFactory.getLogger(FeatureClient.class.getSimpleName());

  private static final int RELOAD_THREADS = 2;

  private final FeatureStoreLocal backingFeatureStore;
  private final Resources resources;
  private final ExecutorService
      reloadExecutor = Executors.newFixedThreadPool(RELOAD_THREADS,
      new ThreadFactoryBuilder()
          .setUncaughtExceptionHandler(
              (t, e) -> logger.error("feature_store_err {}, {}", t, e.getMessage(), e))
          .setNameFormat("outland-feature-%d").build());

  HttpCacheLoader(Resources resources, FeatureStoreLocal backingFeatureStore) {
    this.resources = resources;
    this.backingFeatureStore = backingFeatureStore;
  }

  @Override public FeatureRecord load(String key) throws Exception {

    final String[] storageKeySplit = FeatureStoreKeys.storageKeySplit(key);

    final Feature feature = httpLoad(storageKeySplit[0], storageKeySplit[1]);

    if (feature == null) {
      throw new FeatureException(
          Problem.noSuchFeature(storageKeySplit[0] + ", " + storageKeySplit[1], ""));
    }

    backingFeatureStore.put(feature);
    return newFeatureRecord(feature);
  }

  @Override public ListenableFuture<FeatureRecord> reload(String key, FeatureRecord oldValue)
      throws Exception {

    final String[] storageKeySplit = FeatureStoreKeys.storageKeySplit(key);
    ListenableFutureTask<FeatureRecord>
        t = ListenableFutureTask.create(
        () -> httpLoad(storageKeySplit[0], storageKeySplit[1], oldValue));
    reloadExecutor.execute(t);
    return t;
  }

  private Feature httpLoad(String group, String featureKey) {
    logger.info("op=cache_load_from_api, group={}, feature_key={}", group, featureKey);
    return resources.features().findByKey(group, featureKey);
  }

  private FeatureRecord httpLoad(String group, String featureKey, FeatureRecord oldValue) {
    try {
      logger.info("op=background_cache_load_from_api, group={}, feature_key={}", group, featureKey);
      return newFeatureRecord(resources.features().findByKey(group, featureKey));
    } catch (FeatureException e) {
      logger.error(
          String.format(
              "op=background_cache_load_from_api, group=%s, feature_key=%s, "
                  + "action=retaining_old_feature, err=%s",
              group, featureKey, e.getMessage()));
      return oldValue;
    }
  }

  private FeatureRecord newFeatureRecord(Feature feature) {
    return FeatureRecord.build(feature);
  }
}
