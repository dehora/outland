package outland.feature.server;

import com.google.inject.AbstractModule;
import outland.feature.server.resources.HelloResource;

public class ServerModule extends AbstractModule {

  private final ServerConfiguration configuration;

  public ServerModule(ServerConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    bind(ServerConfiguration.class).toInstance(configuration);
    bind(HelloResource.class).asEagerSingleton();
  }
}
