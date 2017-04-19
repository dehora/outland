package outland.feature.server.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import outland.feature.proto.Feature;
import outland.feature.proto.Group;
import outland.feature.proto.NamespaceFeature;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.auth.AccessControlSupport;
import outland.feature.server.auth.AuthPrincipal;
import outland.feature.server.features.FeatureService;
import outland.feature.server.groups.GroupService;

@Resource
@Path("/features")
public class FeatureResource {

  private final FeatureService featureService;
  private final GroupService groupService;
  private final IdempotencyChecker idempotencyChecker;
  private final AccessControlSupport accessControlSupport;
  private final URI baseURI;
  private final Headers headers;
  private final GroupValidator groupValidator;

  @Inject
  public FeatureResource(
      FeatureService featureService,
      GroupService groupService,
      IdempotencyChecker idempotencyChecker,
      AccessControlSupport accessControlSupport,
      ServerConfiguration serviceConfiguration,
      Headers headers
  ) {
    this.featureService = featureService;
    this.groupService = groupService;
    this.idempotencyChecker = idempotencyChecker;
    this.baseURI = serviceConfiguration.baseURI;
    this.accessControlSupport = accessControlSupport;
    this.headers = headers;
    this.groupValidator = new GroupValidator();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "registerFeature")
  public Response registerFeature(
      @Auth AuthPrincipal principal,
      Feature feature,
      @Context HttpHeaders httpHeaders
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    final Optional<Group> maybe = groupService.loadByKey(feature.getGroup());
    if (!maybe.isPresent()) {
      return notFound(start);
    }

    accessControlSupport.throwUnlessGrantedFor(principal, maybe.get());

    final URI loc = locationUrl(feature);

    final Optional<String> maybeSeen = idempotencyChecker.extractKey(httpHeaders);
    if (maybeSeen.isPresent() && idempotencyChecker.seen(maybeSeen.get())) {
      return respondAlreadyCreated(feature, start, loc, maybeSeen);
    }

    return headers.enrich(
        Response.created(loc).entity(featureService.registerFeature(feature)), start).build();
  }

