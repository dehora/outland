package outland.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

class FeatureStoreReal implements FeatureStore {

  private static final Logger logger = LoggerFactory.getLogger(FeatureClient.class.getSimpleName());

  static final long MAX_CACHE_SIZE = 2048L;
  static final int INITIAL_CAPACITY = 50;
  static final int REFRESH_AFTER_WRITE_S = 8;

  private final FeatureClient client;
  private final FeatureStoreLocal backingFeatureStore;
  private LoadingCache<String, Feature> featureCache;
  private long maxCacheSize = FeatureStoreReal.MAX_CACHE_SIZE;
  private int initialCacheSize = FeatureStoreReal.INITIAL_CAPACITY;
  private long refreshCacheAfterWriteSeconds = FeatureStoreReal.REFRESH_AFTER_WRITE_S;

  @VisibleForTesting AtomicBoolean loadFromApiSuccessful = new AtomicBoolean(false);
  @VisibleForTesting AtomicBoolean loadFromLocalSuccessful = new AtomicBoolean(false);
  @VisibleForTesting AtomicBoolean loadFromApiAttempted = new AtomicBoolean(false);
  @VisibleForTesting AtomicBoolean loadFromLocalAttempted = new AtomicBoolean(false);

  FeatureStoreReal(FeatureClient client, FeatureStoreLocal backingFeatureStore) {
    this.client = client;
    this.backingFeatureStore = backingFeatureStore;
    this.featureCache = buildCache(client, backingFeatureStore);
    this.maxCacheSize = client.serverConfiguration().maxCacheSize();
    this.initialCacheSize  = client.serverConfiguration().initialCacheSize();
    this.refreshCacheAfterWriteSeconds = client.serverConfiguration().refreshCacheAfterWriteSeconds();
  }

  void open() {
    loadFeaturesIntoCache();
  }

  @Override public Void put(Feature feature) {
    final String storageKey = FeatureStoreKeys.storageKey(feature.getAppId(), feature.getKey());

    logger.info("op=put, storage=cache, app_id={}, feature_key={} storage_key={}",
        feature.getAppId(), feature.getKey(), storageKey);

    featureCache.put(storageKey, feature);

    // stash in our local backing store to provide a fast local loader
    backingFeatureStore.put(feature);

    return null;
  }

  @Override public Optional<Feature> find(String appId, String featureKey) {
    try {

      final String storageKey = FeatureStoreKeys.storageKey(appId, featureKey);

      logger.debug("op=find, app_id={}, feature_key={}, storage_key={}",
          appId, featureKey, storageKey);

      return Optional.ofNullable(featureCache.get(storageKey));
    } catch (ExecutionException | UncheckedExecutionException e) {
      logger.warn(String.format("error finding %s %s", featureKey, e.getMessage()));
      return Optional.empty();
    }
  }

  @Override public FeatureCollection findAll(String appId) {
    logger.info("op=findAll, app_id={}", appId);

    return client.resources().features().listFeatures(appId);
  }

  @Override public Void remove(String appId, String featureKey) {

    final String storageKey = FeatureStoreKeys.storageKey(appId, featureKey);

    logger.info("op=remove, app_id={}, feature_key={} storage_key={}",
        appId, featureKey, storageKey);

    featureCache.invalidate(storageKey);
    return null;
  }

  @Override public Void removeAll() {
    logger.info("op=removeAll");
    featureCache.invalidateAll();
    return null;
  }

  @Override public void close() {

    if (client.localStoreEnabled()) {
      // flush in memory keys to local store
      try {
        final ConcurrentMap<String, Feature> map = featureCache.asMap();
        final Set<Map.Entry<String, Feature>> entries = map.entrySet();
        for (Map.Entry<String, Feature> entry : entries) {
          logger.info("op=close, action=flush_feature_to_local_store, app_id={}, feature_key={}",
              entry.getValue().getAppId(), entry.getValue().getKey());
          backingFeatureStore.put(entry.getValue());
        }
      } catch (Exception e) {
        logger.warn("{} {}", e.getClass(), e.getMessage());
      }
    }

    backingFeatureStore.close();
  }

  private LoadingCache<String, Feature> buildCache(FeatureClient client,
      FeatureStoreLocal backingFeatureStore) {
    return CacheBuilder.newBuilder()
        .recordStats()
        .maximumSize(maxCacheSize)
        .refreshAfterWrite(refreshCacheAfterWriteSeconds, TimeUnit.SECONDS)
        .initialCapacity(initialCacheSize)
        .build(new HttpCacheLoader(client, backingFeatureStore));
  }

  private void loadFeaturesIntoCache() {

    // only call the api when we are working with one app id
    if (client.appId() != null) {
      loadFromApiAttempted.getAndSet(true);
      logger.info("op=populateCache, action=attempt_load, source=api");
      if (loadedFromApi(client.appId())) {
        loadFromApiSuccessful.getAndSet(true);
        logger.info("op=populateCache, action=attempt_load, source=api result=ok");
      }
    }

    if (loadFromApiSuccessful.get()) {
      return;
    }

    if (client.localStoreEnabled()) {
      loadFromLocalAttempted.getAndSet(true);
      logger.info("op=populateCache, action=attempt_load, source=local");
      if (loadedFromLocal()) {
        loadFromLocalSuccessful.getAndSet(true);
        logger.info("op=populateCache, action=attempt_load, source=local, result=ok");
      }
    }
  }

  private boolean loadedFromApi(String appId) {

    try {
      FeatureCollection all = findAll(appId);
      // todo: do we need a distinction between empty and failed or trust the exception?
      if (all != null) {
        logger.info("op=populateCache, action=load, source=api, feature_count={}, app_id={}",
            all.getItemsCount(),
            appId);
        all.getItemsList().forEach(this::put);
        return true;
      }
    } catch (FeatureException e) {
      logger.error(
          String.format("op=populateCache, action=load, source=api, app_id=%s err=%s",
              appId, e.problem().toMessage()));
    }

    return false;
  }

  private boolean loadedFromLocal() {
    try {

      FeatureCollection all = null;
      if(client.multiAppEnabled()) {
        logger.info("op=loadedFromLocal, action=attempt_load_multi_app, source=local");
        all = backingFeatureStore.loadAll();
      } else {
        logger.info("op=loadedFromLocal, action=attempt_load_single_app, source=local app_id={}",
            client.appId());
        all = backingFeatureStore.findAll(client.appId());
      }

      if (all != null) {
        final List<Feature> itemsList = all.getItemsList();
        logger.info("op=populateCache, action=load, source=local, feature_count={}",
            all.getItemsCount());

        // access cache directly to avoid writing back out to store
        itemsList.forEach(
            f -> featureCache.put(FeatureStoreKeys.storageKey(f.getAppId(), f.getKey()), f));
        return true;
      } else {
        logger.warn("op=populateCache, action=load, source=local, result=null");
      }
    } catch (FeatureException e) {
      logger.error(String.format("op=populateCache, action=load, source=local, err=%s",
              e.problem().toMessage()));
    }

    return false;
  }
}
