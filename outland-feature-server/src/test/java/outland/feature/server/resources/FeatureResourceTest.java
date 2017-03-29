package outland.feature.server.resources;

import com.google.gson.Gson;
import com.google.inject.Injector;
import com.google.protobuf.util.JsonFormat;
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
import org.junit.Before;
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

public class FeatureResourceTest {

  @ClassRule public static final DropwizardAppRule<ServerConfiguration> APP = ServerSuite.APP;
  private String basicPassword;
  private final Injector injector = ((ServerMain) APP.getApplication()).injector();
  private final AppService appService = injector.getInstance(AppService.class);
  private final Gson gson;

  private String seedAppOneKey = "appOne";
  private final String seedOwnerOne = "ownerOne";
  private final String seedServiceOne = "serviceOne";
  private final String seedMemberOne = "memberOne";

  private String seedAppFooKey = "appFoo";
  private final String seedOwnerFoo = "ownerFoo";
  private final String seedServiceFoo = "serviceFoo";
  private final String seedMemberFoo = "memberFoo";

  private String seedAppBarKey = "appBar";
  private final String seedOwnerBar = "ownerBar";
  private final String seedServiceBar = "serviceBar";
  private final String seedMemberBar = "memberBar";

  public FeatureResourceTest() {
    gson = new Gson();
  }

