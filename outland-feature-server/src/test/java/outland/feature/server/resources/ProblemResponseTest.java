package outland.feature.server.resources;

import com.google.gson.Gson;
import io.dropwizard.testing.junit.DropwizardAppRule;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.JerseyClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;

import static junit.framework.TestCase.assertTrue;

public class ProblemResponseTest {

  @ClassRule public static final DropwizardAppRule<ServerConfiguration> APP = ServerSuite.APP;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void test404IsAProblem() {

    String url = "http://localhost:" + APP.getLocalPort() + "/404";
    JerseyClient clientNoAuth = ServerSuite.client();

    Response response = clientNoAuth.target(url)
        .request()
        .get();

    assertTrue(response.getStatus() == 404);

    final String raw = response.readEntity(String.class);
    Gson gson = new Gson();
    final Problem problem = gson.fromJson(raw, Problem.class);
    assertTrue(problem.title() != null);
    assertTrue(problem.data().containsKey("trace_id"));
    assertTrue(problem.status() == 404);
  }
}
