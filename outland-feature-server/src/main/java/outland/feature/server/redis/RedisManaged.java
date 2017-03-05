package outland.feature.server.redis;

import io.dropwizard.lifecycle.Managed;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static outland.feature.server.StructLog.kvp;

@Singleton
public class RedisManaged implements Managed {

  private static final Logger logger = LoggerFactory.getLogger(RedisManaged.class);

  private RedisProviders providers;

  @Inject
  public RedisManaged(RedisProviders providers) {
    this.providers = providers;
  }

  @Override
  public void start() throws Exception {
    for (Map.Entry<String, RedisProvider> entry : providers.getProviders().entrySet()) {
      logger.info(kvp("op", "start_redis", "provider", entry.getKey()));
    }
  }

  @Override
  public void stop() throws Exception {
    for (Map.Entry<String, RedisProvider> entry : providers.getProviders().entrySet()) {
      logger.info(kvp("op", "stop_redis", "provider", entry.getKey()));
      entry.getValue().closePool();
    }
  }
}