  @Before
  public void setUp() throws Exception {
    basicPassword = APP.getConfiguration().auth.basicAuthenticationKeys;

    GrantCollection.Builder builder = GrantCollection.newBuilder();
    builder.addServices(ServiceGrant.newBuilder().setKey(seedServiceOne).buildPartial());
    builder.addMembers(MemberGrant.newBuilder().setUsername(seedMemberOne).buildPartial());

    appService.registerApp(
        App.newBuilder()
            .setKey(seedAppOneKey)
            .setName("name")
            .addOwners(Owner.newBuilder().setUsername(seedOwnerOne))
            .setGranted(builder.buildPartial())
            .build()
    );

    builder = GrantCollection.newBuilder();
    builder.addServices(ServiceGrant.newBuilder().setKey(seedServiceFoo).buildPartial());
    builder.addMembers(MemberGrant.newBuilder().setUsername(seedMemberFoo).buildPartial());

    appService.registerApp(
        App.newBuilder()
            .setKey(seedAppFooKey)
            .setName("name")
            .addOwners(Owner.newBuilder().setUsername(seedOwnerFoo))
            .setGranted(builder.buildPartial())
            .build()
    );

    builder = GrantCollection.newBuilder();
    builder.addServices(ServiceGrant.newBuilder().setKey(seedServiceBar).buildPartial());
    builder.addMembers(MemberGrant.newBuilder().setUsername(seedMemberBar).buildPartial());

    appService.registerApp(
        App.newBuilder()
            .setKey(seedAppBarKey)
            .setName("name")
            .addOwners(Owner.newBuilder().setUsername(seedOwnerBar))
            .setGranted(builder.buildPartial())
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

    final String appKey = "testAuthFailures";
    final String user = "unknownuser";
    final String service = "knownservice";

    final GrantCollection.Builder grantBuilder = GrantCollection.newBuilder();

    grantBuilder.addAllServices(
        Lists.newArrayList(ServiceGrant.newBuilder().setKey(service).buildPartial()));

    appService.registerApp(
        App.newBuilder()
            .setKey("testAuthFailures")
            .setName("name")
            .addOwners(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
            .setGranted(grantBuilder.buildPartial())
            .build()
    );

    String url = createFeatureUrl();
    JerseyClient clientNoAuth = ServerSuite.client();

    // no auth header returns 401

    Response response = clientNoAuth.target(url + "/" + appKey)
        .request()
        .get();
    assertTrue(response.getStatus() == 401);
    assertTrue(response.getHeaderString("WWW-Authenticate").contains("Basic"));

    // mismatched auth header and url app ids return 401

    JerseyClient clientWithAuth = ServerSuite.client()
        //.register(
        //    new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());

    Response response1 = clientWithAuth.target(url + "/" + appKey)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, user + "/member")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response1.getStatus() == 401);

    // matching auth header and url works

    Response response2 = clientWithAuth.target(url + "/" + appKey)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, service + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response2.getStatus() == 200);
  }

  @Test
  public void testGetWithNonGrantedServiceCauses401_Basic() {

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    final String featureKey = Ulid.random();
    final String appKey = seedAppOneKey;
    final String serviceName = "unknownService";

    Response response = client.target(url + "/" + appKey + "/" + featureKey)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, serviceName + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response.getStatus() == 401);
    String jsonRes = response.readEntity(String.class);
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 401);
    assertTrue(problem.title().contains("Service grant not authenticated"));
    assertEquals(Problem.AUTH_TYPE, problem.type());
  }

  @Test
  public void testGetWithNonGrantedMemberCauses401_Basic() {

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    final String featureKey = Ulid.random();
    final String appKey = seedAppOneKey;
    final String serviceName = "unknownMember";

    Response response = client.target(url + "/" + appKey + "/" + featureKey)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, serviceName + "/member")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response.getStatus() == 401);
    String jsonRes = response.readEntity(String.class);
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 401);
    assertTrue(problem.title().contains("Member grant not authenticated"));
    assertEquals(Problem.AUTH_TYPE, problem.type());
  }

  @Test
  public void testGetWithUnknownScopedMemberCauses401_Basic() {

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    final String featureKey = Ulid.random();
    final String appKey = seedAppOneKey;
    final String serviceName = "unknownMember";
    final String grantName = "unknownKind";

    Response response = client.target(url + "/" + appKey + "/" + featureKey)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, serviceName + "/" + grantName)
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response.getStatus() == 401);
    String jsonRes = response.readEntity(String.class);
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 401);
    assertTrue(problem.title().contains("Unknown grant type"));
    assertEquals(Problem.AUTH_TYPE, problem.type());
  }

  @Test
  public void testUpdateNonMatchingAppKeyUrlAndDataCauses422() throws Exception {

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    // don't accept mismatched url and body even when authenticated

    String featureKey = Ulid.random();
    Feature feature = buildTestFeature(seedAppBarKey, featureKey);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    // auth is performed relative to the url, so match it with a granted service
    final String uri = url + "/" + seedAppOneKey + "/" + featureKey;
    final String basicAuth = seedServiceOne + "/service";

    Response response = client.target(uri)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, basicAuth)
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(response.getStatus() == 422);
    String jsonRes = response.readEntity(String.class);

    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 422);
    assertTrue(problem.detail().get().contains("url_appkey"));
    assertTrue(problem.detail().get().contains("data_appkey"));

    assertEquals(Problem.CLIENT_TYPE, problem.type());
  }

  @Test
  public void testUpdateNonMatchingFeatureUrlAndDataCauses422() throws Exception {

    final String appKey = seedAppOneKey;

    String urlFeatureKey = Ulid.random();
    String dataFeatureKey = Ulid.random();

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    Feature feature = buildTestFeature(appKey, dataFeatureKey);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    final String uri = url + "/" + appKey + "/" + urlFeatureKey;

    Response response = client.target(uri)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, seedServiceOne + "/service")
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

    String url = createFeatureUrl();

    JerseyClient client = createClient();

    final String idempotencyKey = Ulid.random();
    String featureKey = Ulid.random();
    Feature feature = buildTestFeature(seedAppOneKey, featureKey);
    String featureJson = Protobuf3Support.toJsonString(feature);

    Response post = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, seedServiceOne + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .header(IdempotencyChecker.REQ_HEADER, idempotencyKey)
        .post(Entity.entity(featureJson, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(post.getStatus() == 201);
    assertNull(post.getHeaderString(IdempotencyChecker.RES_HEADER));

    Response post2 = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, seedServiceOne + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .header(IdempotencyChecker.REQ_HEADER, idempotencyKey)
        .post(Entity.entity(featureJson, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(post2.getStatus() == 201);
    assertNotNull(post2.getHeaderString(IdempotencyChecker.RES_HEADER));
    assertTrue(
        post2.getHeaderString(IdempotencyChecker.RES_HEADER).contains("key=" + idempotencyKey));
  }

  private JerseyClient createClient() {
    return ServerSuite.client()
        .register(
            new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());
  }

  @Test
  public void testPost() throws Exception {

    final AppService instance = injector.getInstance(AppService.class);

    final String appKey = "testPostAppKey";
    final String serviceKey = "testPostService";

    GrantCollection.Builder grantBuilder = GrantCollection.newBuilder();
    final ArrayList<ServiceGrant> services = Lists.newArrayList();
    services.add(ServiceGrant.newBuilder().setKey(serviceKey).buildPartial());
    grantBuilder.addAllServices(services);

    instance.registerApp(
        App.newBuilder()
            .setKey(appKey)
            .setName("name")
            .addOwners(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
            .setGranted(grantBuilder.buildPartial())
            .build()
    );

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    String key = "testPostFeatureKey";
    Feature feature = buildTestFeature(appKey, key);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    Response post = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, serviceKey + "/service")
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

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    String featureKey = "testUpdateFeatureKey";
    Feature feature = buildTestFeature(seedAppOneKey, featureKey);
    String featureJson = Protobuf3Support.toJsonString(feature);

    // create a feature

    Response register = client.target(url)
        .request()
        // create using a service's grant
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, seedServiceOne + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(featureJson, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(register.getStatus() == 201);

    String jsonRegister = register.readEntity(String.class);
    Feature.Builder builderRegister = Feature.newBuilder();
    JsonFormat.parser().merge(jsonRegister, builderRegister);

    // check our result

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

    // turn on our feature and send an update

    Feature update = builderRegister.build().toBuilder().setState(Feature.State.on).build();
    String featureUpdateJson = Protobuf3Support.toJsonString(update);
    Response responseUpdate = client.target(url + "/" + seedAppOneKey + "/" + featureKey)
        .request()
        // this time update using a member
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, seedMemberOne + "/member")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(featureUpdateJson, MediaType.APPLICATION_JSON_TYPE));

    // check our update result

    assertTrue(responseUpdate.getStatus() == 200);

    String jsonUpdateResult = responseUpdate.readEntity(String.class);
    Feature.Builder builderUpdated = Feature.newBuilder();
    JsonFormat.parser().merge(jsonUpdateResult, builderUpdated);

    // same id
    assertEquals(id, builderUpdated.getId());
    // updated changed from create
    assertNotEquals(updated, builderUpdated.getUpdated());
    // same created
    assertEquals(created, builderUpdated.getCreated());
    // updated to enabled
    assertEquals(Feature.State.on, builderUpdated.getState());

    // now read it back from the server and double check

    Response responseGet = client.target(url + "/" + seedAppOneKey + "/" + featureKey)
        .request()
        // ask using a service's grant
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, seedServiceOne + "/service")
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

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    String key = "testMissingAppThrows404";
    String appKey = Ulid.random();
    String serviceCaller = Ulid.random(); // nb: we hit the missing app first

    Feature feature = buildTestFeature(appKey, key);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    Response post = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, serviceCaller + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(post.getStatus() == 404);
  }

  @Test
  public void testOwnerIncomplete422() throws Exception {

    final String whitelisted = "own";
    final String appKey = "testOwnerIncomplete422App";

    final AppService instance = injector.getInstance(AppService.class);

    GrantCollection.Builder grantBuilder = GrantCollection.newBuilder();
    final ArrayList<ServiceGrant> services = Lists.newArrayList();
    services.add(ServiceGrant.newBuilder().setKey(whitelisted).buildPartial());
    grantBuilder.addAllServices(services);

    instance.registerApp(
        App.newBuilder()
            .setKey(appKey)
            .setName("name")
            .addOwners(Owner.newBuilder().setName("Jayne").setUsername("jayne"))
            .setGranted(grantBuilder.buildPartial())
            .build()
    );

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    String key = "testOwnerIncomplete422Feature";
    Feature feature = Feature.newBuilder()
        .setKey(key)
        .setDescription("desc")
        .setAppkey(appKey)
        .build();
    ;
    String jsonReq = Protobuf3Support.toJsonString(feature);

    Response response = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, whitelisted + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(response.getStatus() == 422);

    String jsonRes = response.readEntity(String.class);
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 422);
    assertTrue(problem.title().contains("no_owner_for_feature"));
    assertEquals(Problem.CLIENT_TYPE, problem.type());
  }

  private String createFeatureUrl() {
    return "http://localhost:" + APP.getLocalPort() + "/features";
  }

  private Feature buildTestFeature(String appKey, String key) {
    final Owner owner =
        Owner.newBuilder().setEmail("wile.e@acme.com").setUsername("wile,e").build();
    return Feature.newBuilder()
        .setKey(key)
        .setDescription("desc")
        .setAppkey(appKey)
        .setOwner(owner)
        .build();
  }
}
