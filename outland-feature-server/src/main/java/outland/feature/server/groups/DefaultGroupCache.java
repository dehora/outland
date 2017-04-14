package outland.feature.server.groups;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.TextFormat;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Group;
import outland.feature.server.hystrix.HystrixConfiguration;
import outland.feature.server.protobuf.Protobuf3Support;
import outland.feature.server.redis.RedisCache;
import outland.feature.server.redis.RedisCacheCommand;

import static outland.feature.server.StructLog.kvp;

public class DefaultGroupCache implements GroupCache {

  private static final Logger logger = LoggerFactory.getLogger(DefaultGroupCache.class);

  private final RedisCache redisCache;
  private final HystrixConfiguration hystrixConfiguration;
  private final MetricRegistry metrics;

  @Inject
  public DefaultGroupCache(
      RedisCache redisCache,
      @Named("redisFeatureCacheHystrix") HystrixConfiguration hystrixConfiguration,
      MetricRegistry metrics
  ) {
    this.redisCache = redisCache;
    this.hystrixConfiguration = hystrixConfiguration;
    this.metrics = metrics;
  }

  @Override public String buildCacheKey(String group) {
    return "outland:groups:" + group;
  }

  @Override public Void addToCache(Group group) {
    String raw = Protobuf3Support.toJsonString(group);

    new RedisCacheCommand<>("GroupCacheSet",
        () -> redisCache.set(buildCacheKey(group.getKey()), raw),
        () -> null,
        hystrixConfiguration,
        metrics).execute();

    return null;
  }

  @Override public Optional<Group> findInCache(String cacheKey) {

    logger.info("{}", kvp("op", "findInCache", "group", cacheKey));

    @SuppressWarnings("unchecked")
    Optional<String> maybe = (Optional<String>) new RedisCacheCommand<Optional<String>>(
        "GroupCacheGet",
        () -> redisCache.get(cacheKey),
        Optional::empty,
        hystrixConfiguration,
        metrics).execute();

    if (maybe.isPresent()) {
      Group cached = GroupSupport.toGroup(maybe.get());
      logger.info("{} /group=[{}]",
          kvp("op", "findInCache", "key", cacheKey, "result", "cache_hit"),
          TextFormat.shortDebugString(cached));
      return Optional.of(cached);
    }

    logger.info("{}", kvp("op", "findInCache", "key", cacheKey, "result", "cache_miss"));

    return Optional.empty();
  }

  @Override public Void flushCache(String cacheKey) {

    logger.info("{}", kvp("op", "flushCache", "group", cacheKey));

    new RedisCacheCommand<>("GroupCacheDel",
        () -> redisCache.del(cacheKey),
        () -> null,
        hystrixConfiguration,
        metrics).execute();

    return null;
  }

  @Override public Void flushAll() {
    return redisCache.flushAll();
  }
}
