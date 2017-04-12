package outland.feature.server.auth;

import javax.inject.Inject;

public class TokenAuthorizer implements io.dropwizard.auth.Authorizer<AuthPrincipal> {

  public static final String WILDCARD_SCOPE = "*";

  private final AuthConfiguration serviceConfiguration;

  @Inject
  public TokenAuthorizer(AuthConfiguration authConfiguration) {
    this.serviceConfiguration = authConfiguration;
  }

  @Override public boolean authorize(AuthPrincipal principal, String role) {

    if (AuthConfiguration.SCOPE_OAUTH_POLICY_DISABLED.equals(
        serviceConfiguration.oauthScopePolicy)) {
      return true;
    }

    if (principal.scopes().contains(WILDCARD_SCOPE)) {
      return true;
    }

    return principal.scopes().contains(role);
  }
}
