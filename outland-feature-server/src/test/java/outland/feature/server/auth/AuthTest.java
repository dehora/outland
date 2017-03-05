package outland.feature.server.auth;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.Optional;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthTest {

  @Test
  public void testLoad() {

    AuthConfiguration authConfiguration = new AuthConfiguration();
    authConfiguration.oauthEnabled=false; // turn off oauth for now

    authConfiguration.basicAuthenticationKeys="letmein,secret";
    authConfiguration.basicEnabled=true;
    authConfiguration.basicScopePolicy = AuthConfiguration.SCOPE_BASIC_POLICY_DISABLED;

    final Injector injector = Guice.createInjector(new AuthModule(authConfiguration));

    final Authenticator<BasicCredentials, App> authenticator =
        injector.getInstance(Key.get(new TypeLiteral<Authenticator<BasicCredentials, App>>() {
        }, Names.named("basicAppAuthenticator")));

    try {
      final Optional<App> authenticate =
          authenticator.authenticate(new BasicCredentials("foo", "letmein"));
      assertTrue(authenticate.isPresent());

    } catch (AuthenticationException e) {
      e.printStackTrace();
      fail();
    }

    try {
      final Optional<App> authenticate =
          authenticator.authenticate(new BasicCredentials("foo", "badsecret"));
      assertFalse(authenticate.isPresent());
    } catch (AuthenticationException e) {
      e.printStackTrace();
      fail();
    }

    final Authorizer<App> authorizer =
        injector.getInstance(Key.get(new TypeLiteral<Authorizer<App>>() {
        }, Names.named("basicAppAuthorizer")));

    // scope checks are disabled
    assertTrue(authorizer.authorize(new App("foo"), "any"));

  }
}
