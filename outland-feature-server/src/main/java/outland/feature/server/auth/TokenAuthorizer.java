package outland.feature.server.auth;

import javax.inject.Inject;

public class TokenAuthorizer implements io.dropwizard.auth.Authorizer<App> {

  private final AuthConfiguration serviceConfiguration;

  @Inject
  public TokenAuthorizer(AuthConfiguration authConfiguration) {
    this.serviceConfiguration = authConfiguration;
  }


  @Override public boolean authorize(App principal, String role) {

    if(AuthConfiguration.SCOPE_OAUTH_POLICY_DISABLED.equals(serviceConfiguration.oauthScopePolicy)) {
      return true;
    }

    // todo: integrate with tokens
    return false;
  }
}
