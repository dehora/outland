package outland.feature.server.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import java.net.URI;
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
import outland.feature.proto.App;
import outland.feature.proto.MemberGrant;
import outland.feature.proto.Owner;
import outland.feature.proto.ServiceGrant;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.apps.AppService;
import outland.feature.server.auth.AccessControlSupport;
import outland.feature.server.auth.AuthPrincipal;

@Resource
@Path("/apps")
public class AppResource {

  private final AppService appService;
  private final IdempotencyChecker idempotencyChecker;
  private final AccessControlSupport accessControlSupport;
  private final URI baseURI;
  private final Headers headers;

  @Inject
  public AppResource(
      AppService appService,
      IdempotencyChecker idempotencyChecker,
      AccessControlSupport accessControlSupport,
      ServerConfiguration serviceConfiguration,
      Headers headers
  ) {
    this.appService = appService;
    this.idempotencyChecker = idempotencyChecker;
    this.accessControlSupport = accessControlSupport;
    this.baseURI = serviceConfiguration.baseURI;
    ;
    this.headers = headers;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Timed(name = "registerApp")
  public Response registerApp(
      @Auth AuthPrincipal authPrincipal,
      App app,
      @Context HttpHeaders httpHeaders
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    accessControlSupport.throwUnlessGrantedForApp(authPrincipal, app);

    URI loc = UriBuilder.fromUri(baseURI)
        .path(app.getKey())
        .build();

    final Optional<String> optional = idempotencyChecker.extractKey(httpHeaders);
    final boolean seen = optional.isPresent() && idempotencyChecker.seen(optional.get());
    if (seen) {
      return headers.enrich(
          Response.ok(appService.loadAppByKey(app.getKey()))
              .header(IdempotencyChecker.RES_HEADER, "key=" + optional.get()), start).build();
    }

    App registered = appService.registerApp(app)
        .orElseThrow(() -> new RuntimeException("todo"));

    return headers.enrich(Response.created(loc).entity(registered), start).build();
  }

  @GET
  @Path("/{app_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getAppByKey")
  public Response getAppByKey(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_key") String appKey
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    final Optional<App> maybe = appService.loadAppByKey(appKey);

    if (maybe.isPresent()) {
      accessControlSupport.throwUnlessGrantedForApp(authPrincipal, maybe.get());
      return headers.enrich(Response.ok(maybe.get()), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("app_not_found", "", 404)), start).build();
  }

  @POST
  @Path("/{app_key}/owners")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addOwner")
  public Response addOwner(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_key") String appKey,
      Owner owner
  ) throws AuthenticationException {
    return postUpdate(authPrincipal, appKey, app -> appService.addToApp(app, owner));
  }

  @POST
  @Path("/{app_key}/grants/services")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addService")
  public Response addService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_key") String appKey,
      ServiceGrant grant
  ) throws AuthenticationException {
    return postUpdate(authPrincipal, appKey, app -> appService.addToApp(app, grant));
  }

  @POST
  @Path("/{app_key}/grants/members")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addService")
  public Response addService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_key") String appKey,
      MemberGrant grant
  ) throws AuthenticationException {
    return postUpdate(authPrincipal, appKey, app -> appService.addToApp(app, grant));
  }

  @DELETE
  @Path("/{app_key}/grants/services/{service_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeServiceGrant")
  public Response removeService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_key") String appKey,
      @PathParam("service_key") String serviceKey
  ) throws AuthenticationException {
    final long start = System.currentTimeMillis();

    final Optional<App> maybe = appService.loadAppByKey(appKey);

    if (maybe.isPresent()) {
      final App app = maybe.get();
      accessControlSupport.throwUnlessGrantedForApp(authPrincipal, app);
      final App updated = appService.removeServiceGrant(app, serviceKey);

      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("app_not_found", "", 404)), start).build();
  }

  @DELETE
  @Path("/{app_key}/grants/members/{member_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeServiceGrant")
  public Response removeMember(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_key") String appKey,
      @PathParam("member_key") String memberKey
  ) throws AuthenticationException {
    final long start = System.currentTimeMillis();

    final Optional<App> maybe = appService.loadAppByKey(appKey);

    if (maybe.isPresent()) {
      final App app = maybe.get();
      accessControlSupport.throwUnlessGrantedForApp(authPrincipal, app);
      final App updated = appService.removeMemberGrant(app, memberKey);

      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("app_not_found", "", 404)), start).build();
  }

  @DELETE
  @Path("/{app_key}/owners")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeOwner")
  public Response removeService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("app_key") String appKey,
      @QueryParam("username") String username,
      @QueryParam("email") String email
  ) throws AuthenticationException {
    final long start = System.currentTimeMillis();

    final Optional<App> maybe = appService.loadAppByKey(appKey);

    if (maybe.isPresent()) {
      final App app = maybe.get();
      accessControlSupport.throwUnlessGrantedForApp(authPrincipal, app);
      final App updated = appService.removeOwner(app, username, email);
      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("app_not_found", "", 404)), start).build();
  }

  /*
   * Test endpoint to see how our graph writes are doing.
   */
  @GET
  @Path("graph")
  public Response belongsTo(
      @Auth AuthPrincipal authPrincipal,
      @QueryParam("app_key") String appKey,
      @QueryParam("username") String username,
      @QueryParam("email") String email,
      @QueryParam("service_key") String serviceKey,
      @QueryParam("relation") String relation
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    final Optional<App> maybe = appService.loadAppByKey(appKey);
    if (maybe.isPresent()) {
      accessControlSupport.throwUnlessGrantedForApp(authPrincipal, maybe.get());

      boolean found = false;

      if(AppService.GRANT_RELATION.equals(relation)) {
        if (!Strings.isNullOrEmpty(username)) {
          found = appService.appHasMemberGrant(appKey, username);
        } else if (!Strings.isNullOrEmpty(email)) {
          found = appService.appHasMemberGrant(appKey, email);
        } else {
          found = appService.appHasServiceGrant(appKey, serviceKey);
        }
      }

      if(AppService.OWNER_RELATION.equals(relation)) {
        if (!Strings.isNullOrEmpty(username)) {
          found = appService.appHasMemberGrant(appKey, username);
        } else if (!Strings.isNullOrEmpty(email)) {
          found = appService.appHasMemberGrant(appKey, email);
        }
      }

      if (found) {
        return Response.ok().build();
      } else {
        return Response.status(404).build();
      }
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("app_not_found", "", 404)), start).build();

  }

  private Response postUpdate(
      AuthPrincipal authPrincipal,
      String appKey,
      Function<App, App> updater
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    final Optional<App> maybe = appService.loadAppByKey(appKey);

    if (maybe.isPresent()) {
      final App app = maybe.get();
     accessControlSupport.throwUnlessGrantedForApp(authPrincipal, app);
      final App updated = updater.apply(app);
      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("app_not_found", "", 404)), start).build();
  }
}
