package outland.feature.server;

import com.google.inject.AbstractModule;
import outland.feature.server.hystrix.HystrixConfiguration;
import outland.feature.server.resources.HelloResource;
import outland.feature.server.resources.IdempotencyChecker;
import outland.feature.server.resources.IdempotencyCheckerRedis;

import static com.google.inject.name.Names.named;

public class ServerModule extends AbstractModule {

  private final ServerConfiguration configuration;

  public ServerModule(ServerConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    bind(ServerConfiguration.class).toInstance(configuration);
    bind(HelloResource.class).asEagerSingleton();
    bind(IdempotencyChecker.class).to(IdempotencyCheckerRedis.class).asEagerSingleton();

    bind(HystrixConfiguration.class)
        .annotatedWith(named("redisFeatureCacheHystrix"))
        .toInstance(configuration.redisFeatureCacheHystrix);

    bind(HystrixConfiguration.class)
        .annotatedWith(named("dynamodbFeatureWriteHystrix"))
        .toInstance(configuration.dynamodbFeatureWriteHystrix);

    bind(HystrixConfiguration.class)
        .annotatedWith(named("dynamodbFeatureReadHystrix"))
        .toInstance(configuration.dynamodbFeatureReadHystrix);

    bind(HystrixConfiguration.class)
        .annotatedWith(named("dynamodbGraphWriteHystrix"))
        .toInstance(configuration.dynamodbGraphWriteHystrix);

    bind(HystrixConfiguration.class)
        .annotatedWith(named("dynamodbGroupWriteHystrix"))
        .toInstance(configuration.dynamodbGroupWriteHystrix);

    bind(HystrixConfiguration.class)
        .annotatedWith(named("dynamodbNamespaceGraphQueryHystrix"))
        .toInstance(configuration.dynamodbGroupWriteHystrix);
  }
}
