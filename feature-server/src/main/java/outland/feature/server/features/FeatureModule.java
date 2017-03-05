package outland.feature.server.features;

import com.google.inject.AbstractModule;

public class FeatureModule extends AbstractModule {

  @Override protected void configure() {
    bind(FeatureService.class).to(DefaultFeatureService.class).asEagerSingleton();
    bind(FeatureStorage.class).to(DefaultFeatureStorage.class).asEagerSingleton();
    bind(FeatureCache.class).to(DefaultFeatureCache.class).asEagerSingleton();
    bind(RedisCache.class).asEagerSingleton();
  }
}
