package outland.feature.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.List;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;
import outland.feature.server.app.GuiceApplication;
import outland.feature.server.aws.DynamoDbModule;
import outland.feature.server.hystrix.HystrixModule;
import outland.feature.server.protobuf.Protobuf3Bundle;
import outland.feature.server.redis.RedisModule;

public class ServerMain extends GuiceApplication<ServerConfiguration> {

  @VisibleForTesting
  public ServerMain() {
    this("outland.feature");
  }

  @SuppressWarnings("WeakerAccess") public ServerMain(String... packages) {
    super(packages);
  }

  public static void main(String[] args) throws Exception {
    DnsCache.setup();
    new ServerMain("outland.feature").run(args);
  }

  @Override
  public void initialize(Bootstrap<ServerConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor()));
    bootstrap.addBundle(new Java8Bundle());
    bootstrap.addBundle(new Protobuf3Bundle());
    super.initialize(bootstrap);
  }

  @Override
  protected List<Module> addModules(ServerConfiguration configuration, Environment environment) {
    return Lists.newArrayList(
        new ServerModule(configuration),
        new HystrixModule(),
        new RedisModule(configuration.redis),
        new DynamoDbModule(configuration.aws)
    );
  }

  @Override
  protected void applicationOnRun(ServerConfiguration configuration, Environment environment,
      Injector injector) {

    enableContentEncodingGzip(environment);
  }

  private void enableContentEncodingGzip(Environment environment) {
    EncodingFilter.enableFor(environment.jersey().getResourceConfig(), GZipEncoder.class);
  }
}
