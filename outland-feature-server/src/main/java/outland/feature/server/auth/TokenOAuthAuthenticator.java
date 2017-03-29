package outland.feature.server.auth;

import com.google.common.collect.Lists;
import io.dropwizard.auth.AuthenticationException;
import java.util.Optional;
import javax.inject.Inject;
import outland.feature.server.apps.AppAuthService;

public class TokenOAuthAuthenticator implements io.dropwizard.auth.Authenticator<String, AuthPrincipal> {

  private final AppAuthService appAuthService;

  @Inject
  public TokenOAuthAuthenticator(
      AppAuthService appAuthService
  ) {
    this.appAuthService = appAuthService;
  }

  @Override public Optional<AuthPrincipal> authenticate(String credentials) throws AuthenticationException {
    return appAuthService.authenticate(credentials, AppAuthService.BEARER);
  }
}
