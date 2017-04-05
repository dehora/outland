package outland.feature.server.auth;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import outland.feature.server.apps.AppAuthService;
import outland.feature.server.apps.AppAuthServiceViaPlanBServer;
import outland.feature.server.apps.TestAppModule;
import outland.feature.server.features.VersionService;
import outland.feature.server.features.Versions;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthTest {

  private final String serviceKey = "foo";
  private final String apiKey = "letmein";

  @Test
  public void testLoad() throws Exception {


    AuthConfiguration.RemoteOAuthServerConfiguration rsc =
        new AuthConfiguration.RemoteOAuthServerConfiguration();

    rsc.tokenLookupURI = new URI("http:localhost:9999");

    AuthConfiguration authConfiguration = new AuthConfiguration();
    authConfiguration.remoteOAuthServer = rsc;

    authConfiguration.oauthEnabled=false; // turn off oauth for now

    authConfiguration.basicAuthenticationKeys = serviceKey + "=" + apiKey;
    authConfiguration.basicEnabled=true;
    authConfiguration.multipleNamespaceAccessList = "acme.ui";

    final Injector injector = Guice.createInjector(
        new AuthModule(authConfiguration),
        new TestAppModule(),
        new AbstractModule(){
          @Override protected void configure() {
            bind(VersionService.class).to(Versions.class).asEagerSingleton();
            bind(AppAuthService.class).to(AppAuthServiceViaPlanBServer.class).asEagerSingleton();
            List<String> multipleNamespaceAccessList = Lists.newArrayList();
            multipleNamespaceAccessList.addAll(
                Splitter.on(",").splitToList(authConfiguration.multipleNamespaceAccessList));
            bind(new TypeLiteral<List<String>>() {
            }).annotatedWith(Names.named("multipleNamespaceAccessList"))
                .toInstance(multipleNamespaceAccessList);
            bind(AccessControlSupport.class).asEagerSingleton();
          }
        }

    );

    final Authenticator<BasicCredentials, AuthPrincipal> authenticator =
        injector.getInstance(Key.get(new TypeLiteral<Authenticator<BasicCredentials, AuthPrincipal>>() {
        }, Names.named("basicAppAuthenticator")));

    try {
      final Optional<AuthPrincipal> authenticate =
          authenticator.authenticate(new BasicCredentials(serviceKey + "/owner", apiKey));
      assertTrue(authenticate.isPresent());

    } catch (AuthenticationException e) {
      e.printStackTrace();
      fail();
    }

    try {
      final Optional<AuthPrincipal> authenticate =
          authenticator.authenticate(new BasicCredentials(serviceKey + "/owner", "badsecret"));
      assertFalse(authenticate.isPresent());
    } catch (AuthenticationException e) {
      e.printStackTrace();
      fail();
    }
  }
}
