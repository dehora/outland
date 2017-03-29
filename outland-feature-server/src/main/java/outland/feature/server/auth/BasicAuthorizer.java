package outland.feature.server.auth;

import javax.inject.Inject;

public class BasicAuthorizer implements io.dropwizard.auth.Authorizer<AuthPrincipal> {

  private final AuthConfiguration authConfiguration;

  @Inject
  public BasicAuthorizer(AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
  }

  @Override public boolean authorize(AuthPrincipal principal, String role) {
    return true;
  }
}
