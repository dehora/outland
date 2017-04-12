package outland.feature.server.features;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.protobuf.TextFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
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
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.NamespaceFeatureCollection;

import static outland.feature.server.StructLog.kvp;

class DefaultFeatureService implements FeatureService, MetricsTimer {

  private static final Logger logger = LoggerFactory.getLogger(DefaultFeatureService.class);

  private static final Ordering<Feature> byUpdatedOrdering = new Ordering<Feature>() {
    public int compare(Feature left, Feature right) {
      OffsetDateTime dtRight = TimeSupport.asOffsetDateTime(right.getUpdated());
      OffsetDateTime dtLeft = TimeSupport.asOffsetDateTime(left.getUpdated());
      return dtRight.compareTo(dtLeft);
    }
  };

  private final FeatureStorage featureStorage;
  private final FeatureCache featureCache;
  private final VersionService versionService;
  private final FeatureRegisterProcessor featureRegisterProcessor;
  private final FeatureUpdateProcessor featureUpdateProcessor;

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
    this.featureRegisterProcessor = new FeatureRegisterProcessor(versionService);
    this.featureUpdateProcessor = new FeatureUpdateProcessor(versionService);
    configureMetrics(metrics);
  }

  @Override public Optional<Feature> registerFeature(Feature registering) {

    logger.info("{} /feature[{}]", kvp("op", "registerFeature"), toString(registering));

    Feature feature = featureRegisterProcessor.prepareNewFeature(registering);

    timed(saveFeatureTimer, () -> featureStorage.createFeature(feature));

    logger.info("{} /feature=[{}]",
        kvp("op", "registerFeature", "result", "ok"), toString(feature));

    addToCache(feature);
    return Optional.of(feature);
  }

  private String toString(Feature feature) {
    return TextFormat.shortDebugString(feature);
  }

  @Override
  public Optional<Feature> updateFeature(String group, String featureKey, Feature updates) {

    logger.info("{} /update_feature=[{}]",
        kvp("op", "updateFeature", "group", group, "feature_key", featureKey), toString(updates));

    FeatureValidator featureValidator = new FeatureValidator();

    // catch bad input before merging
    featureValidator.validateFeatureUpdateThrowing(updates);

    Optional<Feature> maybeFound =
        timed(loadFeatureByKeyTimer, () -> featureStorage.loadFeatureByKey(group, featureKey));

    if (!maybeFound.isPresent()) {
      logger.info("updateFeature not_found {} {}", group, updates);
      return maybeFound;
    }

    Feature found = maybeFound.get();

    logger.info("{} /found_feature=[{}]",
        kvp("op", "updateFeature", "group", group, "feature_key", featureKey), toString(found));

    Feature updated = featureUpdateProcessor.prepareUpdateFeature(found, updates);

    final FeatureVersion foundVersion = found.getVersion();
    timed(updateFeatureTimer, () -> featureStorage.updateFeature(updated, foundVersion));

    logger.info("{} /updated_feature=[{}]",
        kvp("op", "updateFeature", "group", group, "feature_key", featureKey), toString(updated));

    addToCache(updated);
    return Optional.of(updated);
  }

  @Override public Optional<Feature> loadFeatureByKey(String group, String featureKey) {

    logger.info("{}", kvp("op", "loadFeatureByKey", "group", group, "feature_key", featureKey));

    Optional<Feature> cached = timed(loadFeatureCacheTimer,
        () -> featureCache.findInCache(featureCache.buildCacheKeyByFeatureKey(group, featureKey)));

    if (cached.isPresent()) {
      loadFeatureCacheHitMeter.mark();
      logger.info("{} /feature=[{}]",
          kvp("op", "loadFeatureByKey", "group", group, "feature_key", featureKey,
          "result", "cache_hit"), toString(cached.get()));
      return cached;
    }

    loadFeatureCacheMissMeter.mark();

    Optional<Feature> feature =
        timed(loadFeatureByKeyTimer, () -> featureStorage.loadFeatureByKey(group, featureKey));

    if (feature.isPresent()) {
      /*
      drive a full load; this is to avoid the case where a single item is pulled from storage
      added to an empty set, but there are other items in storage such that the single cached
      result will be served, leaving behind the other features.
      */
      logger.info("{}", kvp("op", "loadFeatureByKey", "group", group,
          "feature_key", featureKey, "msg", "force_cache_reload"));

      addAllToCache(group, timed(loadFeaturesTimer, () -> featureStorage.loadFeatures(group)));

      logger.info("{} /feature=[{}]", kvp("op", "loadFeatureByKey",
          "group", group, "feature_key", featureKey, "result", "cache_miss",
          "msg", "add_to_cache"), toString(feature.get()));
    }

    logger.info("{}", kvp("op", "loadFeatureByKey",
        "group", group, "result", feature.isPresent() ? "ok" : "not_found"));

    // todo: signal 404 if empty
    return feature;
  }

  @Override public FeatureCollection loadFeatures(String group) {

    logger.info("{}", kvp("op", "loadFeatures", "group", group));

    FeatureCollection.Builder builder = FeatureCollection.newBuilder();
    builder.setType("feature.list");
    builder.setGroup(group);

    Optional<Map<String, String>> cacheSet = timed(loadFeaturesCacheTimer,
        () -> featureCache.getCacheSet(group));

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
          "group", group,
          "result", "cache_hit"),
          toString(featureCollection));

      return featureCollection;
    }

    loadFeaturesCacheMissMeter.mark();

    List<Feature> features = timed(loadFeaturesTimer, () -> featureStorage.loadFeatures(group));
    features.sort(byUpdatedOrdering);
    builder.addAllItems(features);
    FeatureCollection featureCollection = builder.build();

    addAllToCache(group, features);

    logger.info("{} /features=[{}]", kvp("op", "loadFeatures",
        "group", group,
        "result", "ok"),
        toString(featureCollection));

    return featureCollection;
  }

  private String toString(FeatureCollection featureCollection) {
    return TextFormat.shortDebugString(featureCollection);
  }

  @Override public FeatureCollection loadFeaturesChangedSince(String group, OffsetDateTime since) {

    logger.info("{}", kvp("op", "loadFeaturesSince", "group", group, "since", since));

    FeatureCollection collection = loadFeatures(group);

    List<Feature> sinceList = Lists.newArrayList();

    List<Feature> list = collection.getItemsList();
    for (Feature feature : list) {
      OffsetDateTime time = TimeSupport.asOffsetDateTime(feature.getUpdated());
      if (time.isAfter(since)) {
        sinceList.add(feature);
      }
    }

    sinceList.sort(byUpdatedOrdering);

    FeatureCollection.Builder builder = FeatureCollection.newBuilder();
    final FeatureCollection featureCollection = builder
        .addAllItems(sinceList)
        .setGroup(group)
        .setType("feature.list")
        .build();

    logger.info("{} /features=[{}]", kvp("op", "loadFeaturesSince",
            "group", group, "since", since, "result", "ok"), toString(featureCollection));

    return featureCollection;
  }

  @Override public Feature add(Feature feature, NamespaceFeature namespaceFeature) {

    FeatureValidator featureValidator = new FeatureValidator();

    featureValidator.validateFeatureDataNewCandidateThrowing(feature, namespaceFeature);

    FeatureUpdateProcessor processor = new FeatureUpdateProcessor(versionService);
    final List<NamespaceFeature> namespaceFeatures =
        processor.buildMergedNamespaceFeatures(feature, namespaceFeature);

    final NamespaceFeatureCollection.Builder builder = NamespaceFeatureCollection.newBuilder();
    builder.setType("namespace.feature.collection").addAllItems(namespaceFeatures);

    final Feature.Builder wipBuilder = feature.toBuilder()
        .clearNamespaces()
        .setNamespaces(builder);


    final FeatureVersion foundVersion = feature.getVersion();

    String now = TimeSupport.asString(OffsetDateTime.now());
    wipBuilder.setUpdated(now);

    applyVersion(feature, wipBuilder);

    Feature updated = wipBuilder.build();


    featureValidator.validateFeatureRegistrationThrowing(updated);
    timed(updateFeatureTimer, () -> featureStorage.updateFeature(updated, foundVersion));
    logger.info("{} /updated_feature_namespace=[{}]", kvp("op", "updateFeatureNamespace",
        "group", feature.getGroup(), "feature_key", feature.getKey(),
        "namespace", namespaceFeature.getNamespace()), toString(updated));

    addToCache(updated);
    return updated;
  }

  @Override public Feature removeNamespaceFeature(String group, String featureKey,
      String namespace) {
    final Optional<Feature> maybe = loadFeatureByKey(group, featureKey);

    if(! maybe.isPresent()) {
      return  null;
    }

    final Feature feature = maybe.get();

    final NamespaceFeatureCollection namespaced = feature.getNamespaces();

    final ArrayList<NamespaceFeature> namespaceFeatures =
        Lists.newArrayList(namespaced.getItemsList());

    NamespaceFeature namespaceFeature = null;
    final Iterator<NamespaceFeature> iterator = namespaceFeatures.iterator();
    while (iterator.hasNext()) {
      NamespaceFeature next = iterator.next();
      if(next.getNamespace().equals(namespace)) {
        namespaceFeature = next;
        iterator.remove();
        break;
      }
    }

    if(namespaceFeature == null) {
      return null;
    }

    final NamespaceFeatureCollection.Builder nfcBuilder = NamespaceFeatureCollection.newBuilder()
        .setType("namespace.feature.collection")
        .addAllItems(namespaceFeatures);

    final Feature.Builder wipBuilder = feature.toBuilder()
        .clearNamespaces()
        .setNamespaces(nfcBuilder);

    final FeatureVersion foundVersion = feature.getVersion();
    applyVersion(feature, wipBuilder);
    wipBuilder.setUpdated(TimeSupport.asString(OffsetDateTime.now()));

    final Feature updated = wipBuilder.build();

    FeatureValidator featureValidator = new FeatureValidator();
    featureValidator.validateFeatureRegistrationThrowing(updated);

    timed(updateFeatureTimer, () -> featureStorage.updateFeature(updated, foundVersion));

    logger.info("{} /updated_feature_namespace=[{}]",
        kvp("op", "removeNamespaceFeature",
            "group", feature.getGroup(),
            "feature_key", feature.getKey(),
            "namespace", namespaceFeature.getNamespace()
        ), toString(updated));

    addToCache(updated);
    return updated;
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

    builder.setVersion(buildVersion(next));
  }

  private FeatureVersion.Builder buildVersion(VersionService.HybridLogicalTimestamp next) {
    return FeatureVersion.newBuilder()
        .setType("hlcver")
        .setCounter(next.counter())
        .setTimestamp(next.logicalTime())
        .setId(next.id());
  }

  private void addAllToCache(String group, List<Feature> features) {
    for (Feature feature : features) {

      logger.info("{} /feature=[{}]", kvp("op", "loadFeatures",
          "group", group, "result", "cache_miss", "msg", "add_to_cache"), toString(feature));

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
