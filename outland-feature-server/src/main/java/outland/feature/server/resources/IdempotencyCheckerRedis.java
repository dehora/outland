package outland.feature.server.resources;

import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.server.redis.RedisProvider;
import redis.clients.jedis.Jedis;

import static outland.feature.server.StructLog.kvp;

public class IdempotencyCheckerRedis implements IdempotencyChecker {

  private static final Logger logger = LoggerFactory.getLogger(IdempotencyCheckerRedis.class);

  private static final int TTL_SECONDS = 3600 * 24;

  private final RedisProvider redisProvider;

  @Inject
  public IdempotencyCheckerRedis(Map<String, RedisProvider> providers) {
    this.redisProvider = providers.get("outland_feature_idempotency_check_redis");
  }

  public boolean seen(String idempotencyKey) {

    if (idempotencyKey == null) {
      return false;
    }

    Jedis jedis = null;

    try {
      jedis = redisProvider.get();
      String cacheKey = "outland:feature:idem_key:" + idempotencyKey;
      final Long res = jedis.setnx(cacheKey, "");
      jedis.expire(cacheKey, TTL_SECONDS);
      return res == 0;
    } catch (Exception e) {
      logger.error("{}", kvp("op", "idempotency_check",
          "key", idempotencyKey,
          "err", "[" + e.getMessage() + "]"));
    } finally {
      redisProvider.closeSafely(jedis);
    }

    return false;
  }
}
