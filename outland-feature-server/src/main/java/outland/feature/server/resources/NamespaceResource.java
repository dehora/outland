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
import outland.feature.proto.Namespace;
import outland.feature.proto.MemberAccess;
import outland.feature.proto.Owner;
import outland.feature.proto.ServiceAccess;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.apps.NamespaceService;
import outland.feature.server.auth.AccessControlSupport;
import outland.feature.server.auth.AuthPrincipal;

@Resource
@Path("/namespaces")
public class NamespaceResource {

  private final NamespaceService namespaceService;
  private final IdempotencyChecker idempotencyChecker;
  private final AccessControlSupport accessControlSupport;
  private final URI baseURI;
  private final Headers headers;

  @Inject
  public NamespaceResource(
      NamespaceService namespaceService,
      IdempotencyChecker idempotencyChecker,
      AccessControlSupport accessControlSupport,
      ServerConfiguration serviceConfiguration,
      Headers headers
  ) {
    this.namespaceService = namespaceService;
    this.idempotencyChecker = idempotencyChecker;
    this.accessControlSupport = accessControlSupport;
    this.baseURI = serviceConfiguration.baseURI;
    ;
    this.headers = headers;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Timed(name = "registerNamespace")
  public Response registerNamespace(
      @Auth AuthPrincipal authPrincipal,
      Namespace namespace,
      @Context HttpHeaders httpHeaders
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    // no further acl check; if you are an authenticated AuthPrincipal, you can create namespaces

    URI loc = UriBuilder.fromUri(baseURI)
        .path(namespace.getKey())
        .build();

    final Optional<String> optional = idempotencyChecker.extractKey(httpHeaders);
    final boolean seen = optional.isPresent() && idempotencyChecker.seen(optional.get());
    if (seen) {
      return headers.enrich(
          Response.ok(namespaceService.loadNamespaceByKey(namespace.getKey()))
              .header(IdempotencyChecker.RES_HEADER, "key=" + optional.get()), start).build();
    }

    Namespace registered = namespaceService.registerNamespace(namespace)
        .orElseThrow(() -> new RuntimeException("todo"));

    return headers.enrich(Response.created(loc).entity(registered), start).build();
  }

  @GET
  @Path("/{namespace}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getNamespaceByKey")
  public Response getNamespaceByKey(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("namespace") String namespace
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    final Optional<Namespace> maybe = namespaceService.loadNamespaceByKey(namespace);

    if (maybe.isPresent()) {
      accessControlSupport.throwUnlessGrantedForNamespace(authPrincipal, maybe.get());
      return headers.enrich(Response.ok(maybe.get()), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("namespace_not_found", "", 404)), start).build();
  }

  @POST
  @Path("/{namespace}/owners")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addOwner")
  public Response addOwner(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("namespace") String namespace,
      Owner owner
  ) throws AuthenticationException {
    return postUpdate(authPrincipal, namespace, ns -> namespaceService.addToNamespace(ns, owner));
  }

  @POST
  @Path("/{namespace}/access/services")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addService")
  public Response addService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("namespace") String namespace,
      ServiceAccess serviceAccess
  ) throws AuthenticationException {
    return postUpdate(authPrincipal, namespace, ns -> namespaceService.addToNamespace(ns, serviceAccess));
  }

