package outland.feature.server.features;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.protobuf.TextFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;
import outland.feature.proto.FeatureVersion;

import static outland.feature.server.StructLog.kvp;

class DefaultFeatureService implements FeatureService, MetricsTimer {

  private static final Logger logger = LoggerFactory.getLogger(DefaultFeatureService.class);

  private static final Ordering<Feature> byUpdatedOrdering = new Ordering<Feature>() {
    public int compare(Feature left, Feature right) {
      OffsetDateTime dtRight = FeatureService.asOffsetDateTime(right.getUpdated());
      OffsetDateTime dtLeft = FeatureService.asOffsetDateTime(left.getUpdated());
      return dtRight.compareTo(dtLeft);
    }
  };

  private final FeatureStorage featureStorage;
  private final FeatureCache featureCache;
  private final VersionService versionService;

  private Timer saveFeatureTimer;
  private Timer updateFeatureTimer;
  private Timer addToCacheTimer;
  private Timer loadFeaturesTimer;
  private Timer loadFeaturesCacheTimer;
  private Meter loadFeatureCacheHitMeter;
  private Meter loadFeaturesCacheHitMeter;
  private Meter loadFeaturesCacheMissMeter;
  private Timer loadFeatureByKeyTimer;
  private Timer loadFeatureCacheTimer;
  private Meter loadFeatureCacheMissMeter;

  @Inject
  public DefaultFeatureService(
      FeatureStorage featureStorage,
      FeatureCache featureCache,
      VersionService versionService,
      MetricRegistry metrics
  ) {
    this.featureStorage = featureStorage;
    this.featureCache = featureCache;
    this.versionService = versionService;
    configureMetrics(metrics);
  }

  @Override public Optional<Feature> registerFeature(Feature registering) {

    logger.info("{} /feature[{}]", kvp("op", "registerFeature"),
        TextFormat.shortDebugString(registering));

    OffsetDateTime now = OffsetDateTime.now();
    String id = "feat_" + Ulid.random(now.toInstant().toEpochMilli());
    String created = FeatureService.asString(now);

    Feature.Builder builder = registering.toBuilder();
    builder.setId(id);
    builder.setCreated(created);
    builder.setUpdated(builder.getCreated());
    builder.setState(Feature.State.off); // always disabled on registerFeature

    applyVersion(registering, builder);

    Feature feature = builder.build();

    timed(saveFeatureTimer, () -> featureStorage.saveFeature(feature));

    logger.info("{} /feature=[{}]", kvp("op", "registerFeature", "result", "ok"),
        TextFormat.shortDebugString(feature));

    addToCache(feature);
    return Optional.of(feature);
  }

  @Override
  public Optional<Feature> updateFeature(String appId, String featureKey, Feature updates) {

    logger.info("{} /update_feature=[{}]",
        kvp("op", "updateFeature", "app_id", appId, "feature_key", featureKey),
        TextFormat.shortDebugString(updates));

    String now = FeatureService.asString(OffsetDateTime.now());
    Optional<Feature> maybeFound =
        timed(loadFeatureByKeyTimer, () -> featureStorage.loadFeatureByKey(appId, featureKey));

    if (!maybeFound.isPresent()) {
      logger.info("updateFeature not_found {} {}", appId, updates);
      return maybeFound;
    }

    Feature found = maybeFound.get();

    logger.info("{} /found_feature=[{}]",
        kvp("op", "updateFeature", "app_id", appId, "feature_key", featureKey),
        TextFormat.shortDebugString(found));

    Feature.Builder builder = found.toBuilder()
        .mergeFrom(updates)
        .setUpdated(now);

    applyVersion(updates, builder);

    // a value other than none indicates the client sent something
    if (!updates.getState().equals(Feature.State.none)) {
      builder.setState(updates.getState());
    }

    Feature updated = builder.build();

    timed(updateFeatureTimer, () -> featureStorage.updateFeature(updated));

    logger.info("{} /updated_feature=[{}]",
        kvp("op", "updateFeature", "app_id", appId, "feature_key", featureKey),
        TextFormat.shortDebugString(updated));

    addToCache(updated);
    return Optional.of(updated);
  }

