package outland.feature.server.auth;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.auth.basic.BasicCredentials;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthModule extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(AuthModule.class);

  private final AuthConfiguration authConfiguration;

  @Inject
  public AuthModule(AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  @Override protected void configure() {

    bind(AuthConfiguration.class).toInstance(authConfiguration);

    bind(new TypeLiteral<Authenticator<String, AuthPrincipal>>() {
    })
        .annotatedWith(Names.named("oauthAppAuthenticator"))
        .to(TokenOAuthAuthenticator.class);

    bind(new TypeLiteral<Authorizer<AuthPrincipal>>() {
    })
        .annotatedWith(Names.named("oauthAppAuthorizer"))
        .to(TokenAuthorizer.class);

    bind(new TypeLiteral<Authenticator<BasicCredentials, AuthPrincipal>>() {
    })
        .annotatedWith(Names.named("basicAppAuthenticator"))
        .to(BasicAuthenticator.class);

    bind(new TypeLiteral<Authorizer<AuthPrincipal>>() {
    })
        .annotatedWith(Names.named("basicAppAuthorizer"))
        .to(BasicAuthorizer.class);

    List<String> multipleAppAccessList = Lists.newArrayList();

    multipleAppAccessList.addAll(
        Splitter.on(",").splitToList(authConfiguration.multipleAppAccessList));

    bind(new TypeLiteral<List<String>>() {
    }).annotatedWith(Names.named("multipleAppAccessList")).toInstance(multipleAppAccessList);

    bind(AccessControlSupport.class).asEagerSingleton();

    bind(UnauthorizedHandler.class).to(DefaultUnauthorizedHandler.class);

    OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .connectTimeout(authConfiguration.remoteOAuthServer.connectTimeout, TimeUnit.MILLISECONDS)
        .readTimeout(authConfiguration.remoteOAuthServer.connectTimeout, TimeUnit.MILLISECONDS);

    OkHttpClient client = builder.build();

    bind(OkHttpClient.class)
        .annotatedWith(Names.named("OAuthServiceClient"))
        .toInstance(client);

    logger.info("op=configure_oauth,remote_oauth_lookup_url={}",
        authConfiguration.remoteOAuthServer.tokenLookupURI);

    bind(URI.class)
        .annotatedWith(Names.named("OAuthServiceTokenLookupUri"))
        .toInstance(authConfiguration.remoteOAuthServer.tokenLookupURI);
  }
}
