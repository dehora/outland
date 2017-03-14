package outland.feature.server.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthenticationException;
import java.net.URI;
import java.util.List;
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
import outland.feature.proto.App;
import outland.feature.proto.Owner;
import outland.feature.proto.Service;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.apps.AppService;
import outland.feature.server.auth.AuthPrincipal;

@Resource
@Path("/apps")
public class AppResource {

  private final AppService appService;
  private final IdempotencyChecker idempotencyChecker;
  private final URI baseURI;
  private final Headers headers;

  @Inject
  public AppResource(
      AppService appService,
      IdempotencyChecker idempotencyChecker,
      ServerConfiguration serviceConfiguration,
      Headers headers
  ) {
    this.appService = appService;
    this.idempotencyChecker = idempotencyChecker;
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

    throwUnlessMember(authPrincipal, app);

    URI loc = UriBuilder.fromUri(baseURI)
        .path(app.getKey())
        .build();

    // noop: todo: fill this out on seen=t
    final Optional<String> optional = idempotencyChecker.extractKey(httpHeaders);
    final boolean seen = optional.isPresent() && idempotencyChecker.seen(optional.get());

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
      throwUnlessMember(authPrincipal, maybe.get());
      return headers.enrich(Response.ok(maybe.get()), start).build();
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
      @QueryParam("service_key") String serviceKey
  ) {

    boolean found;
    if (!Strings.isNullOrEmpty(username)) {
      found = appService.appHasOwner(appKey, username);
    } else if (!Strings.isNullOrEmpty(email)) {
      found = appService.appHasOwner(appKey, email);
    } else {
      found = appService.appHasService(appKey, serviceKey);
    }

    if (found) {
      return Response.ok().build();
    } else {
      return Response.status(404).build();
    }
  }

  private void throwUnlessMember(AuthPrincipal authPrincipal, App app)
      throws AuthenticationException {

    // todo: whitelist user interface service

    boolean member;

    if (authPrincipal.type().equals(AppService.OWNER)) {
      member = app.getOwnersList().stream()
          .anyMatch(owner ->
              owner.getUsername().equals(authPrincipal.identifier())
                  ||
                  owner.getEmail().equals(authPrincipal.identifier()));
    } else {
      member = app.getServicesList().stream()
          .anyMatch(service -> service.getKey().equals(authPrincipal.identifier()));
    }

    if (!member) {
      throw new AuthenticationException("Membership not authenticated for request");
    }
  }
}
