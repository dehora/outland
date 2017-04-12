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
  private final VersionSupport versionSupport;
  private final FeatureValidator featureValidator;
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
    this.versionSupport = new VersionSupport(versionService);
    configureMetrics(metrics);
    featureValidator = new FeatureValidator();
  }

  @Override public Optional<Feature> registerFeature(Feature registering) {

    logger.info("{} /feature[{}]", kvp("op", "registerFeature"), toString(registering));

    final Feature feature = featureRegisterProcessor.prepareNewFeature(registering);
    timed(saveFeatureTimer, () -> featureStorage.createFeature(feature));
    addToCache(feature);
    return Optional.of(feature);
  }

  @Override
  public Optional<Feature> updateFeature(String group, String featureKey, Feature updates) {

    logger.info("{} /update_feature=[{}]",
        kvp("op", "updateFeature", "group", group, "feature_key", featureKey), toString(updates));

    featureValidator.validateFeatureUpdateThrowing(updates);

    final Optional<Feature> maybeFound =
        timed(loadFeatureByKeyTimer, () -> featureStorage.loadFeatureByKey(group, featureKey));

    if (!maybeFound.isPresent()) {
      return maybeFound;
    }

    final Feature existing = maybeFound.get();
    final Feature prepared = featureUpdateProcessor.prepareUpdateFeature(existing, updates);
    featureValidator.validateFeatureUpdateThrowing(prepared);
    timed(updateFeatureTimer, () -> featureStorage.updateFeature(prepared, existing.getVersion()));
    addToCache(prepared);
    return Optional.of(prepared);
  }

  @Override public Optional<Feature> loadFeatureByKey(String group, String featureKey) {

    logger.info("{}", kvp("op", "loadFeatureByKey", "group", group, "feature_key", featureKey));

    Optional<Feature> cached = timed(loadFeatureCacheTimer,
        () -> featureCache.findInCache(featureCache.buildCacheKeyByFeatureKey(group, featureKey)));

    if (cached.isPresent()) {
      loadFeatureCacheHitMeter.mark();
      return cached;
    }

    loadFeatureCacheMissMeter.mark();

    final Optional<Feature> maybe =
        timed(loadFeatureByKeyTimer, () -> featureStorage.loadFeatureByKey(group, featureKey));

    if (maybe.isPresent()) {
      /*
      drive a full read; this is to avoid the case where a single item is pulled from storage
      added to an empty set, but there are other items in storage such that the single cached
      result will be served, leaving behind the other features.
      */
      addAllToCache(timed(loadFeaturesTimer, () -> featureStorage.loadFeatures(group)));
    }

    return maybe;
  }

  @Override public FeatureCollection loadFeatures(String group) {

    logger.info("{}", kvp("op", "loadFeatures", "group", group));

    final FeatureCollection.Builder builder = FeatureCollection.newBuilder();
    builder.setType("feature.list");
    builder.setGroup(group);

    final Optional<Map<String, String>> cacheSet = timed(loadFeaturesCacheTimer,
        () -> featureCache.getCacheSet(group));

    if (cacheSet.isPresent()) {
      loadFeaturesCacheHitMeter.mark();
      final ArrayList<Feature> features = Lists.newArrayList();
      final Set<Map.Entry<String, String>> entries = cacheSet.get().entrySet();
      for (Map.Entry<String, String> entry : entries) {
        features.add(FeatureSupport.toFeature(entry.getValue()));
      }

      features.sort(byUpdatedOrdering);
      builder.addAllItems(features);
      return builder.build();
    }

    loadFeaturesCacheMissMeter.mark();

    final List<Feature> features =
        timed(loadFeaturesTimer, () -> featureStorage.loadFeatures(group));
    features.sort(byUpdatedOrdering);
    addAllToCache(features);
    builder.addAllItems(features);
    return builder.build();
  }

  @Override public FeatureCollection loadFeaturesChangedSince(String group, OffsetDateTime since) {

    logger.info("{}", kvp("op", "loadFeaturesSince", "group", group, "since", since));

    final List<Feature> sinceList = Lists.newArrayList();
    final List<Feature> list = loadFeatures(group).getItemsList();
    for (Feature feature : list) {
      final OffsetDateTime time = TimeSupport.asOffsetDateTime(feature.getUpdated());
      if (time.isAfter(since)) {
        sinceList.add(feature);
      }
    }

    sinceList.sort(byUpdatedOrdering);
    return FeatureCollection.newBuilder()
        .addAllItems(sinceList)
        .setGroup(group)
        .setType("feature.list")
        .build();
  }

  @Override public Feature add(Feature feature, NamespaceFeature namespaceFeature) {

    logger.info("{} {}", kvp("op", "addNamespaceFeature"),
        TextFormat.shortDebugString(namespaceFeature));

    final FeatureValidator featureValidator = new FeatureValidator();
    featureValidator.validateFeatureDataNewCandidateThrowing(feature, namespaceFeature);

    final FeatureUpdateProcessor processor = new FeatureUpdateProcessor(versionService);
    final List<NamespaceFeature> namespaceFeatures =
        processor.buildMergedNamespaceFeatures(feature, namespaceFeature);

    final NamespaceFeatureCollection.Builder builder = NamespaceFeatureCollection.newBuilder();
    builder.setType("namespace.feature.collection").addAllItems(namespaceFeatures);

    final Feature.Builder wipBuilder = feature.toBuilder()
        .clearNamespaces()
        .setNamespaces(builder);

    final FeatureVersion foundVersion = feature.getVersion();

    final String now = TimeSupport.asString(OffsetDateTime.now());
    wipBuilder.setUpdated(now);

    versionSupport.applyVersion(feature, wipBuilder);

    final Feature updated = wipBuilder.build();

    featureValidator.validateFeatureRegistrationThrowing(updated);
    timed(updateFeatureTimer, () -> featureStorage.updateFeature(updated, foundVersion));
    addToCache(updated);
    return updated;
  }

  @Override public Feature removeNamespaceFeature(String group, String featureKey,
      String namespace) {
    final Optional<Feature> maybe = loadFeatureByKey(group, featureKey);

    if (!maybe.isPresent()) {
      return null;
    }

    final Feature feature = maybe.get();
    final NamespaceFeatureCollection namespaced = feature.getNamespaces();
    final List<NamespaceFeature> namespaceFeatures = Lists.newArrayList(namespaced.getItemsList());

    NamespaceFeature namespaceFeature = null;
    final Iterator<NamespaceFeature> iterator = namespaceFeatures.iterator();
    while (iterator.hasNext()) {
      NamespaceFeature next = iterator.next();
      if (next.getNamespace().equals(namespace)) {
        namespaceFeature = next;
        iterator.remove();
        break;
      }
    }

    if (namespaceFeature == null) {
      return null;
    }

    final NamespaceFeatureCollection.Builder nfcBuilder = NamespaceFeatureCollection.newBuilder()
        .setType("namespace.feature.collection")
        .addAllItems(namespaceFeatures);

    final Feature.Builder wipBuilder = feature.toBuilder()
        .clearNamespaces()
        .setNamespaces(nfcBuilder);

    final FeatureVersion foundVersion = feature.getVersion();
    versionSupport.applyVersion(feature, wipBuilder);
    wipBuilder.setUpdated(TimeSupport.asString(OffsetDateTime.now()));

    final Feature updated = wipBuilder.build();
    final FeatureValidator featureValidator = new FeatureValidator();
    featureValidator.validateFeatureRegistrationThrowing(updated);

    timed(updateFeatureTimer, () -> featureStorage.updateFeature(updated, foundVersion));
    addToCache(updated);
    return updated;
  }

  private void addAllToCache(List<Feature> features) {
    for (Feature feature : features) {
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

  private String toString(Feature feature) {
    return TextFormat.shortDebugString(feature);
  }
}
