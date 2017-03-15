package outland.feature.server.resources;

import com.google.gson.Gson;
import com.google.inject.Injector;
import com.google.protobuf.util.JsonFormat;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import outland.feature.proto.App;
import outland.feature.proto.Feature;
import outland.feature.proto.Owner;
import outland.feature.proto.Service;
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

public class FeatureResourceTest {

  @ClassRule public static final DropwizardAppRule<ServerConfiguration> APP = ServerSuite.APP;
  private String basicPassword;
  private final Injector injector = ((ServerMain)APP.getApplication()).injector();
  private final AppService appService = injector.getInstance(AppService.class);

  @Before
  public void setUp() throws Exception {
    basicPassword = APP.getConfiguration().auth.basicAuthenticationKeys;
    appService.registerApp(
        App.newBuilder()
            .setKey("own")
            .addServices(Service.newBuilder().setKey("own"))
            .build()
    );

    appService.registerApp(
        App.newBuilder()
            .setKey("foo")
            .addOwners(Owner.newBuilder().setUsername("own"))
            .addServices(Service.newBuilder().setKey("foo"))
            .build()
    );

    appService.registerApp(
        App.newBuilder()
            .setKey("bar")
            .addServices(Service.newBuilder().setKey("bar"))
            .build()
    );
  }

