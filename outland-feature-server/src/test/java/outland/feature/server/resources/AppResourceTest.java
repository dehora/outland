package outland.feature.server.resources;

import com.google.gson.Gson;
import com.google.inject.Injector;
import com.google.protobuf.util.JsonFormat;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
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
import outland.feature.proto.App;
import outland.feature.proto.Feature;
import outland.feature.proto.GrantCollection;
import outland.feature.proto.MemberGrant;
import outland.feature.proto.Owner;
import outland.feature.proto.ServiceGrant;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.ServerMain;
import outland.feature.server.apps.AppService;
import outland.feature.server.features.Ulid;
import outland.feature.server.protobuf.Protobuf3Support;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class AppResourceTest {

  @ClassRule public static final DropwizardAppRule<ServerConfiguration> APP = ServerSuite.APP;

  private static String basicPassword = "topsecret";
  private static String authService = "knownservice";

  private final static Gson gson = new Gson();

  public AppResourceTest() {
  }

  @Test
  public void testDoubleCreatePostCauses409() throws Exception {

    final String appKey = "testDoubleCreatePost409AppKey";
    final String serviceKey = "testDoubleCreatePost409Service";

    final GrantCollection.Builder grantBuilder = GrantCollection.newBuilder();
    final ArrayList<ServiceGrant> services = Lists.newArrayList();
    services.add(ServiceGrant.newBuilder().setKey(serviceKey).buildPartial());
    grantBuilder.addAllServices(services);

    final App app = App.newBuilder()
        .setKey(appKey)
        .setName("name")
        .addOwners(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
        .setGranted(grantBuilder.buildPartial())
        .build();

    final String url = createAppUrl();
    final JerseyClient client = createClient();

    final String jsonReq = Protobuf3Support.toJsonString(app);

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