  @Override public Optional<Feature> loadFeatureByKey(String appId, String featureKey) {

    logger.info("{}", kvp("op", "loadFeatureByKey", "app_id", appId, "feature_key", featureKey));

    Optional<Feature> cached = timed(loadFeatureCacheTimer,
        () -> featureCache.findInCache(featureCache.buildCacheKeyByFeatureKey(appId, featureKey)));

    if (cached.isPresent()) {
      loadFeatureCacheHitMeter.mark();
      logger.info("{} /feature=[{}]", kvp("op", "loadFeatureByKey",
          "app_id", appId,
          "feature_key", featureKey,
          "result", "cache_hit"),
          TextFormat.shortDebugString(cached.get()));
      return cached;
    }

    loadFeatureCacheMissMeter.mark();

    Optional<Feature> feature =
        timed(loadFeatureByKeyTimer, () -> featureStorage.loadFeatureByKey(appId, featureKey));

    if (feature.isPresent()) {
      /*
      drive a full load; this is to avoid the case where a single item is pulled from storage
      added to an empty set, but there are other items in storage such that the single cached
      result will be served, leaving behind the other features.
      */
      logger.info("{}", kvp(
          "op", "loadFeatureByKey",
          "app_id", appId,
          "feature_key", featureKey,
          "msg", "force_cache_reload"
      ));
      addAllToCache(appId, timed(loadFeaturesTimer, () -> featureStorage.loadFeatures(appId)));
      logger.info("{} /feature=[{}]", kvp("op", "loadFeatureByKey",
          "app_id", appId,
          "feature_key", featureKey,
          "result", "cache_miss",
          "msg", "add_to_cache"),
          TextFormat.shortDebugString(feature.get()));
    }

    logger.info("{}", kvp("op", "loadFeatureByKey",
        "app_id", appId,
        "result", feature.isPresent() ? "ok" : "not_found"));

    // todo: signal 404 if empty
    return feature;
  }


  @Override public FeatureCollection loadFeatures(String appId) {

    logger.info("{}", kvp("op", "loadFeatures", "app_id", appId));

    FeatureCollection.Builder builder = FeatureCollection.newBuilder();
    builder.setType("feature.list");
    builder.setAppId(appId);

    Optional<Map<String, String>> cacheSet = timed(loadFeaturesCacheTimer,
        () -> featureCache.getCacheSet(appId));

    if (cacheSet.isPresent()) {
      loadFeaturesCacheHitMeter.mark();
      final ArrayList<Feature> features = Lists.newArrayList();
      Set<Map.Entry<String, String>> entries = cacheSet.get().entrySet();
      for (Map.Entry<String, String> entry : entries) {
        features.add(FeatureSupport.toFeature(entry.getValue()));
      }

      features.sort(byUpdatedOrdering);
      builder.addAllItems(features);
      FeatureCollection featureCollection = builder.build();

      logger.info("{} /features=[{}]", kvp("op", "loadFeatures",
          "app_id", appId,
          "result", "cache_hit"),
          TextFormat.shortDebugString(featureCollection));

      return featureCollection;
    }

    loadFeaturesCacheMissMeter.mark();

    List<Feature> features = timed(loadFeaturesTimer, () -> featureStorage.loadFeatures(appId));
    features.sort(byUpdatedOrdering);
    builder.addAllItems(features);
    FeatureCollection featureCollection = builder.build();

    addAllToCache(appId, features);

    logger.info("{} /features=[{}]", kvp("op", "loadFeatures",
        "app_id", appId,
        "result", "ok"),
        TextFormat.shortDebugString(featureCollection));

    return featureCollection;
  }

