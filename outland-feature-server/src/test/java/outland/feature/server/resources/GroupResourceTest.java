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
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.ClassRule;
import org.junit.Test;
import outland.feature.proto.AccessCollection;
import outland.feature.proto.Group;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.proto.ServiceAccess;
import outland.feature.server.Names;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.TestSupport;
import outland.feature.server.protobuf.Protobuf3Support;

import static junit.framework.TestCase.assertTrue;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME;

public class GroupResourceTest {

  @ClassRule public static final DropwizardAppRule<ServerConfiguration> APP = ServerSuite.APP;

  private static String basicPassword = "topsecret";
  private static String authService = "knownservice";

  private final static Gson gson = new Gson();

  public GroupResourceTest() {
  }

  private JerseyInvocation.Builder buildAuthorisedClient(String url) {
    final JerseyClient client = createClient();
    return client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, authService + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword);
  }

  @Test
  public void testInvalidContentCauses422() throws Exception {
    String invalid = TestSupport.load("json/group-invalid-owner.json");
    final String url = createAppUrl();

    final Response response =
        buildAuthorisedClient(url).post(Entity.entity(invalid, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(response.getStatus() == 422);
    final Problem problem = gson.fromJson(response.readEntity(String.class), Problem.class);
    assertTrue(problem.status() == 422);
    assertTrue(problem.title().contains("request_entity_invalid"));
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
        .setType(Names.ownerCollectionType())
        .addItems(Owner.newBuilder().setName("Jayne").setUsername("jayne"));
    final Group group = Group.newBuilder()
        .setKey(appKey)
        .setName("name")
        .setOwners(oc)
        .setGranted(accessBuilder.buildPartial())
        .build();

    final String url = createAppUrl();
    final JerseyClient client = createClient();

    final String jsonReq = Protobuf3Support.toJsonString(group);

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
    assertTrue(problem.title().contains("conflict_group_already_exists"));
  }

  private String createAppUrl() {
    return "http://localhost:" + APP.getLocalPort() + "/groups";
  }

  private JerseyClient createClient() {
    return ServerSuite.client()
        .register(
            new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());
  }
}
