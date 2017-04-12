package outland.feature.server.resources;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.util.List;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.ServerMain;
import outland.feature.server.ServerModule;
import outland.feature.server.groups.TestAppModule;
import outland.feature.server.auth.AuthModule;
import outland.feature.server.aws.DynamoDbModule;
import outland.feature.server.features.TestFeatureServiceModule;
import outland.feature.server.hystrix.HystrixModule;
import outland.feature.server.redis.RedisModule;

@RunWith(Suite.class)
@Suite.SuiteClasses({HelloResourceTest.class})
public class ServerSuite {

  private static final String CONFIG_PATH =
      ResourceHelpers.resourceFilePath("test-configuration.yaml");

  @ClassRule
  public static final DropwizardAppRule<ServerConfiguration> APP =
      new DropwizardAppRule<ServerConfiguration>(
          new DropwizardTestSupport<ServerConfiguration>(ServerMain.class, CONFIG_PATH) {
            @Override public Application<ServerConfiguration> newApplication() {

              return new ServerMain("outland.feature.server") {

                @Override
                protected List<Module> addModules(
                    ServerConfiguration configuration, Environment environment) {
                  return Lists.newArrayList(
                      new ServerModule(configuration),
                      new HystrixModule(),
                      new RedisModule(configuration.redis),
                      new DynamoDbModule(configuration.aws),
                      new AuthModule(configuration.auth),
                      new TestFeatureServiceModule(),
                      new TestAppModule()
                  );
                }
              };
            }
          }

      ) {

      };

  static JerseyClient client() {
    return new JerseyClientBuilder()
        .property(ClientProperties.CONNECT_TIMEOUT, 1000)
        .property(ClientProperties.READ_TIMEOUT, 2000).build();
  }
}
