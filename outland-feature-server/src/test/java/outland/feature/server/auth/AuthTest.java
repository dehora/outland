package outland.feature.server.auth;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.Authorizer;
import io.dropwizard.auth.basic.BasicCredentials;
import java.net.URI;
import java.util.Optional;
import org.junit.Test;
import outland.feature.server.apps.AppAuthService;
import outland.feature.server.apps.AppAuthServiceRemoteServer;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthTest {

  @Test
  public void testLoad() throws Exception {


    AuthConfiguration.RemoteOAuthServerConfiguration rsc =
        new AuthConfiguration.RemoteOAuthServerConfiguration();

    rsc.tokenLookupURI = new URI("http:localhost:9999");

    AuthConfiguration authConfiguration = new AuthConfiguration();
    authConfiguration.remoteOAuthServer = rsc;

    authConfiguration.oauthEnabled=false; // turn off oauth for now

    authConfiguration.basicAuthenticationKeys="letmein,secret";
    authConfiguration.basicEnabled=true;
    authConfiguration.basicScopePolicy = AuthConfiguration.SCOPE_BASIC_POLICY_DISABLED;

    final Injector injector = Guice.createInjector(
        new AuthModule(authConfiguration),
        new AbstractModule(){
          @Override protected void configure() {
            bind(AppAuthService.class).to(AppAuthServiceRemoteServer.class).asEagerSingleton();
          }
        }

    );

    final Authenticator<BasicCredentials, AppMember> authenticator =
        injector.getInstance(Key.get(new TypeLiteral<Authenticator<BasicCredentials, AppMember>>() {
        }, Names.named("basicAppAuthenticator")));

    try {
      final Optional<AppMember> authenticate =
          authenticator.authenticate(new BasicCredentials("foo/owner", "letmein"));
      assertTrue(authenticate.isPresent());

    } catch (AuthenticationException e) {
      e.printStackTrace();
      fail();
    }

    try {
      final Optional<AppMember> authenticate =
          authenticator.authenticate(new BasicCredentials("foo/owner", "badsecret"));
      assertFalse(authenticate.isPresent());
    } catch (AuthenticationException e) {
      e.printStackTrace();
      fail();
    }

    final Authorizer<AppMember> authorizer =
        injector.getInstance(Key.get(new TypeLiteral<Authorizer<AppMember>>() {
        }, Names.named("basicAppAuthorizer")));

    // scope checks are disabled
    assertTrue(authorizer.authorize(new AppMember(
        "service",
        "foo",
        Lists.newArrayList(),
        "woo",
        100000

    ), "any"));

  }
}
