package outland.feature.server.auth;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.auth.basic.BasicCredentials;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import okhttp3.OkHttpClient;

public class AuthModule extends AbstractModule {

  private final AuthConfiguration authConfiguration;

  @Inject
  public AuthModule(AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  @Override protected void configure() {

    bind(AuthConfiguration.class).toInstance(authConfiguration);

    bind(new TypeLiteral<Authenticator<String, AppMember>>(){})
        .annotatedWith(Names.named("oauthAppAuthenticator"))
        .to(TokenOAuthAuthenticator.class);

    bind(new TypeLiteral<Authorizer<AppMember>>(){})
        .annotatedWith(Names.named("oauthAppAuthorizer"))
        .to(TokenAuthorizer.class);

    bind(new TypeLiteral<Authenticator<BasicCredentials, AppMember>>(){})
        .annotatedWith(Names.named("basicAppAuthenticator"))
        .to(BasicAuthenticator.class);

    bind(new TypeLiteral<Authorizer<AppMember>>(){})
        .annotatedWith(Names.named("basicAppAuthorizer"))
        .to(BasicAuthorizer.class);

    bind(UnauthorizedHandler.class).to(DefaultUnauthorizedHandler.class);

    OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .connectTimeout(authConfiguration.remoteOAuthServer.connectTimeout, TimeUnit.MILLISECONDS)
        .readTimeout(authConfiguration.remoteOAuthServer.connectTimeout, TimeUnit.MILLISECONDS);

    OkHttpClient client = builder.build();

    bind(OkHttpClient.class)
        .annotatedWith(Names.named("OAuthServiceClient"))
        .toInstance(client);

    bind(URI.class)
        .annotatedWith(Names.named("OAuthServiceTokenLookupUri"))
        .toInstance(authConfiguration.remoteOAuthServer.tokenLookupURI);
  }
}
