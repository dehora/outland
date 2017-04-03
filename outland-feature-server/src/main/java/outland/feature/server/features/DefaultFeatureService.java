package outland.feature.server.features;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
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
import outland.feature.proto.FeatureOption;
import outland.feature.proto.FeatureVersion;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.Owner;

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
  public static final int DEFAULT_MAXWEIGHT = 10_000;

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

    FeatureValidator featureValidator = new FeatureValidator();

    // catch bad input before merging
    featureValidator.validateFeatureRegistrationThrowing(registering);

    OffsetDateTime now = OffsetDateTime.now();
    String id = "feat_" + Ulid.random(now.toInstant().toEpochMilli());
    String created = FeatureService.asString(now);

    Feature.Builder builder = registering.toBuilder();
    builder.setType("feature");
    builder.setId(id);
    builder.setCreated(created);
    builder.setUpdated(builder.getCreated());
    builder.setState(Feature.State.off); // always disabled on registerFeature

    applyVersion(registering, builder);
    builder.clearOptions();
    applyOptionsRegister(registering, builder);

    builder.clearOwner();
    applyOwnerRegister(registering, builder);

    Feature feature = builder.build();

    timed(saveFeatureTimer, () -> featureStorage.saveFeature(feature));

    logger.info("{} /feature=[{}]", kvp("op", "registerFeature", "result", "ok"),
        TextFormat.shortDebugString(feature));

    addToCache(feature);
    return Optional.of(feature);
  }

  @Override
  public Optional<Feature> updateFeature(String appKey, String featureKey, Feature updates) {

    logger.info("{} /update_feature=[{}]",
        kvp("op", "updateFeature", "appkey", appKey, "feature_key", featureKey),
        TextFormat.shortDebugString(updates));

    FeatureValidator featureValidator = new FeatureValidator();

    // catch bad input before merging
    featureValidator.validateFeatureUpdateThrowing(updates);

    String now = FeatureService.asString(OffsetDateTime.now());
    Optional<Feature> maybeFound =
        timed(loadFeatureByKeyTimer, () -> featureStorage.loadFeatureByKey(appKey, featureKey));

    if (!maybeFound.isPresent()) {
      logger.info("updateFeature not_found {} {}", appKey, updates);
      return maybeFound;
    }

    Feature found = maybeFound.get();

    featureValidator.validateOptionIds(found.getOptions(), updates.getOptions());

    logger.info("{} /found_feature=[{}]",
        kvp("op", "updateFeature", "appkey", appKey, "feature_key", featureKey),
        TextFormat.shortDebugString(found));

    Feature.Builder wipBuilder = found.toBuilder()
        .mergeFrom(updates)
        .setUpdated(now);


    // can't change some values in update
    wipBuilder.setType("feature");
    wipBuilder.setCreated(found.getCreated());
    wipBuilder.setId(found.getId());
    wipBuilder.setAppkey(found.getAppkey());
    wipBuilder.setKey(found.getKey());

    applyVersion(updates, wipBuilder);

    // process options if we received some
    if (updates.getOptions().getOption().equals(OptionType.bool) && updates.getOptions().getItemsCount() != 0) {

      // clear out options, we can rebuild them
      wipBuilder.clearOptions();
      final OptionCollection.Builder wipOptionsBuilder = OptionCollection.newBuilder();

      List<FeatureOption> options = applyOptionsUpdate(updates, found);
      wipOptionsBuilder.addAllItems(options);

      // can't change some values in update
      wipOptionsBuilder.setOption(found.getOptions().getOption());
      wipOptionsBuilder.setType("options.collection");
      wipOptionsBuilder.setMaxweight(DEFAULT_MAXWEIGHT);

      wipBuilder.setOptions(wipOptionsBuilder);
    }

    if(updates.hasOwner()) {
      Owner foundOwner = found.getOwner();
      wipBuilder.clearOwner();
      applyOwnerUpdate(updates, foundOwner, wipBuilder);
    }

    // a value other than none indicates the client sent something
    if (!updates.getState().equals(Feature.State.none)) {
      wipBuilder.setState(updates.getState());
    }

    Feature updated = wipBuilder.build();

    // post check everything
    featureValidator.validateFeatureRegistrationThrowing(updated);

    timed(updateFeatureTimer, () -> featureStorage.updateFeature(updated));

    logger.info("{} /updated_feature=[{}]",
        kvp("op", "updateFeature", "appkey", appKey, "feature_key", featureKey),
        TextFormat.shortDebugString(updated));

    addToCache(updated);
    return Optional.of(updated);
  }

  @Override public Optional<Feature> loadFeatureByKey(String appKey, String featureKey) {

    logger.info("{}", kvp("op", "loadFeatureByKey", "appkey", appKey, "feature_key", featureKey));

    Optional<Feature> cached = timed(loadFeatureCacheTimer,
        () -> featureCache.findInCache(featureCache.buildCacheKeyByFeatureKey(appKey, featureKey)));

    if (cached.isPresent()) {
      loadFeatureCacheHitMeter.mark();
      logger.info("{} /feature=[{}]", kvp("op", "loadFeatureByKey",
          "appkey", appKey,
          "feature_key", featureKey,
          "result", "cache_hit"),
          TextFormat.shortDebugString(cached.get()));
      return cached;
    }

    loadFeatureCacheMissMeter.mark();

    Optional<Feature> feature =
        timed(loadFeatureByKeyTimer, () -> featureStorage.loadFeatureByKey(appKey, featureKey));

    if (feature.isPresent()) {
      /*
      drive a full load; this is to avoid the case where a single item is pulled from storage
      added to an empty set, but there are other items in storage such that the single cached
      result will be served, leaving behind the other features.
      */
      logger.info("{}", kvp(
          "op", "loadFeatureByKey",
          "appkey", appKey,
          "feature_key", featureKey,
          "msg", "force_cache_reload"
      ));
      addAllToCache(appKey, timed(loadFeaturesTimer, () -> featureStorage.loadFeatures(appKey)));
      logger.info("{} /feature=[{}]", kvp("op", "loadFeatureByKey",
          "appkey", appKey,
          "feature_key", featureKey,
          "result", "cache_miss",
          "msg", "add_to_cache"),
          TextFormat.shortDebugString(feature.get()));
    }

    logger.info("{}", kvp("op", "loadFeatureByKey",
        "appkey", appKey,
        "result", feature.isPresent() ? "ok" : "not_found"));

    // todo: signal 404 if empty
    return feature;
  }

  @Override public FeatureCollection loadFeatures(String appKey) {

    logger.info("{}", kvp("op", "loadFeatures", "appkey", appKey));

    FeatureCollection.Builder builder = FeatureCollection.newBuilder();
    builder.setType("feature.list");
    builder.setAppkey(appKey);

    Optional<Map<String, String>> cacheSet = timed(loadFeaturesCacheTimer,
        () -> featureCache.getCacheSet(appKey));

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
          "appkey", appKey,
          "result", "cache_hit"),
          TextFormat.shortDebugString(featureCollection));

      return featureCollection;
    }

    loadFeaturesCacheMissMeter.mark();

    List<Feature> features = timed(loadFeaturesTimer, () -> featureStorage.loadFeatures(appKey));
    features.sort(byUpdatedOrdering);
    builder.addAllItems(features);
    FeatureCollection featureCollection = builder.build();

    addAllToCache(appKey, features);

    logger.info("{} /features=[{}]", kvp("op", "loadFeatures",
        "appkey", appKey,
        "result", "ok"),
        TextFormat.shortDebugString(featureCollection));

    return featureCollection;
  }

  @Override public FeatureCollection loadFeaturesChangedSince(String appKey, OffsetDateTime since) {

     logger.info("{}", kvp("op", "loadFeaturesSince", "appkey", appKey, "since", since));

    FeatureCollection collection = loadFeatures(appKey);

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
        .setAppkey(appKey)
        .setType("feature.list")
        .build();

    logger.info("{} /features=[{}]",
        kvp("op", "loadFeaturesSince",
            "appkey", appKey,
            "since", since,
            "result", "ok"),
        TextFormat.shortDebugString(featureCollection));

    return featureCollection;
  }

  private List<FeatureOption> applyOptionsUpdate(Feature updated, Feature found) {

    final ArrayList<FeatureOption> results = Lists.newArrayList();

    // nothing in the update, return what we have
    if(updated.getOptions().getItemsCount() == 0) {
      results.addAll(found.getOptions().getItemsList());
      return results;
    }

    final List<FeatureOption> updatedOptionsList = updated.getOptions().getItemsList();
    final List<FeatureOption> foundOptionsList = found.getOptions().getItemsList();

    for (FeatureOption updateOption : updatedOptionsList) {
      final String updateId = updateOption.getId();

      for (FeatureOption foundOption : foundOptionsList) {
        final FeatureOption.Builder builder = foundOption.toBuilder();
        if (foundOption.getId().equals(updateId)) {
          // weight is the only field we change
          builder.setWeight(updateOption.getWeight());
          results.add(builder.build());
          break;
        }
      }
    }

    return results;
  }

  private void applyOptionsRegister(Feature feature, Feature.Builder builder) {

    if (feature.getOptions().getOption().equals(OptionType.flag)) {
      // flags don't have weighted options
      return;
    }

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder();
    collectionBuilder.setMaxweight(DEFAULT_MAXWEIGHT);
    collectionBuilder.setType("options.collection");
    collectionBuilder.setOption(feature.getOptions().getOption());

    if (feature.getOptions().getOption().equals(OptionType.bool)) {
      if (feature.getOptions().getItemsCount() != 0) {

        final List<FeatureOption> options = feature.getOptions().getItemsList();

        for (FeatureOption option : options) {
          final FeatureOption.Builder optionBuilder = FeatureOption.newBuilder().mergeFrom(option);
          optionBuilder.setType("option");
          optionBuilder.setId("opt_" + Ulid.random());
          optionBuilder.setOption(OptionType.bool);
          collectionBuilder.addItems(optionBuilder);
        }

        builder.setOptions(collectionBuilder);


      } else {

        collectionBuilder.addItems(FeatureOption.newBuilder()
            .setType("option")
            .setId("opt_" + Ulid.random())
            .setName("false")
            .setValue("false")
            .setOption(OptionType.bool)
            .setWeight(5_000));

        collectionBuilder.addItems(FeatureOption.newBuilder()
            .setType("option")
            .setId("opt_" + Ulid.random())
            .setName("true")
            .setValue("true")
            .setOption(OptionType.bool)
            .setWeight(5_000));

        builder.setOptions(collectionBuilder);
      }
    }
  }

  private void applyOwnerUpdate(Feature updates, Owner foundOwner, Feature.Builder builder) {
    final Owner updateOwner = updates.getOwner();
    new FeatureValidator().validateOwner(updateOwner);

    if ((!Strings.isNullOrEmpty(updateOwner.getEmail())
        && !updateOwner.getEmail().equals(foundOwner.getEmail()))
        &&
        (!Strings.isNullOrEmpty(updateOwner.getUsername())
            && !updateOwner.getUsername().equals(foundOwner.getEmail()))) {
      // treat as new owner
      final Owner.Builder replacementOwnerBuilder = updateOwner.toBuilder();
      replacementOwnerBuilder.setType("featureowner");
      replacementOwnerBuilder.setId("own_" + Ulid.random());
      builder.setOwner(replacementOwnerBuilder.buildPartial());
    } else {

      final Owner.Builder wipOwnerBuilder = foundOwner.toBuilder().mergeFrom(updateOwner);
      // some fields can't be changed
      wipOwnerBuilder.setType("featureowner");
      wipOwnerBuilder.setId(foundOwner.getId());

      builder.setOwner(wipOwnerBuilder.buildPartial());
    }
  }

  private void applyOwnerRegister(Feature registering, Feature.Builder builder) {
    final Owner owner = registering.getOwner();
    final Owner.Builder ownerBuilder = owner.toBuilder();
    ownerBuilder.setType("featureowner");
    ownerBuilder.setId("own_" + Ulid.random());
    builder.setOwner(ownerBuilder.buildPartial());
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
        .setType("hlcver")
        .setCounter(next.counter())
        .setTimestamp(next.logicalTime())
        .setId(next.id()));
  }

  private void addAllToCache(String appKey, List<Feature> features) {
    for (Feature feature : features) {

      logger.info("{} /feature=[{}]", kvp("op", "loadFeatures",
          "appkey", appKey,
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
