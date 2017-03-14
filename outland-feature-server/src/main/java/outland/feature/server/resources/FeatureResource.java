package outland.feature.server.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
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
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.ServiceException;
import outland.feature.server.apps.AppService;
import outland.feature.server.auth.AuthPrincipal;
import outland.feature.server.features.FeatureService;

import static outland.feature.server.StructLog.kvp;

@Resource
@Path("/features")
public class FeatureResource {

  private final FeatureService featureService;
  private final AppService appService;
  private final IdempotencyChecker idempotencyChecker;
  private final URI baseURI;
  private final Headers headers;

  @Inject
  public FeatureResource(
      FeatureService featureService,
      AppService appService,
      IdempotencyChecker idempotencyChecker,
      ServerConfiguration serviceConfiguration,
      Headers headers
  ) {
    this.featureService = featureService;
    this.appService = appService;
    this.idempotencyChecker = idempotencyChecker;
    this.baseURI = serviceConfiguration.baseURI;
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

    throwUnlessMember(authPrincipal, feature.getAppId());

    URI loc = UriBuilder.fromUri(baseURI)
        .path(feature.getAppId())
        .path(feature.getKey())
        .build();

    final Optional<String> optional = idempotencyChecker.extractKey(httpHeaders);
    final boolean seen = optional.isPresent() && idempotencyChecker.seen(optional.get());
    if (optional.isPresent() && seen) {
      return headers.enrich(
          Response.created(loc).header(IdempotencyChecker.RES_HEADER, "key=" + optional.get())
              .entity(featureService.loadFeatureByKey(feature.getAppId(), feature.getKey()))
          , start).build();
    }

    Feature registered = featureService.registerFeature(feature)
        .orElseThrow(() -> new RuntimeException(""));

    return headers.enrich(Response.created(loc).entity(registered), start).build();
  }

  @POST
  @Path("/{app_id}/{feature_key}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "updateFeature")
  public Response updateFeature(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_id") String appId,
      @PathParam("feature_key") String featureKey,
      Feature feature,
      @Context HttpHeaders httpHeaders
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    throwUnlessMember(authPrincipal, appId);
    throwUnlessAppIdMatch(feature, appId);
    throwUnlessFeatureKeyMatch(feature, featureKey);

    final Optional<String> optional = idempotencyChecker.extractKey(httpHeaders);

    if (optional.isPresent() && idempotencyChecker.seen(optional.get())) {
      return headers.enrich(
          Response.ok(featureService.loadFeatureByKey(feature.getAppId(), feature.getKey()))
              .header(IdempotencyChecker.RES_HEADER, "key=" + optional.get()),
          start).build();
    }

    Feature updated = featureService.updateFeature(appId, featureKey, feature)
        .orElseThrow(() -> new RuntimeException(""));

    return headers.enrich(Response.ok(updated), start).build();
  }

  @GET
  @Path("/{app_id}/{feature_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getFeatureByKey")
  public Response getFeatureByKey(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_id") String appId,
      @PathParam("feature_key") String featureKey
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    throwUnlessMember(authPrincipal, appId);

    final Optional<Feature> feature = featureService.loadFeatureByKey(appId, featureKey);

    if(feature.isPresent()) {
      return headers.enrich(Response.ok(feature.get()), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("feature_not_found", "", 404)), start).build();
  }

  @GET
  @Path("/{app_id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getFeatures")
  public Response getFeatures(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_id") String appId
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    throwUnlessMember(authPrincipal, appId);

    FeatureCollection features = featureService.loadFeatures(appId);

    return this.headers.enrich(Response.ok(features), start).build();
  }

  @GET
  @Path("/{app_id}/feed")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getFeaturesSince")
  public Response getFeaturesSince(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_id") String appId,
      @QueryParam("since") long since
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    throwUnlessMember(authPrincipal, appId);

    OffsetDateTime utc =
        OffsetDateTime.ofInstant(Instant.ofEpochSecond(since), ZoneId.of("UTC").normalized());
    FeatureCollection features = featureService.loadFeaturesChangedSince(appId, utc);

    return this.headers.enrich(Response.ok(features), start).build();
  }

  private void throwUnlessMember(AuthPrincipal authPrincipal, String appKey) throws AuthenticationException {

    boolean member;
    if(authPrincipal.type().equals(AppService.OWNER)) {
      member = appService.appHasOwner(appKey, authPrincipal.identifier());
    } else {
      member = appService.appHasService(appKey, authPrincipal.identifier());
    }

    if (! member) {
      throw new AuthenticationException("Membership not authenticated for request");
    }
  }

  private void throwUnlessFeatureKeyMatch(Feature feature, String featureKey) {
    if (!feature.getKey().equals(featureKey)) {
      throw new ServiceException(Problem.clientProblem(
              "Resource and entity feature keys do not match.",
              kvp("url_feature_key", featureKey, "data_feature_key", feature.getKey()),
              422));
    }
  }

  private void throwUnlessAppIdMatch(Feature feature, String appId) {
    if (!feature.getAppId().equals(appId)) {
      throw new ServiceException(Problem.clientProblem(
          "Resource and entity app ids do not match.",
          kvp("url_app_id", appId, "data_app_id", feature.getAppId()),
          422));
    }
  }
}
