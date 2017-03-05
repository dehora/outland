package outland.feature.server.auth;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.auth.basic.BasicCredentials;
import javax.inject.Inject;

public class AuthModule extends AbstractModule {

  private final AuthConfiguration authConfiguration;

  @Inject
  public AuthModule(AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  @Override protected void configure() {

    bind(AuthConfiguration.class).toInstance(authConfiguration);

    bind(new TypeLiteral<Authenticator<String, App>>(){})
        .annotatedWith(Names.named("oauthAppAuthenticator"))
        .to(TokenOAuthAuthenticator.class);

    bind(new TypeLiteral<Authorizer<App>>(){})
        .annotatedWith(Names.named("oauthAppAuthorizer"))
        .to(TokenAuthorizer.class);

    bind(new TypeLiteral<Authenticator<BasicCredentials, App>>(){})
        .annotatedWith(Names.named("basicAppAuthenticator"))
        .to(BasicAuthenticator.class);

    bind(new TypeLiteral<Authorizer<App>>(){})
        .annotatedWith(Names.named("basicAppAuthorizer"))
        .to(BasicAuthorizer.class);

    bind(UnauthorizedHandler.class).to(DefaultUnauthorizedHandler.class);
  }
}
