package outland.feature.server.apps;

import com.google.inject.AbstractModule;
import javax.inject.Inject;

public class AppModule extends AbstractModule {

  // depends on TableConfiguration being loaded by FeatureModule

  @Inject
  public AppModule() {
  }

  @Override protected void configure() {
    bind(AppService.class).to(DefaultAppService.class).asEagerSingleton();
    bind(AppStorage.class).to(DefaultAppStorage.class).asEagerSingleton();
    bind(AppAuthService.class).to(AppAuthServiceViaPlanBServer.class).asEagerSingleton();
  }
}
