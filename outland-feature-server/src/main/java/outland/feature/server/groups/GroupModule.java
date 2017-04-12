package outland.feature.server.groups;

import com.google.inject.AbstractModule;
import javax.inject.Inject;

public class GroupModule extends AbstractModule {

  // depends on TableConfiguration being loaded by FeatureModule

  @Inject
  public GroupModule() {
  }

  @Override protected void configure() {
    bind(GroupService.class).to(DefaultGroupService.class).asEagerSingleton();
    bind(GroupStorage.class).to(DefaultGroupStorage.class).asEagerSingleton();
    bind(NamesapaceAuthService.class).to(NamesapaceAuthServiceViaPlanBServer.class)
        .asEagerSingleton();
  }
}
