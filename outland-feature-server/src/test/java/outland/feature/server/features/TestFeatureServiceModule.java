package outland.feature.server.features;

import com.google.inject.AbstractModule;

public class TestFeatureServiceModule extends AbstractModule {

  @Override protected void configure() {
    bind(FeatureService.class).to(DefaultFeatureService.class).asEagerSingleton();
    bind(FeatureStorage.class).to(TestFeatureStorage.class).asEagerSingleton();
    bind(FeatureCache.class).to(TestFeatureCache.class).asEagerSingleton();
    bind(VersionService.class).to(DefaultVersionService.class).asEagerSingleton();
  }
}
