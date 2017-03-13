package outland.feature.server.auth;

import com.google.common.collect.Lists;
import io.dropwizard.auth.AuthenticationException;
import java.util.Optional;
import javax.inject.Inject;

public class TokenOAuthAuthenticator implements io.dropwizard.auth.Authenticator<String, AppMember> {

  private final AuthConfiguration serviceConfiguration;

  @Inject
  public TokenOAuthAuthenticator(
      AuthConfiguration configuration
  ) {
    this.serviceConfiguration = configuration;
  }

  @Override public Optional<AppMember> authenticate(String credentials) throws AuthenticationException {

    if (AuthConfiguration.AUTHENTICATION_POLICY_OAUTH_BEARER_REFLECT.equals(
        serviceConfiguration.oauthAuthenticationPolicy)) {
      /*
      Exercises oauth by sending bearer credentials in the form identity@[service,owner].
      Exists for development.
       */

      final String[] split = credentials.split("/");

      return Optional.of(new AppMember(
          split[1],
          split[0],
          Lists.newArrayList(TokenAuthorizer.WILDCARD_SCOPE),
          credentials,
          Long.MAX_VALUE
      ));
    }

    return Optional.empty();
  }
}
