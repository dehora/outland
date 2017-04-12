package outland.feature.server.redis;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisModule extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(RedisModule.class);
  private final RedisServersConfiguration configuration;

  public RedisModule(RedisServersConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    bind(RedisManaged.class).asEagerSingleton();
    RedisProviders providers = new RedisProviders(configuration);
    bind(RedisProviders.class).toInstance(providers);
    bind(RedisHealthCheck.class);

    MapBinder<String, RedisProvider> mapBinder
        = MapBinder.newMapBinder(binder(), String.class, RedisProvider.class);

    final Map<String, RedisProvider> providerMap = providers.getProviders();
    for (Map.Entry<String, RedisProvider> entry : providerMap.entrySet()) {
      mapBinder.addBinding(entry.getKey()).toInstance(entry.getValue());
    }
  }
}