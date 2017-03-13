package outland.feature.server.auth;

import javax.inject.Inject;

public class BasicAuthorizer implements io.dropwizard.auth.Authorizer<AppMember> {

  private final AuthConfiguration authConfiguration;

  @Inject
  public BasicAuthorizer(AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  @Override public boolean authorize(AppMember principal, String role) {

    if(AuthConfiguration.SCOPE_BASIC_POLICY_DISABLED.equals(authConfiguration.basicScopePolicy)) {
      return true;
    }

    return false;
  }
}
