package outland.feature.server.auth;

import io.dropwizard.auth.AuthenticationException;
import java.util.Optional;
import javax.inject.Inject;

public class TokenOAuthAuthenticator implements io.dropwizard.auth.Authenticator<String, App> {

  private final AuthConfiguration serviceConfiguration;

  @Inject
  public TokenOAuthAuthenticator(AuthConfiguration configuration) {
    this.serviceConfiguration = configuration;
  }

  @Override public Optional<App> authenticate(String credentials) throws AuthenticationException {

    if (AuthConfiguration.AUTHENTICATION_POLICY_OAUTH_BEARER_REFLECT.equals(
        serviceConfiguration.oauthAuthenticationPolicy)) {
      return Optional.of(new App(credentials));
    }

    // todo: integrate with tokens
    return Optional.empty();
  }
}
