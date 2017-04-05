package outland.feature.server.apps;

import com.google.inject.AbstractModule;
import javax.inject.Inject;

public class AppModule extends AbstractModule {

  // depends on TableConfiguration being loaded by FeatureModule

  @Inject
  public AppModule() {
  }

  @Override protected void configure() {
    bind(NamespaceService.class).to(DefaultNamespaceService.class).asEagerSingleton();
    bind(NamespaceStorage.class).to(DefaultNamespaceStorage.class).asEagerSingleton();
    bind(AppAuthService.class).to(AppAuthServiceViaPlanBServer.class).asEagerSingleton();
  }
}
