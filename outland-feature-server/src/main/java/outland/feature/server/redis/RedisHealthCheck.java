package outland.feature.server.redis;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import static outland.feature.server.StructLog.kvp;

public class RedisHealthCheck extends HealthCheck {

  private static final Logger logger = LoggerFactory.getLogger(RedisHealthCheck.class);

  private final RedisProviders redisProviders;

  @Inject
  public RedisHealthCheck(RedisProviders redisProviders) {
    this.redisProviders = redisProviders;
  }

  @Override
  protected Result check() throws Exception {

    final Map<String, RedisProvider> providers = redisProviders.getProviders();
    for (Map.Entry<String, RedisProvider> entry : providers.entrySet()) {
      RedisProvider redisProvider = entry.getValue();
      Jedis jedis = null;
      try {
        jedis = redisProvider.get();

        final String key = "healthcheck:" + redisProvider.getName();
        final int ttl = 3600;
        final String checkString = String.format("%d", System.currentTimeMillis());
        jedis.lpush(key, checkString);
        jedis.expire(key, ttl);
        logger.info(kvp("op", "redis_health",
            "server", redisProvider.getName(),
            "key", key,
            "value", checkString,
            "ttl", "" + ttl));
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        return Result.unhealthy(kvp("redis_provider", redisProvider.getName(),
            "err", e.getMessage()));
      } finally {
        redisProvider.closeSafely(jedis);
      }
    }

    return Result.healthy(providers.keySet().toString());
  }
}
