package outland.feature.server.auth;

import io.dropwizard.auth.AuthenticationException;
import java.util.Optional;
import javax.inject.Inject;
import outland.feature.server.groups.NamesapaceAuthService;

public class TokenOAuthAuthenticator
    implements io.dropwizard.auth.Authenticator<String, AuthPrincipal> {

  private final NamesapaceAuthService namesapaceAuthService;

  @Inject
  public TokenOAuthAuthenticator(
      NamesapaceAuthService namesapaceAuthService
  ) {
    this.namesapaceAuthService = namesapaceAuthService;
  }

  @Override public Optional<AuthPrincipal> authenticate(String credentials)
      throws AuthenticationException {
    return namesapaceAuthService.authenticate(credentials, NamesapaceAuthService.BEARER);
  }
}
