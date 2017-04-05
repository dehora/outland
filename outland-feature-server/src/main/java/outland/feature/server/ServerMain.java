package outland.feature.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.java8.Java8Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.server.app.GuiceApplication;
import outland.feature.server.namespaces.NamesapaceAuthService;
import outland.feature.server.namespaces.AppModule;
import outland.feature.server.auth.AuthPrincipal;
import outland.feature.server.auth.AuthConfiguration;
import outland.feature.server.auth.AuthModule;
import outland.feature.server.aws.DynamoDbModule;
import outland.feature.server.features.FeatureModule;
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
    final boolean strict = false;
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(strict)));
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
        new DynamoDbModule(configuration.aws),
        new AuthModule(configuration.auth),
        new FeatureModule(configuration.featureTable),
        new AppModule()
    );
  }

  @Override
  protected void applicationOnRun(ServerConfiguration configuration, Environment environment,
      Injector injector) {

    enableContentEncodingGzip(environment);
    configureAuth(configuration.auth, environment, injector);
  }

  private void enableContentEncodingGzip(Environment environment) {
    EncodingFilter.enableFor(environment.jersey().getResourceConfig(), GZipEncoder.class);
  }


  private void configureAuth(AuthConfiguration configuration, Environment environment,
      Injector injector) {

    Logger logger = LoggerFactory.getLogger(ServerMain.class);

    final UnauthorizedHandler unauthorizedHandler = injector.getInstance(UnauthorizedHandler.class);
    final ArrayList<AuthFilter> filters = Lists.newArrayList();

    if (configuration.oauthEnabled) {
      logger.info("op=auth_configuration,mechanism=oauth");

      final Authenticator<String, AuthPrincipal> oauthAppAuthenticator =
          injector.getInstance(Key.get(new TypeLiteral<Authenticator<String, AuthPrincipal>>() {
          }, Names.named("oauthAppAuthenticator")));

      final Authorizer<AuthPrincipal> oauthAppAuthorizer =
          injector.getInstance(Key.get(new TypeLiteral<Authorizer<AuthPrincipal>>() {
          }, Names.named("oauthAppAuthorizer")));

      final CachingAuthenticator<String, AuthPrincipal> cached = new CachingAuthenticator<>(
          environment.metrics(),
          oauthAppAuthenticator,
          CacheBuilder.newBuilder().maximumSize(1024)
              .expireAfterWrite(configuration.oauthCacheTokenSeconds, TimeUnit.SECONDS));

      final AuthFilter oauthFilter = new OAuthCredentialAuthFilter.Builder<AuthPrincipal>()
          .setPrefix(NamesapaceAuthService.BEARER)
          .setRealm("outland_feature")
          .setAuthenticator(cached)
          .setAuthorizer(oauthAppAuthorizer)
          .setUnauthorizedHandler(unauthorizedHandler)
          .buildAuthFilter();

      filters.add(oauthFilter);
    }

    if (configuration.basicEnabled) {
      logger.info("op=auth_configuration,mechanism=basic");

      final Authenticator<BasicCredentials, AuthPrincipal> basicAppAuthenticator =
          injector.getInstance(Key.get(new TypeLiteral<Authenticator<BasicCredentials, AuthPrincipal>>() {
          }, Names.named("basicAppAuthenticator")));

      final CachingAuthenticator<BasicCredentials, AuthPrincipal> cached = new CachingAuthenticator<>(
          environment.metrics(),
          basicAppAuthenticator,
          CacheBuilder.newBuilder().maximumSize(1024)
              .expireAfterWrite(configuration.basicCacheCredentialSeconds, TimeUnit.SECONDS));

      final AuthFilter basicAuthFilter = new BasicCredentialAuthFilter.Builder<AuthPrincipal>()
          .setPrefix("Basic")
          .setRealm("outland_feature")
          .setAuthenticator(basicAppAuthenticator)
          .setUnauthorizedHandler(unauthorizedHandler)
          .buildAuthFilter();

      filters.add(basicAuthFilter);
    }

    if(filters.size() == 0) {
      logger.warn("op=auth_configuration,mechanism=no_auth_configured");
    }

    final ChainedAuthFilter chainedAuthFilter = new ChainedAuthFilter(filters);
    environment.jersey().register(new AuthDynamicFeature(chainedAuthFilter));
    environment.jersey().register(RolesAllowedDynamicFeature.class);
    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(AuthPrincipal.class));
  }
}
