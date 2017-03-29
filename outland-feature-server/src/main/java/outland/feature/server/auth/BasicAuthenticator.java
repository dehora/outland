package outland.feature.server.auth;

import com.google.common.collect.Lists;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

public class BasicAuthenticator
    implements io.dropwizard.auth.Authenticator<BasicCredentials, AuthPrincipal> {

  private final ApiKeyCredentials apiKeyCredentials;

  @Inject
  public BasicAuthenticator(ApiKeyCredentials apiKeyCredentials) {
    this.apiKeyCredentials = apiKeyCredentials;
  }

  @Override public Optional<AuthPrincipal> authenticate(BasicCredentials credentials)
      throws AuthenticationException {
    final String[] split = credentials.getUsername().split("/");
    return apiKeyCredentials.authenticated(credentials.getPassword(), split[0], split[1]);
  }
}
