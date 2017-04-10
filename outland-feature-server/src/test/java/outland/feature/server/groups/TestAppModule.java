package outland.feature.server.groups;

import com.google.inject.AbstractModule;
import javax.inject.Inject;

public class TestAppModule extends AbstractModule {

  @Inject
  public TestAppModule() {
  }

  @Override protected void configure() {
    bind(GroupService.class).to(DefaultGroupService.class).asEagerSingleton();
    bind(GroupStorage.class).to(TestGroupStorage.class).asEagerSingleton();
    bind(NamesapaceAuthService.class).to(NamesapaceAuthServiceViaPlanBServer.class).asEagerSingleton();
  }
}
