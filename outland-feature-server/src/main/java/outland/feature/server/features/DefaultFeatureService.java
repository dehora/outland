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
import outland.feature.proto.FeatureOwner;
import outland.feature.proto.FeatureVersion;
import outland.feature.proto.OptionType;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

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

    String now = FeatureService.asString(OffsetDateTime.now());
    Optional<Feature> maybeFound =
        timed(loadFeatureByKeyTimer, () -> featureStorage.loadFeatureByKey(appKey, featureKey));

    if (!maybeFound.isPresent()) {
      logger.info("updateFeature not_found {} {}", appKey, updates);
      return maybeFound;
    }

    Feature found = maybeFound.get();

    logger.info("{} /found_feature=[{}]",
        kvp("op", "updateFeature", "appkey", appKey, "feature_key", featureKey),
        TextFormat.shortDebugString(found));

    Feature.Builder builder = found.toBuilder()
        .mergeFrom(updates)
        .setUpdated(now);

    // can't change some values in update
    builder.setType("feature");
    builder.setOptionType(found.getOptionType());
    builder.setCreated(found.getCreated());
    builder.setId(found.getId());
    builder.setAppkey(found.getAppkey());
    builder.setKey(found.getKey());

    applyVersion(updates, builder);

    if (builder.getOptionType().equals(OptionType.bool) && builder.getOptionsCount() != 0) {
      List<FeatureOption> options = applyOptionsUpdate(updates, found);
      builder.clearOptions();
      builder.addAllOptions(options);
    }

    if(updates.hasOwner()) {
      builder.clearOwner();
      applyOwnerUpdate(updates, builder);
    }

    // a value other than none indicates the client sent something
    if (!updates.getState().equals(Feature.State.none)) {
      builder.setState(updates.getState());
    }

    Feature updated = builder.build();

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
    if(updated.getOptionsCount() == 0) {
      results.addAll(found.getOptionsList());
      return results;
    }

    final List<FeatureOption> updatedOptionsList = updated.getOptionsList();
    if (updatedOptionsList.size() != 2) {
      throw new ServiceException(Problem.clientProblem("options_wrong_number",
          "please submit both bool options for update.", 422));
    }

    for (FeatureOption updateOption : updatedOptionsList) {
      final String updateId = updateOption.getId();

      final List<FeatureOption> foundOptionsList = found.getOptionsList();

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

    int sum = 0;
    for (FeatureOption result : results) {
      validateOption(result);
      sum += result.getWeight();
    }

    if (sum != 10_000) {
      throw new ServiceException(
          Problem.clientProblem("weights_wrong_total", "option weights must sum to 10000",
              422));
    }

    return results;
  }

  private void applyOptionsRegister(Feature feature, Feature.Builder builder) {

    if (feature.getOptionType().equals(OptionType.flag)) {
      // flags don't have weighted options
      return;
    }

    if (feature.getOptionType().equals(OptionType.bool)) {
      if (feature.getOptionsCount() != 0) {

        int sum = 0;

        final List<FeatureOption> options = feature.getOptionsList();

        for (FeatureOption option : options) {

          validateOption(option);

          final FeatureOption.Builder optionBuilder = FeatureOption.newBuilder().mergeFrom(option);
          optionBuilder.setType("option");
          optionBuilder.setId("opt_" + Ulid.random());
          optionBuilder.setOptionType(OptionType.bool);
          builder.addOptions(optionBuilder);

          sum += option.getWeight();
        }

        if (sum != 10_000) {
          throw new ServiceException(Problem.clientProblem("weights_wrong_total",
              "option weights must sum to 10000", 422));
        }
      } else {
        builder.addOptions(FeatureOption.newBuilder()
            .setType("option")
            .setId("opt_" + Ulid.random())
            .setName("false")
            .setValue("false")
            .setOptionType(OptionType.bool)
            .setWeight(5_000));

        builder.addOptions(FeatureOption.newBuilder()
            .setType("option")
            .setId("opt_" + Ulid.random())
            .setName("true")
            .setValue("true")
            .setOptionType(OptionType.bool)
            .setWeight(5_000));
      }
    }
  }

  private void applyOwnerUpdate(Feature registering, Feature.Builder builder) {
    applyOwnerRegister(registering, builder);
  }

  private void applyOwnerRegister(Feature registering, Feature.Builder builder) {
    final FeatureOwner owner = registering.getOwner();

    if(Strings.isNullOrEmpty(owner.getUsername()) && Strings.isNullOrEmpty(owner.getEmail())) {
      throw new ServiceException(Problem.clientProblem("owner_incomplete",
          "owner has no email or username", 422));
    }

    final FeatureOwner.Builder ownerBuilder = owner.toBuilder();
    ownerBuilder.setType("featureowner");
    builder.setOwner(ownerBuilder.buildPartial());
  }

  private void validateOption(FeatureOption option) {
    if (option.getWeight() > 10_000 || option.getWeight() < 0) {
      throw new ServiceException(Problem.clientProblem("weights_out_of_bounds",
          "option weights must be between 0 and 10000", 422));
    }

    if (!"true".equalsIgnoreCase(option.getValue())
        &&
        !"false".equalsIgnoreCase(option.getValue())) {
      throw new ServiceException(Problem.clientProblem("bad_option_value",
          "option values must be true or false", 422));
    }

    if (!"true".equalsIgnoreCase(option.getName())
        &&
        !"false".equalsIgnoreCase(option.getName())) {
      throw new ServiceException(Problem.clientProblem("bad_option_name",
          "option names must be true or false", 422));
    }
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
