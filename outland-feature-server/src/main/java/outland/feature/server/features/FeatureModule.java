package outland.feature.server.features;

import com.google.inject.AbstractModule;
import javax.inject.Inject;

public class FeatureModule extends AbstractModule {

  private final TableConfiguration tableConfiguration;

  @Inject
  public FeatureModule(TableConfiguration tableConfiguration) {
    this.tableConfiguration = tableConfiguration;
  }

  @Override protected void configure() {
    bind(TableConfiguration.class).toInstance(tableConfiguration);
    bind(FeatureService.class).to(DefaultFeatureService.class).asEagerSingleton();
    bind(FeatureStorage.class).to(DefaultFeatureStorage.class).asEagerSingleton();
    bind(FeatureCache.class).to(DefaultFeatureCache.class).asEagerSingleton();
    bind(VersionService.class).to(Versions.class).asEagerSingleton();
    bind(RedisCache.class).asEagerSingleton();
  }
}
