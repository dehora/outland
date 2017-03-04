package outland.feature.server.auth;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

public class BasicAuthenticator implements io.dropwizard.auth.Authenticator<BasicCredentials, App> {

  private final AuthConfiguration configuration;
  private final List<String> apiKeys = Lists.newArrayList();

  @Inject
  public BasicAuthenticator(AuthConfiguration configuration) {

    this.configuration = configuration;
    if (isUsingApiKeys(configuration)) {
      apiKeys.addAll(Splitter.on(",").splitToList(configuration.basicAuthenticationKeys));
    }
  }

  @Override public Optional<App> authenticate(BasicCredentials credentials)
      throws AuthenticationException {

    if (isUsingApiKeys(configuration)) {
      if (apiKeys.contains(credentials.getPassword())) {
        return Optional.of(new App(credentials.getUsername()));
      }
    }

    return Optional.empty();
  }

  private boolean isUsingApiKeys(AuthConfiguration configuration) {
    return AuthConfiguration.AUTHENTICATION_POLICY_BASIC_APP_USERNAME_PASSWORD_KEYS.equals(
        configuration.basicAuthenticationPolicy);
  }
}
