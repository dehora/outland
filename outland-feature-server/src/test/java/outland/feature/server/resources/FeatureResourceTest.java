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
import outland.feature.proto.AccessCollection;
import outland.feature.proto.Feature;
import outland.feature.proto.Group;
import outland.feature.proto.MemberAccess;
import outland.feature.proto.Owner;
import outland.feature.proto.OwnerCollection;
import outland.feature.proto.ServiceAccess;
import outland.feature.proto.Status;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.ServerMain;
import outland.feature.server.features.Ulid;
import outland.feature.server.groups.GroupService;
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
  private static AtomicBoolean setup = new AtomicBoolean(false);
  private String basicPassword = "topsecret";
  private Gson gson;

  private final String seedGroupOneKey = "appOne";
  private final String seedServiceOne = "serviceOne";
  private final String seedMemberOne = "memberOne";
  private final String seedGroupBarKey = "appBar";
  private final String seedServiceBar = "serviceBar";

  private Injector injector;
  private GroupService groupService;

  public FeatureResourceTest() {
    gson = new Gson();
    ServerMain application = APP.getApplication();
    injector = application.injector();
    groupService = injector.getInstance(GroupService.class);

    /*
    create our seeds just once. we're not using a before class because we need APP to be
    fully instantiated before calling its injector above.
     */
    if(setup.compareAndSet(false, true)) {
      setUp();
    }
  }

  public void setUp() {
    AccessCollection.Builder builder = AccessCollection.newBuilder();
    builder.addServices(ServiceAccess.newBuilder().setKey(seedServiceOne).buildPartial());
    builder.addMembers(MemberAccess.newBuilder().setUsername(seedMemberOne).buildPartial());

    String seedOwnerOne = "ownerOne";
    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .addItems(Owner.newBuilder().setUsername(seedOwnerOne));

    groupService.register(
        Group.newBuilder()
            .setKey(seedGroupOneKey)
            .setName("name")
            .setOwners(oc)
            .setGranted(builder.buildPartial())
            .build()
    );

    builder = AccessCollection.newBuilder();
    String seedServiceFoo = "serviceFoo";
    builder.addServices(ServiceAccess.newBuilder().setKey(seedServiceFoo).buildPartial());
    String seedMemberFoo = "memberFoo";
    builder.addMembers(MemberAccess.newBuilder().setUsername(seedMemberFoo).buildPartial());

    String seedOwnerFoo = "ownerFoo";
    oc = OwnerCollection.newBuilder()
        .addItems(Owner.newBuilder().setUsername(seedOwnerFoo));

    String seedGroupFooKey = "appFoo";
    groupService.register(
        Group.newBuilder()
            .setKey(seedGroupFooKey)
            .setName("name")
            .setOwners(oc)
            .setGranted(builder.buildPartial())
            .build()
    );

    builder = AccessCollection.newBuilder();
    builder.addServices(ServiceAccess.newBuilder().setKey(seedServiceBar).buildPartial());
    String seedMemberBar = "memberBar";
    builder.addMembers(MemberAccess.newBuilder().setUsername(seedMemberBar).buildPartial());

    String seedOwnerBar = "ownerBar";
    oc = OwnerCollection.newBuilder()
        .addItems(Owner.newBuilder().setUsername(seedOwnerBar));

    groupService.register(
        Group.newBuilder()
            .setKey(seedGroupBarKey)
            .setName("name")
            .setOwners(oc)
            .setGranted(builder.buildPartial())
            .build()
    );
  }

  @Test
  public void testAuthFailures() {

    /*
    The test server is configured to reflect the bearer token as the found group id and accept it
    without performing a token check or lookup. These test the interface and not an underlying
    auth mechanism
     */

    final String groupKey = "testAuthFailures";
    final String user = "unknownuser";
    final String service = "knownservice";

    final AccessCollection.Builder accessBuilder = AccessCollection.newBuilder();

    accessBuilder.addAllServices(
        Lists.newArrayList(ServiceAccess.newBuilder().setKey(service).buildPartial()));

    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .addItems(Owner.newBuilder().setName("Jayne").setUsername("jayne"));

    groupService.register(
        Group.newBuilder()
            .setKey("testAuthFailures")
            .setName("name")
            .setOwners(oc)
            .setGranted(accessBuilder.buildPartial())
            .build()
    );

    String url = createFeatureUrl();
    JerseyClient clientNoAuth = ServerSuite.client();

    // no auth header returns 401

    Response response = clientNoAuth.target(url + "/" + groupKey)
        .request()
        .get();
    assertTrue(response.getStatus() == 401);
    assertTrue(response.getHeaderString("WWW-Authenticate").contains("Basic"));

    // mismatched auth header and url group ids return 401

    JerseyClient clientWithAuth = ServerSuite.client()
        //.register(
        //    new LoggingFeature(Logger.getLogger(getClass().getName()), Level.INFO, null, null))
        .register(HttpAuthenticationFeature.universalBuilder().build());

    Response response1 = clientWithAuth.target(url + "/" + groupKey)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, user + "/member")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response1.getStatus() == 401);

    // matching auth header and url works

    Response response2 = clientWithAuth.target(url + "/" + groupKey)
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
    final String groupKey = seedGroupOneKey;
    final String serviceName = "unknownService";

    Response response = client.target(url + "/" + groupKey + "/" + featureKey)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, serviceName + "/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response.getStatus() == 401);
    String jsonRes = response.readEntity(String.class);
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 401);
    assertTrue(problem.title().contains("invalid_or_missing_credentials"));
    assertEquals(Problem.AUTH_TYPE, problem.type());
  }

  @Test
  public void testGetWithNonGrantedMemberCauses401_Basic() {

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    final String featureKey = Ulid.random();
    final String groupKey = seedGroupOneKey;
    final String serviceName = "unknownMember";

    Response response = client.target(url + "/" + groupKey + "/" + featureKey)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, serviceName + "/member")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response.getStatus() == 401);
    String jsonRes = response.readEntity(String.class);
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 401);
    assertTrue(problem.title().contains("Member not authenticated"));
    assertEquals(Problem.AUTH_TYPE, problem.type());
  }

  @Test
  public void testGetWithUnknownScopedMemberCauses401_Basic() {

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    final String featureKey = Ulid.random();
    final String groupKey = seedGroupOneKey;
    final String serviceName = "unknownMember";
    final String accessName = "unknownKind";

    Response response = client.target(url + "/" + groupKey + "/" + featureKey)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, serviceName + "/" + accessName)
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .get();

    assertTrue(response.getStatus() == 401);
    String jsonRes = response.readEntity(String.class);
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 401);
    assertTrue(problem.title().contains("Unknown access type"));
    assertEquals(Problem.AUTH_TYPE, problem.type());
  }

  @Test
  public void testUpdateNonMatchingGroupKeyUrlAndDataCauses422() throws Exception {

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    // don't accept mismatched url and body even when authenticated

    String featureKey = Ulid.random();
    Feature feature = buildTestFeature(seedGroupBarKey, featureKey);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    // auth is performed relative to the url, so match it with a granted service
    final String uri = url + "/" + seedGroupOneKey + "/" + featureKey;
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
    assertTrue(problem.detail().get().contains("url_group"));
    assertTrue(problem.detail().get().contains("data_group"));

    assertEquals(Problem.CLIENT_TYPE, problem.type());
  }

  @Test
  public void testUpdateNonMatchingFeatureUrlAndDataCauses422() throws Exception {

    final String groupKey = seedGroupOneKey;

    String urlFeatureKey = Ulid.random();
    String dataFeatureKey = Ulid.random();

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    Feature feature = buildTestFeature(groupKey, dataFeatureKey);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    final String uri = url + "/" + groupKey + "/" + urlFeatureKey;

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

    String url = createFeatureUrl() + "/" + seedGroupOneKey;

    JerseyClient client = createClient();

    final String idempotencyKey = Ulid.random();
    String featureKey = Ulid.random();
    Feature feature = buildTestFeature(seedGroupOneKey, featureKey);
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

    final GroupService instance = injector.getInstance(GroupService.class);

    final String groupKey = "testPostGroupKey";
    final String serviceKey = "testPostService";

    AccessCollection.Builder accessBuilder = AccessCollection.newBuilder();
    final ArrayList<ServiceAccess> services = Lists.newArrayList();
    services.add(ServiceAccess.newBuilder().setKey(serviceKey).buildPartial());
    accessBuilder.addAllServices(services);

    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .addItems(Owner.newBuilder().setName("Jayne").setUsername("jayne"));

    instance.register(
        Group.newBuilder()
            .setKey(groupKey)
            .setName("name")
            .setOwners(oc)
            .setGranted(accessBuilder.buildPartial())
            .build()
    );

    String url = createFeatureUrl() + "/"+ groupKey;
    JerseyClient client = createClient();

    String key = "testPostFeatureKey";
    Feature feature = buildTestFeature(groupKey, key);
    String jsonReq = Protobuf3Support.toJsonString(feature);

    Response post = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "testPostService/service")
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
    assertEquals(0L, featJson.getVersion().getCounter());
    assertTrue(0L < featJson.getVersion().getTimestamp());
  }

  @Test
  public void testUpdate() throws Exception {


    JerseyClient client = createClient();

    String featureKey = "testUpdateFeatureKey";
    Feature feature = buildTestFeature(seedGroupOneKey, featureKey);
    String featureJson = Protobuf3Support.toJsonString(feature);

    String url = createFeatureUrl() + "/" + seedGroupOneKey;

    // create a feature

    Response register = client.target(url)
        .request()
        // create using a service's access
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
    Status state = builderRegister.getStatus();

    // same id
    assertNotNull(id);
    // updated
    assertNotNull(updated);
    // created
    assertNotNull(created);
    // disabled by default
    assertEquals(Status.off, state);

    // turn on our feature and send an update

    Feature update = builderRegister.build().toBuilder().setStatus(Status.on).build();
    String featureUpdateJson = Protobuf3Support.toJsonString(update);
    Response responseUpdate = client.target(url + "/" + featureKey)
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
    assertEquals(Status.on, builderUpdated.getStatus());

    // now read it back from the server and double check

    Response responseGet = client.target(url + "/" + featureKey)
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
    assertEquals(Status.on, featResponseGet.getStatus());
  }

  @Test
  public void testMissingGroupThrows404() throws Exception {

    String url = createFeatureUrl();
    JerseyClient client = createClient();

    String key = "testMissingGroupThrows404";
    String groupKey = Ulid.random();
    String serviceCaller = seedServiceBar;

    Feature feature = buildTestFeature(groupKey, key);
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

    final String whitelisted = "whitelisted";
    final String groupKey = "testOwnerIncomplete422Group";

    final GroupService instance = injector.getInstance(GroupService.class);

    AccessCollection.Builder accessBuilder = AccessCollection.newBuilder();
    final ArrayList<ServiceAccess> services = Lists.newArrayList();
    services.add(ServiceAccess.newBuilder().setKey(whitelisted).buildPartial());
    accessBuilder.addAllServices(services);

    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .addItems(Owner.newBuilder().setName("Jayne").setUsername("jayne"));

    instance.register(
        Group.newBuilder()
            .setKey(groupKey)
            .setName("name")
            .setOwners(oc)
            .setGranted(accessBuilder.buildPartial())
            .build()
    );

    String url = createFeatureUrl() +"/" + groupKey;
    JerseyClient client = createClient();

    String key = "testOwnerIncomplete422Feature";
    Feature feature = Feature.newBuilder()
        .setKey(key)
        .setDescription("desc")
        .setGroup(groupKey)
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

  @Test
  public void testDoubleCreatePostCauses409() throws Exception {

    final GroupService instance = injector.getInstance(GroupService.class);
    final String groupKey = "testDoubleCreatePostCauses409GroupKey";
    final String serviceKey = "testDoubleCreatePostCauses409Service";

    final AccessCollection.Builder accessBuilder = AccessCollection.newBuilder();
    final ArrayList<ServiceAccess> services = Lists.newArrayList();
    services.add(ServiceAccess.newBuilder().setKey(serviceKey).buildPartial());
    accessBuilder.addAllServices(services);

    OwnerCollection.Builder oc = OwnerCollection.newBuilder()
        .addItems(Owner.newBuilder().setName("Jayne").setUsername("jayne"));

    instance.register(
        Group.newBuilder()
            .setKey(groupKey)
            .setName("name")
            .setOwners(oc)
            .setGranted(accessBuilder.buildPartial())
            .build()
    );

    final String url = createFeatureUrl() + "/" + groupKey;
    final JerseyClient client = createClient();

    final String key = "testDoubleCreatePostCauses409FeatureKey";
    final Feature feature = buildTestFeature(groupKey, key);
    final String jsonReq = Protobuf3Support.toJsonString(feature);

    final Response post = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "whitelisted/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(post.getStatus() == 201);
    assertTrue(post.getHeaderString("Location") != null);

    Response postAgain = client.target(url)
        .request()
        .property(HTTP_AUTHENTICATION_BASIC_USERNAME, "whitelisted/service")
        .property(HTTP_AUTHENTICATION_BASIC_PASSWORD, basicPassword)
        .post(Entity.entity(jsonReq, MediaType.APPLICATION_JSON_TYPE));

    assertTrue(postAgain.getStatus() == 409);
    final String jsonRes = postAgain.readEntity(String.class);
    final Problem problem = gson.fromJson(jsonRes, Problem.class);
    assertTrue(problem.status() == 409);
    assertTrue(problem.title().contains("conflict_feature_already_exists"));
  }

  private String createFeatureUrl() {
    return "http://localhost:" + APP.getLocalPort() + "/features";
  }

  private Feature buildTestFeature(String groupKey, String key) {
    final Owner owner =
        Owner.newBuilder().setEmail("wile.e@acme.com").setUsername("wile,e").build();
    return Feature.newBuilder()
        .setKey(key)
        .setDescription("desc")
        .setGroup(groupKey)
        .setOwner(owner)
        .build();
  }
}
