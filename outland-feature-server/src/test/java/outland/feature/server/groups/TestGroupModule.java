package outland.feature.server.groups;

import com.google.inject.AbstractModule;
import javax.inject.Inject;

public class TestGroupModule extends AbstractModule {

  @Inject
  public TestGroupModule() {
  }

  @Override protected void configure() {
    bind(GroupService.class).to(DefaultGroupService.class).asEagerSingleton();
    bind(GroupStorage.class).to(TestGroupStorage.class).asEagerSingleton();
    bind(NamesapaceAuthService.class).to(NamesapaceAuthServiceViaPlanBServer.class).asEagerSingleton();
    bind(GroupCache.class).to(TestGroupCache.class).asEagerSingleton();
  }
}
