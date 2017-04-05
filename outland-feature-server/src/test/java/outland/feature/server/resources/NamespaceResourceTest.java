package outland.feature.server.resources;

import com.google.gson.Gson;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.assertj.core.util.Lists;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.ClassRule;
import org.junit.Test;
import outland.feature.proto.AccessCollection;
import outland.feature.proto.Namespace;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.proto.ServiceAccess;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.protobuf.Protobuf3Support;

import static junit.framework.TestCase.assertTrue;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME;

public class NamespaceResourceTest {

  @ClassRule public static final DropwizardAppRule<ServerConfiguration> APP = ServerSuite.APP;

  private static String basicPassword = "topsecret";
  private static String authService = "knownservice";

  private final static Gson gson = new Gson();

  public NamespaceResourceTest() {
  }

  @Test
  public void testDoubleCreatePostCauses409() throws Exception {

    final String appKey = "testDoubleCreatePost409AppKey";
    final String serviceKey = "testDoubleCreatePost409Service";

    final AccessCollection.Builder accessBuilder = AccessCollection.newBuilder();
    final ArrayList<ServiceAccess> services = Lists.newArrayList();
    services.add(ServiceAccess.newBuilder().setKey(serviceKey).buildPartial());
    accessBuilder.addAllServices(services);
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .setType("owner.collection")
        .addItems(Owner.newBuilder().setName("Jayne").setUsername("jayne"));
    final Namespace namespace = Namespace.newBuilder()
        .setKey(appKey)
        .setName("name")
        .setOwners(oc)
        .setGranted(accessBuilder.buildPartial())
        .build();

    final String url = createAppUrl();
    final JerseyClient client = createClient();

    final String jsonReq = Protobuf3Support.toJsonString(namespace);

    final Response post = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, authService + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(post.getStatus() == 201);

    final Response postAgain = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, authService + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(postAgain.getStatus() == 409);
    final String jsonRes = postAgain.readEntity(String.class);
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 409);
    assertTrue(problem.title().contains("conflict_app_already_exists"));
  }

  private String createAppUrl() {
    return "http://localhost:" + APP.getLocalPort() + "/apps";
  }

  private JerseyClient createClient() {
    return ServerSuite.client()
        .register(
            new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());
  }
}
