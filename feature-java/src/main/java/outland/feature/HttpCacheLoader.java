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

class HttpCacheLoader extends CacheLoader<String, Feature> {

  private static final Logger logger = LoggerFactory.getLogger(FeatureClient.class.getSimpleName());

  private static final int RELOAD_THREADS = 2;

  private final FeatureClient client;
  private final FeatureStoreLocal backingFeatureStore;

  private final ExecutorService
      reloadExecutor = Executors.newFixedThreadPool(RELOAD_THREADS,
      new ThreadFactoryBuilder()
          .setUncaughtExceptionHandler(
              (t, e) -> logger.error("feature_store_err {}, {}", t, e.getMessage(), e))
          .setNameFormat("outland-feature-%d").build());

  HttpCacheLoader(FeatureClient client, FeatureStoreLocal backingFeatureStore) {
    this.client = client;
    this.backingFeatureStore = backingFeatureStore;
  }

  @Override public Feature load(String key) throws Exception {

    final String[] storageKeySplit = FeatureStoreKeys.storageKeySplit(key);

    final Feature feature = httpLoad(storageKeySplit[0], storageKeySplit[1]);

    if (feature == null) {
      throw new FeatureException(
          Problem.noSuchFeature(storageKeySplit[0] + ", " + storageKeySplit[1], ""));
    }

    backingFeatureStore.put(feature);
    return feature;
  }

  @Override public ListenableFuture<Feature> reload(String key, Feature oldValue)
      throws Exception {

    final String[] storageKeySplit = FeatureStoreKeys.storageKeySplit(key);
    ListenableFutureTask<Feature>
        t = ListenableFutureTask.create(
        () -> httpLoad(storageKeySplit[0], storageKeySplit[1], oldValue));
    reloadExecutor.execute(t);
    return t;
  }

  private Feature httpLoad(String appId, String featureKey) {
    logger.info("op=cache_load_from_api, app_id={}, feature_key={}", appId, featureKey);
    return client.resources().features().findByKey(appId, featureKey);
  }

  private Feature httpLoad(String appId, String featureKey, Feature oldValue) {
    try {
      logger.info("op=background_cache_load_from_api, app_id={}, feature_key={}", appId, featureKey);
      return client.resources().features().findByKey(appId, featureKey);
    } catch (FeatureException e) {
      logger.error(
          String.format(
              "op=background_cache_load_from_api, app_id=%s, feature_key=%s, "
                  + "action=retaining_old_feature, err=%s",
              appId, featureKey, e.getMessage()));
      return oldValue;
    }
  }
}
