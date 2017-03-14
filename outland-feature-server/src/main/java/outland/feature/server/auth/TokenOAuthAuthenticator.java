package outland.feature.server.auth;

import com.google.common.collect.Lists;
import io.dropwizard.auth.AuthenticationException;
import java.util.Optional;
import javax.inject.Inject;
import outland.feature.server.apps.AppAuthService;

public class TokenOAuthAuthenticator implements io.dropwizard.auth.Authenticator<String, AuthPrincipal> {

  private final AuthConfiguration serviceConfiguration;
  private final AppAuthService appAuthService;

  @Inject
  public TokenOAuthAuthenticator(
      AuthConfiguration configuration,
      AppAuthService appAuthService
  ) {
    this.serviceConfiguration = configuration;
    this.appAuthService = appAuthService;
  }

  @Override public Optional<AuthPrincipal> authenticate(String credentials) throws AuthenticationException {

    if (AuthConfiguration.AUTHENTICATION_POLICY_OAUTH_BEARER_REFLECT.equals(
        serviceConfiguration.oauthAuthenticationPolicy)) {
      /*
      Exercises oauth by sending bearer credentials in the form identity/[service,owner].
      Exists for development.
       */

      final String[] split = credentials.split("/");

      return Optional.of(new AuthPrincipal(
          split[1], split[0], Lists.newArrayList(TokenAuthorizer.WILDCARD_SCOPE)
      ));
    }

    return appAuthService.authenticate(credentials, AppAuthService.BEARER);
  }
}
