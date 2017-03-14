package outland.feature.server.apps;

import com.google.inject.AbstractModule;
import javax.inject.Inject;

public class TestAppModule extends AbstractModule {

  @Inject
  public TestAppModule() {
  }

  @Override protected void configure() {
    bind(AppService.class).to(DefaultAppService.class).asEagerSingleton();
    bind(AppStorage.class).to(TestAppStorage.class).asEagerSingleton();
    bind(AppAuthService.class).to(AppAuthServiceViaPlanBServer.class).asEagerSingleton();
  }
}