  @POST
  @Path("/{group}/{feature_key}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "updateFeature")
  public Response updateFeature(
      @Auth AuthPrincipal principal,
      @PathParam("group") String group,
      @PathParam("feature_key") String featureKey,
      Feature feature,
      @Context HttpHeaders httpHeaders
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    grantedGuard(principal, group);
    groupValidGuard(group, featureKey, feature);

    final Optional<String> maybeSeen = idempotencyChecker.extractKey(httpHeaders);
    if (maybeSeen.isPresent() && idempotencyChecker.seen(maybeSeen.get())) {
      return alreadyUpdated(feature, start, maybeSeen);
    }

    return headers.enrich(Response.ok(update(group, feature)), start).build();
  }

  @GET
  @Path("/{group}/{feature_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getFeatureByKey")
  public Response getFeatureByKey(
      @Auth AuthPrincipal principal,
      @PathParam("group") String group,
      @PathParam("feature_key") String featureKey
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    grantedGuard(principal, group);
    return headers.enrich(
        featureService.loadFeatureByKey(group, featureKey)
            .map(Response::ok)
            .orElseGet(this::featureNotFound), start).build();
  }

  @GET
  @Path("/{group}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getFeatures")
  public Response getFeatures(
      @Auth AuthPrincipal principal,
      @PathParam("group") String group
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    grantedGuard(principal, group);
    return this.headers.enrich(Response.ok(featureService.loadFeatures(group)), start).build();
  }

  @GET
  @Path("/{group}/feed")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getFeaturesSince")
  public Response getFeaturesSince(
      @Auth AuthPrincipal principal,
      @PathParam("group") String group,
      @QueryParam("since") long since
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    grantedGuard(principal, group);
    return this.headers.enrich(
        Response.ok(featureService.loadChangedSince(group, toOffset(since))), start).build();
  }

  @POST
  @Path("/{group}/{feature_key}/namespaces")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addNamespaceFeature")
  public Response addNamespaceFeature(
      @Auth AuthPrincipal principal,
      @PathParam("group") String group,
      @PathParam("feature_key") String featureKey,
      NamespaceFeature namespaceFeature
  ) throws AuthenticationException {

    return postUpdate(principal, group, featureKey, f -> featureService.add(f, namespaceFeature));
  }

  @POST
  @Path("/{group}/{feature_key}/namespaces/{namespace}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "updateNamespaceFeature")
  public Response updateNamespaceFeature(
      @Auth AuthPrincipal principal,
      @PathParam("group") String group,
      @PathParam("feature_key") String featureKey,
      @PathParam("namespace") String namespace,
      NamespaceFeature namespaceFeature
  ) throws AuthenticationException {

    return postUpdate(principal, group, featureKey,
        f -> featureService.updateNamespaceFeature(f, namespaceFeature));
  }

  @DELETE
  @Path("/{group}/{feature_key}/namespaces/{namespace}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeNamespaceFeature")
  public Response removeNamespaceFeature(
      @Auth AuthPrincipal principal,
      @PathParam("group") String group,
      @PathParam("feature_key") String featureKey,
      @PathParam("namespace") String namespace
  ) throws AuthenticationException {

    return postUpdate(principal, group, featureKey,
        f -> featureService.removeNamespaceFeature(f.getGroup(), f.getKey(), namespace));
  }

  private Response postUpdate(
      AuthPrincipal authPrincipal, String group, String featureKey,
      Function<Feature, Feature> updater)
      throws AuthenticationException {

    final long start = System.currentTimeMillis();
    grantedGuard(authPrincipal, group);

    final Response.ResponseBuilder rb =
        featureService.loadFeatureByKey(group, featureKey)
            .map(feature -> Response.ok(updater.apply(feature)))
            .orElseGet(this::featureNotFound);

    return headers.enrich(rb, start).build();
  }

  private OffsetDateTime toOffset(@QueryParam("since") long since) {
    return OffsetDateTime.ofInstant(Instant.ofEpochSecond(since), ZoneId.of("UTC").normalized());
  }

  private Response.ResponseBuilder featureNotFound() {
    return Response.status(404).entity(Problem.clientProblem("feature_not_found", "", 404));
  }

  private Optional<Feature> loadFeature(Feature feature) {
    return featureService.loadFeatureByKey(feature.getGroup(), feature.getKey());
  }

  private Response respondAlreadyCreated(
      Feature feature, long start, URI loc, Optional<String> maybeSeen) {
    return headers.enrich(
        Response.created(loc)
            .header(IdempotencyChecker.RES_HEADER, "key=" + maybeSeen.get())
            .entity(loadFeature(feature)), start).build();
  }

  private Response alreadyUpdated(Feature feature, long start, Optional<String> maybeSeen) {
    return headers.enrich(
        Response.ok(loadFeature(feature))
            .header(IdempotencyChecker.RES_HEADER, "key=" + maybeSeen.get()),
        start).build();
  }

  private Response notFound(long start) {
    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("group_not_found", "", 404)), start).build();
  }

  private URI locationUrl(Feature feature) {
    return UriBuilder.fromUri(baseURI)
        .path("features")
        .path(feature.getGroup())
        .path(feature.getKey())
        .build();
  }

  private Feature update(@PathParam("group") String group, Feature feature) {
    return featureService.updateFeature(group, feature.getKey(), feature)
        .orElseThrow(() -> new RuntimeException(""));
  }

  private void grantedGuard(@Auth AuthPrincipal principal, @PathParam("group") String group)
      throws AuthenticationException {
    accessControlSupport.throwUnlessGrantedFor(principal, group);
  }

  private void groupValidGuard(String group, String featureKey, Feature feature) {
    groupValidator.throwUnlessGroupKeyMatch(feature, group);
    groupValidator.throwUnlessFeatureKeyMatch(feature, featureKey);
  }
}
