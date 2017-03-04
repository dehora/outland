package outland.feature.server.redis;

import com.google.common.base.MoreObjects;
import javax.inject.Inject;
import javax.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisProvider implements Provider<Jedis> {

  private static final Logger logger = LoggerFactory.getLogger(RedisProvider.class);

  private final String name;
  private final JedisPool pool;

  @Inject
  public RedisProvider(String name, JedisPool pool) {
    this.name = name;
    this.pool = pool;
  }

  @Override
  public Jedis get() {
    return pool.getResource();
  }

  public String getName() {
    return name;
  }

  public void closeSafely(Jedis jedis) {
    try {
      if (jedis != null) {
        jedis.close();
      }
    } catch (Exception e) {
      logger.error("could not close jedis {} {} ", name, e.getMessage());
    }
  }

  public void closePool() {
    try {
      pool.destroy();
    } catch (Exception e) {
      logger.error("could not destroy jedis {} {} ", name, e.getMessage());
    }
  }

  @Override public int hashCode() {
    return java.util.Objects.hash(name, pool);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RedisProvider that = (RedisProvider) o;
    return java.util.Objects.equals(name, that.name) &&
        java.util.Objects.equals(pool, that.pool);
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("pool", pool)
        .toString();
  }
}