  @POST
  @Path("/{namespace}/access/members")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addService")
  public Response addService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("namespace") String namespace,
      MemberAccess memberAccess
  ) throws AuthenticationException {
    return postUpdate(authPrincipal, namespace, ns -> namespaceService.addToNamespace(ns, memberAccess));
  }

  @DELETE
  @Path("/{namespace}/access/services/{service_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeService")
  public Response removeService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("namespace") String namespaceKey,
      @PathParam("service_key") String serviceKey
  ) throws AuthenticationException {
    final long start = System.currentTimeMillis();

    final Optional<Namespace> maybe = namespaceService.loadNamespaceByKey(namespaceKey);

    if (maybe.isPresent()) {
      final Namespace namespace = maybe.get();
      accessControlSupport.throwUnlessGrantedForNamespace(authPrincipal, namespace);
      final Namespace updated = namespaceService.removeServiceAccess(namespace, serviceKey);

      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("namespace_not_found", "", 404)), start).build();
  }

  @DELETE
  @Path("/{namespace}/access/members/{member_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeMember")
  public Response removeMember(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("namespace") String namespaceKey,
      @PathParam("member_key") String memberKey
  ) throws AuthenticationException {
    final long start = System.currentTimeMillis();

    final Optional<Namespace> maybe = namespaceService.loadNamespaceByKey(namespaceKey);

    if (maybe.isPresent()) {
      final Namespace namespace = maybe.get();
      accessControlSupport.throwUnlessGrantedForNamespace(authPrincipal, namespace);
      final Namespace updated = namespaceService.removeMemberAccess(namespace, memberKey);

      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("namespace_not_found", "", 404)), start).build();
  }

  @DELETE
  @Path("/{namespace}/owners/{owner_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeOwner")
  public Response removeOwner(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("namespace") String namespaceKey,
      @PathParam("owner_key") String ownerKey
  ) throws AuthenticationException {
    final long start = System.currentTimeMillis();

    if(Strings.isNullOrEmpty(ownerKey)) {
      return headers.enrich(Response.status(404).entity(
          Problem.clientProblem("param_not_found", "", 404)), start).build();
    }

    final Optional<Namespace> maybe = namespaceService.loadNamespaceByKey(namespaceKey);

    if (maybe.isPresent()) {
      final Namespace namespace = maybe.get();
      accessControlSupport.throwUnlessGrantedForNamespace(authPrincipal, namespace);

      Namespace updated = namespaceService.removeOwner(namespace, ownerKey);
      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("namespace_not_found", "", 404)), start).build();
  }

  /*
   * Test endpoint to see how our graph writes are doing.
   */
  @GET
  @Path("graph")
  public Response belongsTo(
      @Auth AuthPrincipal authPrincipal,
      @QueryParam("namespace") String namespace,
      @QueryParam("username") String username,
      @QueryParam("email") String email,
      @QueryParam("service_key") String serviceKey,
      @QueryParam("relation") String relation
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    final Optional<Namespace> maybe = namespaceService.loadNamespaceByKey(namespace);
    if (maybe.isPresent()) {
      accessControlSupport.throwUnlessGrantedForNamespace(authPrincipal, maybe.get());

      boolean found = false;

      if(NamespaceService.ACCESS_RELATION.equals(relation)) {
        if (!Strings.isNullOrEmpty(username)) {
          found = namespaceService.hasMemberAccess(namespace, username);
        } else if (!Strings.isNullOrEmpty(email)) {
          found = namespaceService.hasMemberAccess(namespace, email);
        } else {
          found = namespaceService.hasServiceAccess(namespace, serviceKey);
        }
      }

      if(NamespaceService.OWNER_RELATION.equals(relation)) {
        if (!Strings.isNullOrEmpty(username)) {
          found = namespaceService.hasOwner(namespace, username);
        } else if (!Strings.isNullOrEmpty(email)) {
          found = namespaceService.hasOwner(namespace, email);
        }
      }

      if (found) {
        return Response.ok().build();
      } else {
        return Response.status(404).build();
      }
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("namespace_not_found", "", 404)), start).build();

  }

  private Response postUpdate(
      AuthPrincipal authPrincipal,
      String namespaceKey,
      Function<Namespace, Namespace> updater
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    final Optional<Namespace> maybe = namespaceService.loadNamespaceByKey(namespaceKey);

    if (maybe.isPresent()) {
      final Namespace namespace = maybe.get();
     accessControlSupport.throwUnlessGrantedForNamespace(authPrincipal, namespace);
      final Namespace updated = updater.apply(namespace);
      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem("namespace_not_found", "", 404)), start).build();
  }
}
