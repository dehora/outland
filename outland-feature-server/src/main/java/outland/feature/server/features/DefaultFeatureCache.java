package outland.feature.server.features;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.TextFormat;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Feature;
import outland.feature.server.hystrix.HystrixConfiguration;
import outland.feature.server.protobuf.Protobuf3Support;

import static outland.feature.server.StructLog.kvp;

public class DefaultFeatureCache implements FeatureCache {

  private static final Logger logger = LoggerFactory.getLogger(DefaultFeatureCache.class);

  private final RedisCache redisCache;
  private final HystrixConfiguration hystrixConfiguration;
  private final MetricRegistry metrics;

  @Inject
  public DefaultFeatureCache(
      RedisCache redisCache,
      @Named("redisFeatureCacheHystrix") HystrixConfiguration hystrixConfiguration,
      MetricRegistry metrics) {
    this.redisCache = redisCache;
    this.hystrixConfiguration = hystrixConfiguration;
    this.metrics = metrics;
  }

  @Override public String buildCacheKeyByFeatureKey(String group, String featureKey) {
    return "features:" + group + ":" + featureKey;
  }

  @Override public Void addToCache(Feature feature) {
    String raw = Protobuf3Support.toJsonString(feature);

    new RedisCacheCommand<>("CacheSet",
        () -> redisCache.set(feature.getId(), raw),
        () -> null,
        hystrixConfiguration,
        metrics).execute();

    new RedisCacheCommand<>("CacheSet",
        () -> redisCache.set(buildCacheKeyByFeatureKey(feature.getGroup(), feature.getKey()), raw),
        () -> null,
        hystrixConfiguration,
        metrics).execute();

    new RedisCacheCommand<>("CacheHSet",
        () -> redisCache.hset(feature.getGroup(), feature.getKey(), raw),
        () -> null,
        hystrixConfiguration,
        metrics).execute();

    return null;
  }

  @Override public Optional<Map<String, String>> getCacheSet(String group) {
    logger.info("{}", kvp("op", "getCacheSet", "key", group));

    //noinspection unchecked
    return (Optional<Map<String, String>>) new RedisCacheCommand<>("CacheHSet",
        () -> redisCache.hgetAll(group),
        Optional::empty,
        hystrixConfiguration,
        metrics).execute();
  }

  @Override public Optional<Feature> findInCache(String cacheKey) {
    //noinspection unchecked
    Optional<String> maybe = (Optional<String>) new RedisCacheCommand<Optional<String>>("CacheGet",
        () -> redisCache.get(cacheKey),
        Optional::empty,
        hystrixConfiguration,
        metrics).execute();

    if (maybe.isPresent()) {
      Feature cached = FeatureSupport.toFeature(maybe.get());
      logger.info("{} /feature=[{}]",
          kvp("op", "findInCache", "key", cacheKey, "result", "cache_hit"),
          TextFormat.shortDebugString(cached));
      return Optional.of(cached);
    }

    logger.info("{}", kvp("op", "findInCache", "key", cacheKey, "result", "cache_miss"));

    return Optional.empty();
  }

  @Override public Void flushCache(String group, String featureKey, String id) {
    logger.info("{}",
        kvp("op", "flushCache", "group", group, "feature_key", featureKey, "id", id));

    executeFlushCache(group, featureKey, id);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    executeFlushCache(group, featureKey, id);

    return null;
  }

  @Override public Void flushAll() {
    return redisCache.flushAll();
  }

  private void executeFlushCache(String appKey, String featureKey, String id) {

    // we have to drop the entire hset to avoid getting cache stuck with a partial list
    new RedisCacheCommand<>("CacheDel",
        () -> redisCache.del(appKey),
        () -> null,
        hystrixConfiguration,
        metrics).execute();

    new RedisCacheCommand<>("CacheDel",
        () -> redisCache.del(id),
        () -> null,
        hystrixConfiguration,
        metrics).execute();

    new RedisCacheCommand<>("CacheDel",
        () -> buildCacheKeyByFeatureKey(appKey, featureKey),
        () -> null,
        hystrixConfiguration,
        metrics).execute();
  }

}
