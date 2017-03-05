package outland.feature.server.resources;

import io.dropwizard.testing.junit.DropwizardAppRule;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.JerseyClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import outland.feature.server.ServerConfiguration;

import static junit.framework.TestCase.assertTrue;

public class HelloResourceTest {

  @ClassRule public static final DropwizardAppRule<ServerConfiguration> APP = ServerSuite.APP;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testHello() {

    String url = "http://localhost:" + APP.getLocalPort() + "/hello";
    JerseyClient clientNoAuth = ServerSuite.client();

    Response response = clientNoAuth.target(url)
        .request()
        .get();
    assertTrue(response.getStatus() == 200);
  }
}
