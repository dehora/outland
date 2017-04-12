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
import outland.feature.proto.FeatureCollection;
import outland.feature.proto.Group;
import outland.feature.proto.NamespaceFeature;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.ServiceException;
import outland.feature.server.auth.AccessControlSupport;
import outland.feature.server.auth.AuthPrincipal;
import outland.feature.server.features.FeatureService;
import outland.feature.server.groups.GroupService;

import static outland.feature.server.StructLog.kvp;

@Resource
@Path("/features")
public class FeatureResource {

  private final FeatureService featureService;
  private final GroupService groupService;
  private final IdempotencyChecker idempotencyChecker;
  private final AccessControlSupport accessControlSupport;
  private final URI baseURI;
  private final Headers headers;

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
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "registerFeature")
  public Response registerFeature(
      @Auth AuthPrincipal authPrincipal,
      Feature feature,
      @Context HttpHeaders httpHeaders
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    final Optional<Group> maybe = groupService.loadByKey(feature.getGroup());
    if (!maybe.isPresent()) {
      return headers.enrich(Response.status(404).entity(
          Problem.clientProblem("group_not_found", "", 404)), start).build();
    }

    accessControlSupport.throwUnlessGrantedFor(authPrincipal, maybe.get());

    URI loc = UriBuilder.fromUri(baseURI)
        .path(feature.getGroup())
        .path(feature.getKey())
        .build();

    final Optional<String> optional = idempotencyChecker.extractKey(httpHeaders);
    final boolean seen = optional.isPresent() && idempotencyChecker.seen(optional.get());
    if (optional.isPresent() && seen) {
      return headers.enrich(
          Response.created(loc).header(IdempotencyChecker.RES_HEADER, "key=" + optional.get())
              .entity(featureService.loadFeatureByKey(feature.getGroup(), feature.getKey()))
          , start).build();
    }

    Feature registered = featureService.registerFeature(feature)
        .orElseThrow(() -> new RuntimeException(""));

    return headers.enrich(Response.created(loc).entity(registered), start).build();
  }

  @POST
  @Path("/{group}/{feature_key}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "updateFeature")
  public Response updateFeature(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group,
      @PathParam("feature_key") String featureKey,
      Feature feature,
      @Context HttpHeaders httpHeaders
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    accessControlSupport.throwUnlessGrantedFor(authPrincipal, group);
    throwUnlessGroupKeyMatch(feature, group);
    throwUnlessFeatureKeyMatch(feature, featureKey);

    final Optional<String> optional = idempotencyChecker.extractKey(httpHeaders);

    if (optional.isPresent() && idempotencyChecker.seen(optional.get())) {
      return headers.enrich(
          Response.ok(featureService.loadFeatureByKey(feature.getGroup(), feature.getKey()))
              .header(IdempotencyChecker.RES_HEADER, "key=" + optional.get()),
          start).build();
    }

    Feature updated = featureService.updateFeature(group, featureKey, feature)
        .orElseThrow(() -> new RuntimeException(""));

    return headers.enrich(Response.ok(updated), start).build();
  }

  @GET
  @Path("/{group}/{feature_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getFeatureByKey")
  public Response getFeatureByKey(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group,
      @PathParam("feature_key") String featureKey
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    accessControlSupport.throwUnlessGrantedFor(authPrincipal, group);

    final Optional<Feature> feature = featureService.loadFeatureByKey(group, featureKey);

    if (feature.isPresent()) {
      return headers.enrich(Response.ok(feature.get()), start).build();
    }

    return headers.enrich(featureNotFound(), start).build();
  }

  @GET
  @Path("/{group}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getFeatures")
  public Response getFeatures(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    accessControlSupport.throwUnlessGrantedFor(authPrincipal, group);

    FeatureCollection features = featureService.loadFeatures(group);

    return this.headers.enrich(Response.ok(features), start).build();
  }

  @GET
  @Path("/{group}/feed")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getFeaturesSince")
  public Response getFeaturesSince(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group,
      @QueryParam("since") long since
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    accessControlSupport.throwUnlessGrantedFor(authPrincipal, group);

    OffsetDateTime utc =
        OffsetDateTime.ofInstant(Instant.ofEpochSecond(since), ZoneId.of("UTC").normalized());
    FeatureCollection features = featureService.loadFeaturesChangedSince(group, utc);

    return this.headers.enrich(Response.ok(features), start).build();
  }

  @POST
  @Path("/{group}/{feature_key}/namespaces")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addService")
  public Response addNamespaceFeature(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group,
      @PathParam("feature_key") String featureKey,
      NamespaceFeature namespaceFeature
  ) throws AuthenticationException {

    return postUpdate(authPrincipal, group, featureKey,
        feature -> featureService.add(feature, namespaceFeature));
  }

  @DELETE
  @Path("/{group}/{feature_key}/namespaces/{namespace}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeNamespaceFeature")
  public Response removeNamespaceFeature(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group,
      @PathParam("feature_key") String featureKey,
      @PathParam("namespace") String namespace
  ) throws AuthenticationException {

    return postUpdate(authPrincipal, group, featureKey,
        f -> featureService.removeNamespaceFeature(f.getGroup(), f.getKey(), namespace));
  }

  private Response.ResponseBuilder featureNotFound() {
    return Response.status(404).entity(Problem.clientProblem("feature_not_found", "", 404));
  }

  private Response postUpdate(
      AuthPrincipal authPrincipal, String group, String featureKey,
      Function<Feature, Feature> updater)
      throws AuthenticationException {

    final long start = System.currentTimeMillis();
    accessControlSupport.throwUnlessGrantedFor(authPrincipal, group);

    final Response.ResponseBuilder rb =
        featureService.loadFeatureByKey(group, featureKey)
            .map(feature -> Response.ok(updater.apply(feature)))
            .orElseGet(this::featureNotFound);

    return headers.enrich(rb, start).build();
  }

  private void throwUnlessFeatureKeyMatch(Feature feature, String featureKey) {
    if (!feature.getKey().equals(featureKey)) {
      throw new ServiceException(Problem.clientProblem(
          "Resource and entity feature keys do not match.",
          kvp("url_feature_key", featureKey, "data_feature_key", feature.getKey()),
          422));
    }
  }

  private void throwUnlessGroupKeyMatch(Feature feature, String group) {
    if (!feature.getGroup().equals(group)) {
      throw new ServiceException(Problem.clientProblem(
          "Resource and entity group ids do not match.",
          kvp("url_group", group, "data_group", feature.getGroup()),
          422));
    }
  }
}
