package outland.feature.server.auth;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

public class BasicAuthenticator implements io.dropwizard.auth.Authenticator<BasicCredentials, AppMember> {

  private final AuthConfiguration configuration;
  private final List<String> apiKeys = Lists.newArrayList();

  @Inject
  public BasicAuthenticator(AuthConfiguration configuration) {

    this.configuration = configuration;
    if (isUsingApiKeys(configuration)) {
      apiKeys.addAll(Splitter.on(",").splitToList(configuration.basicAuthenticationKeys));
    }
  }

  @Override public Optional<AppMember> authenticate(BasicCredentials credentials)
      throws AuthenticationException {

    if (isUsingApiKeys(configuration)) {
      if (apiKeys.contains(credentials.getPassword())) {

        final String[] split = credentials.getUsername().split("@");

        final String type = split[1];
        final String identifier = split[0];
        final ArrayList<String> scopes = Lists.newArrayList(TokenAuthorizer.WILDCARD_SCOPE);
        final AppMember value = new AppMember(
            type,
            identifier,
            scopes,
            credentials.getPassword(),
            Long.MAX_VALUE
        );
        return Optional.of(value);

      }
    }

    return Optional.empty();
  }

  private boolean isUsingApiKeys(AuthConfiguration configuration) {
    return AuthConfiguration.AUTHENTICATION_POLICY_BASIC_APP_USERNAME_PASSWORD_KEYS.equals(
        configuration.basicAuthenticationPolicy);
  }
}