  @Test
  public void testAuthFailures() {

    /*
    The test server is configured to reflect the bearer token as the found app id and accept it
    without performing a token check or lookup. These test the interface and not an underlying
    auth mechanism
     */

    final String appId = "testAuthFailures";
    final String user = "unknownuser";
    final String service = "knownservice";

    appService.registerApp(
        App.newBuilder()
            .setKey("testAuthFailures")
            .addServices(Service.newBuilder().setKey(service))
            .build()
    );

    String url = "http://localhost:" + APP.getLocalPort() + "/features";
    JerseyClient clientNoAuth = ServerSuite.client();
    
    // no auth header returns 401

    Response response = clientNoAuth.target(url + "/" + appId)
        .request()
        .get();
    assertTrue(response.getStatus() == 401);
    assertTrue(response.getHeaderString("WWW-Authenticate").contains("Basic"));

    // mismatched auth header and url app ids return 401

    JerseyClient clientWithAuth = ServerSuite.client()
        //.register(
        //    new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());

    Response response1 = clientWithAuth.target(url + "/" + appId)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, user+"/owner")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response1.getStatus() == 401);

    // matching auth header and url works

    Response response2 = clientWithAuth.target(url + "/" + appId)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, service + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response2.getStatus() == 200);
  }

  @Test
  public void testGetWithNonMatchingAppIdAndBearerCauses401() {

    String url = "http://localhost:" + APP.getLocalPort() + "/features";
    JerseyClient client = ServerSuite.client()
        .register(
            new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());


    final String fk = Ulid.random();

    Response response = client.target(url + "/" + "foo" + "/" + fk)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "nope/owner")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response.getStatus() == 401);
    String jsonRes = response.readEntity(String.class);
    Gson gson = new Gson();
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 401);
    assertTrue(problem.title().contains("Membership not authenticated for request"));
    assertEquals(Problem.AUTH_TYPE, problem.type());
  }

  @Test
  public void testUpdateNonMatchingAppIdUrlAndDataCauses422() throws Exception {

    final String appId = "foo";
    final String appId2 = "bar";

    String urlKey = Ulid.random();

    String url = "http://localhost:" + APP.getLocalPort() + "/features";
    JerseyClient client = ServerSuite.client()
        .register(
            new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());


    Feature feature = buildTestFeature(appId, urlKey);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    // the url and bearer need to be the same to pass the auth check

    final String uri = url + "/" + appId2 + "/" + urlKey;
    System.out.println(uri);
    Response response = client.target(uri)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, appId2 +"/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(response.getStatus() == 422);
    String jsonRes = response.readEntity(String.class);
    Gson gson = new Gson();
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 422);
    assertTrue(problem.detail().get().contains("url_app_id"));
    assertTrue(problem.detail().get().contains("data_app_id"));

    assertEquals(Problem.CLIENT_TYPE, problem.type());
  }

  @Test
  public void testUpdateNonMatchingFeatureUrlAndDataCauses422() throws Exception {

    final String appId = "foo";

    String urlKey = Ulid.random();
    String featureKey = Ulid.random();

    String url = "http://localhost:" + APP.getLocalPort() + "/features";
    JerseyClient client = ServerSuite.client()
        .register(
            new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());


    Feature feature = buildTestFeature(appId, featureKey);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    final String uri = url + "/" + appId + "/" + urlKey;
    System.out.println(uri);
    Response response = client.target(uri)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, appId+"/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(response.getStatus() == 422);
    String jsonRes = response.readEntity(String.class);
    Gson gson = new Gson();
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 422);
    assertTrue(problem.detail().get().contains("url_feature_key"));
    assertTrue(problem.detail().get().contains("data_feature_key"));

    assertEquals(Problem.CLIENT_TYPE, problem.type());
  }

  @Test
  public void testIdempotentPost() throws Exception {

    String url = "http://localhost:" + APP.getLocalPort() + "/features";

    JerseyClient client = ServerSuite.client()
        //.register(
        //    new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());

    final String ik = Ulid.random();
    String featureKey = Ulid.random();
    Feature feature = buildTestFeature("own", featureKey);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    Response post = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "own"+"/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .header(IdempotencyChecker.REQ_HEADER, ik)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(post.getStatus() == 201);
    assertNull(post.getHeaderString(IdempotencyChecker.RES_HEADER));

    Response post2 = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "own"+"/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .header(IdempotencyChecker.REQ_HEADER, ik)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(post2.getStatus() == 201);
    assertNotNull(post2.getHeaderString(IdempotencyChecker.RES_HEADER));
    assertTrue(post2.getHeaderString(IdempotencyChecker.RES_HEADER).contains("key=" + ik));
  }

  @Test
  public void testPost() throws Exception {

    final AppService instance = injector.getInstance(AppService.class);
    instance.registerApp(
        App.newBuilder()
            .setKey("own")
            .addServices(Service.newBuilder().setKey("own"))
            .build()
    );

    String url = "http://localhost:" + APP.getLocalPort() + "/features";
    JerseyClient client = ServerSuite.client()
        .register(
            new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());


    String key = "release_the_kraken";
    Feature feature = buildTestFeature("own", key);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    Response post = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "own"+"/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(post.getStatus() == 201);
    assertTrue(post.getHeaderString("Location") != null);

    String jsonRes = post.readEntity(String.class);
    Feature.Builder builder = Feature.newBuilder();
    JsonFormat.parser().merge(jsonRes, builder);
    Feature featJson = builder.build();
    assertEquals(key, featJson.getKey());
    assertTrue(featJson.getId().startsWith("feat_"));

    assertTrue(featJson.getVersion() != null);
    final String id = featJson.getVersion().getId();
    assertTrue(id.startsWith("ver_"));
    assertTrue(Ulid.isValid(id.substring(4)));
    assertEquals(0L, featJson.getVersion().getCounter());
    assertTrue(0L < featJson.getVersion().getTimestamp());
  }

  @Test
  public void testUpdate() throws Exception {

    String url = "http://localhost:" + APP.getLocalPort() + "/features";
    JerseyClient client = ServerSuite.client()
        .register(
            new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());


    String key = "feature_one";
    Feature feature = buildTestFeature("own", key);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    Response register = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "own"+"/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(register.getStatus() == 201);

    String jsonRegister = register.readEntity(String.class);
    Feature.Builder builderRegister = Feature.newBuilder();
    JsonFormat.parser().merge(jsonRegister, builderRegister);

    String id = builderRegister.getId();
    String created = builderRegister.getCreated();
    String updated = builderRegister.getUpdated();
    Feature.State state = builderRegister.getState();

    // same id
    assertNotNull(id);
    // updated
    assertNotNull(updated);
    // created
    assertNotNull(created);
    // disabled by default
    assertEquals(Feature.State.off, state);

    Feature update = builderRegister.build().toBuilder().setState(Feature.State.on).build();
    String jsonUpdate = Protobuf3Support.toJsonString(update);

    Thread.sleep(1001);

    Response responseUpdate = client.target(url + "/" + "own" + "/" + key)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "own"+"/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonUpdate, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(responseUpdate.getStatus() == 200);

    String jsonUpdated = responseUpdate.readEntity(String.class);
    Feature.Builder builderUpdated = Feature.newBuilder();
    JsonFormat.parser().merge(jsonUpdated, builderUpdated);

    // same id
    assertEquals(id, builderUpdated.getId());
    // updated changed from create
    assertNotEquals(updated, builderUpdated.getUpdated());
    // same created
    assertEquals(created, builderUpdated.getCreated());
    // updated to enabled
    assertEquals(Feature.State.on, builderUpdated.getState());

    Response responseGet = client.target(url + "/" + "own" + "/" + key)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "own"+"/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    String jsonResponseGet = responseGet.readEntity(String.class);
    Feature.Builder responseGetBuilder = Feature.newBuilder();
    JsonFormat.parser().merge(jsonResponseGet, responseGetBuilder);
    Feature featResponseGet = responseGetBuilder.build();
    assertEquals(id, featResponseGet.getId());

    // same id
    assertEquals(id, featResponseGet.getId());
    // updated changed from create
    assertNotEquals(updated, featResponseGet.getUpdated());
    // updated sames as last update
    assertEquals(builderUpdated.getUpdated(), featResponseGet.getUpdated());
    // same created
    assertEquals(created, featResponseGet.getCreated());
    // still enabled
    assertEquals(Feature.State.on, featResponseGet.getState());
  }

  @Test
  public void testMissingAppThrows404() throws Exception {

    String url = "http://localhost:" + APP.getLocalPort() + "/features";
    JerseyClient client = ServerSuite.client()
        .register(
            new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());


    String key = "testMissingAppThrows404";
    String appId = Ulid.random();
    String serviceCaller = "own";

    Feature feature = buildTestFeature(appId, key);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    Response post = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, serviceCaller+"/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(post.getStatus() == 404);
  }

  private Feature buildTestFeature(String appId, String key) {
    return Feature.newBuilder()
        .setKey(key)
        .setDescription("desc")
        .setAppId(appId)
        .build();
  }
}
