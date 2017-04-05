package outland.feature.server.apps;

import com.google.inject.AbstractModule;
import javax.inject.Inject;

public class TestAppModule extends AbstractModule {

  @Inject
  public TestAppModule() {
  }

  @Override protected void configure() {
    bind(NamespaceService.class).to(DefaultNamespaceService.class).asEagerSingleton();
    bind(NamespaceStorage.class).to(TestNamespaceStorage.class).asEagerSingleton();
    bind(AppAuthService.class).to(AppAuthServiceViaPlanBServer.class).asEagerSingleton();
  }
}
