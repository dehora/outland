package outland.feature.server.redis;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static outland.feature.server.StructLog.kvp;

class RedisProviders {

  private static final Logger logger = LoggerFactory.getLogger(RedisProviders.class);
  private static final Map<String, RedisProvider> theProviders = Maps.newHashMap();

  @Inject
  public RedisProviders(RedisServersConfiguration configuration) {
    initialize(configuration);
  }

  public RedisProvider getProvider(String name) {
    return theProviders.get(name);
  }

  public Map<String, RedisProvider> getProviders() {
    return theProviders;
  }

  private void initialize(RedisServersConfiguration configuration) {
    configuration.servers.forEach(this::buildProvider);
  }

  private RedisProvider buildProvider(RedisConfiguration conf) {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(conf.maxConnections);
    RedisProvider provider =
        new RedisProvider(conf.name, new JedisPool(poolConfig, conf.host, conf.port));
    theProviders.put(conf.name, provider);
    logger.info(kvp("op", "redis_provider_init",
        "provider", provider.getName(), "conf", conf.toString()));
    return provider;
  }
}