  @Override public FeatureCollection loadFeaturesChangedSince(String appId, OffsetDateTime since) {

     logger.info("{}", kvp("op", "loadFeaturesSince", "app_id", appId, "since", since));

    FeatureCollection collection = loadFeatures(appId);

    List<Feature> sinceList = Lists.newArrayList();

    List<Feature> list = collection.getItemsList();
    for (Feature feature : list) {
      OffsetDateTime time = FeatureService.asOffsetDateTime(feature.getUpdated());
      if (time.isAfter(since)) {
        sinceList.add(feature);
      }
    }

    sinceList.sort(byUpdatedOrdering);

    FeatureCollection.Builder builder = FeatureCollection.newBuilder();
    final FeatureCollection featureCollection = builder
        .addAllItems(sinceList)
        .setAppId(appId)
        .setType("feature.list")
        .build();

    logger.info("{} /features=[{}]",
        kvp("op", "loadFeaturesSince",
            "app_id", appId,
            "since", since,
            "result", "ok"),
        TextFormat.shortDebugString(featureCollection));

    return featureCollection;
  }

  private void applyVersion(Feature registering, Feature.Builder builder) {

    VersionService.HybridLogicalTimestamp next;
    if (registering.hasVersion()) {
      next = versionService.nextVersionUpdate(new VersionService.HybridLogicalTimestamp(
          registering.getVersion().getTimestamp(),
          registering.getVersion().getCounter()));
    } else {
      next = versionService.nextVersion();
    }

    builder.setVersion(FeatureVersion.newBuilder()
        .setCounter(next.counter())
        .setTimestamp(next.logicalTime())
        .setId(next.id()));
  }

  private void addAllToCache(String appId, List<Feature> features) {
    for (Feature feature : features) {

      logger.info("{} /feature=[{}]", kvp("op", "loadFeatures",
          "app_id", appId,
          "result", "cache_miss",
          "msg", "add_to_cache"),
          TextFormat.shortDebugString(feature));

      addToCache(feature);
    }
  }

  private void addToCache(Feature feature) {
    timed(addToCacheTimer, () -> featureCache.addToCache(feature));
  }

  private void configureMetrics(MetricRegistry metrics) {
    saveFeatureTimer = metrics.timer(MetricRegistry.name(DefaultFeatureService.class,
        "saveFeatureTimer"));
    updateFeatureTimer = metrics.timer(MetricRegistry.name(DefaultFeatureService.class,
        "updateFeatureTimer"));
    addToCacheTimer = metrics.timer(MetricRegistry.name(DefaultFeatureService.class,
        "addToCacheTimer"));
    loadFeaturesTimer = metrics.timer(MetricRegistry.name(DefaultFeatureService.class,
        "loadFeaturesTimer"));
    loadFeaturesCacheTimer = metrics.timer(MetricRegistry.name(DefaultFeatureService.class,
        "loadFeaturesCacheTimer"));
    loadFeaturesCacheHitMeter = metrics.meter(MetricRegistry.name(DefaultFeatureService.class,
        "loadFeaturesCacheHitMeter"));
    loadFeaturesCacheMissMeter = metrics.meter(MetricRegistry.name(DefaultFeatureService.class,
        "loadFeaturesCacheMissMeter"));
    loadFeatureByKeyTimer = metrics.timer(MetricRegistry.name(DefaultFeatureService.class,
        "loadFeatureByKeyTimer"));
    loadFeatureCacheTimer = metrics.timer(MetricRegistry.name(DefaultFeatureService.class,
        "loadFeatureCacheTimer"));
    loadFeatureCacheHitMeter = metrics.meter(MetricRegistry.name(DefaultFeatureService.class,
        "loadFeatureCacheHitMeter"));
    loadFeatureCacheMissMeter = metrics.meter(MetricRegistry.name(DefaultFeatureService.class,
        "loadFeatureCacheMissMeter"));
  }
}
