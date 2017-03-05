package outland.feature.server.features;

import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.server.redis.RedisProvider;
import redis.clients.jedis.Jedis;

import static outland.feature.server.StructLog.kvp;

public class RedisCache {

  private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

  private final RedisProvider redisProvider;

  @Inject
  public RedisCache(Map<String, RedisProvider> providerMap) {
    this.redisProvider = providerMap.get("outland_feature_cache_redis");
  }

  Void del(String key) {
    Jedis jedis = null;
    try {
      jedis = redisProvider.get();
      jedis.del(key);
    } catch (Exception e) {
      logger.error("{}", kvp("op", "del", "key", key, "err", "[" + e.getMessage() + "]"), e);
    } finally {
      redisProvider.closeSafely(jedis);
    }

    return null;
  }

  Optional<Map<String, String>> hgetAll(String cacheKey) {
    Jedis jedis = null;
    try {
      jedis = redisProvider.get();
      final Map<String, String> result = jedis.hgetAll(cacheKey);
      if (result.size() == 0) {
        // jedis returns an empty map if the key's not present
        return Optional.empty();
      } else {
        return Optional.of(result);
      }
    } finally {
      redisProvider.closeSafely(jedis);
    }
  }

  Optional<String> get(String key) {
    Jedis jedis = null;
    try {
      jedis = redisProvider.get();
      final String data = jedis.get(key);
      return Optional.ofNullable(data);
    } catch (Exception e) {
      logger.error("{}", kvp("op", "get", "key", key, "err", "[" + e.getMessage() + "]"), e);
      return Optional.empty();
    } finally {
      redisProvider.closeSafely(jedis);
    }
  }

  Optional<String> set(String key, String data) {
    Jedis jedis = null;
    try {
      jedis = redisProvider.get();
      final String result = jedis.set(key, data);
      return Optional.ofNullable(result);
    } catch (Exception e) {
      logger.error("{}", kvp("op", "set",
          "key", key,
          "err", "[" + e.getMessage() + "]"), e);
      return Optional.empty();
    } finally {
      redisProvider.closeSafely(jedis);
    }
  }

  Void hset(String cacheKey, String fieldKey, String value) {
    Jedis jedis = null;
    try {
      jedis = redisProvider.get();
      jedis.hset(cacheKey, fieldKey, value);
    } catch (Exception e) {
      logger.error("{}", kvp("op", "hset",
          "key", cacheKey,
          "field", fieldKey,
          "data", "[" + value + "]",
          "err", "[" + e.getMessage() + "]"), e);
    } finally {
      redisProvider.closeSafely(jedis);
    }
    return null;
  }

  public Void flushAll() {
    Jedis jedis = null;
    try {
      jedis = redisProvider.get();
      jedis.flushAll();
    } catch (Exception e) {
      logger.error("{}", kvp("op", "flushAll", "err", "[" + e.getMessage() + "]"), e);
    } finally {
      redisProvider.closeSafely(jedis);
    }

    return null;
  }
}
