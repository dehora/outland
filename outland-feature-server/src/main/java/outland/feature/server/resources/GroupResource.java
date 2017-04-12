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
import outland.feature.proto.Group;
import outland.feature.proto.MemberAccess;
import outland.feature.proto.Owner;
import outland.feature.proto.ServiceAccess;
import outland.feature.server.Problem;
import outland.feature.server.ServerConfiguration;
import outland.feature.server.auth.AccessControlSupport;
import outland.feature.server.auth.AuthPrincipal;
import outland.feature.server.groups.GroupService;

@Resource
@Path("/groups")
public class GroupResource {

  public static final String TITLE_NOT_FOUND = "group_not_found";
  private final GroupService groupService;
  private final IdempotencyChecker idempotencyChecker;
  private final AccessControlSupport accessControlSupport;
  private final URI baseURI;
  private final Headers headers;

  @Inject
  public GroupResource(
      GroupService groupService,
      IdempotencyChecker idempotencyChecker,
      AccessControlSupport accessControlSupport,
      ServerConfiguration serviceConfiguration,
      Headers headers
  ) {
    this.groupService = groupService;
    this.idempotencyChecker = idempotencyChecker;
    this.accessControlSupport = accessControlSupport;
    this.baseURI = serviceConfiguration.baseURI;
    ;
    this.headers = headers;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Timed(name = "register")
  public Response register(
      @Auth AuthPrincipal authPrincipal,
      Group group,
      @Context HttpHeaders httpHeaders
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    // no further acl check; if you are an authenticated AuthPrincipal, you can create

    URI loc = UriBuilder.fromUri(baseURI)
        .path(group.getKey())
        .build();

    final Optional<String> optional = idempotencyChecker.extractKey(httpHeaders);
    final boolean seen = optional.isPresent() && idempotencyChecker.seen(optional.get());
    if (seen) {
      return headers.enrich(
          Response.ok(groupService.loadByKey(group.getKey()))
              .header(IdempotencyChecker.RES_HEADER, "key=" + optional.get()), start).build();
    }

    Group registered = groupService.register(group)
        .orElseThrow(() -> new RuntimeException("todo"));

    return headers.enrich(Response.created(loc).entity(registered), start).build();
  }

  @GET
  @Path("/{group}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "getByKey")
  public Response getByKey(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    final Optional<Group> maybe = groupService.loadByKey(group);

    if (maybe.isPresent()) {
      accessControlSupport.throwUnlessGrantedFor(authPrincipal, maybe.get());
      return headers.enrich(Response.ok(maybe.get()), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem(GroupResource.TITLE_NOT_FOUND, "", 404)), start).build();
  }

  @POST
  @Path("/{group}/owners")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addOwner")
  public Response addOwner(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group,
      Owner owner
  ) throws AuthenticationException {
    return postUpdate(authPrincipal, group, ns -> groupService.add(ns, owner));
  }

  @POST
  @Path("/{group}/access/services")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addService")
  public Response addService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group,
      ServiceAccess serviceAccess
  ) throws AuthenticationException {
    return postUpdate(authPrincipal, group, ns -> groupService.add(ns, serviceAccess));
  }

  @POST
  @Path("/{group}/access/members")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "addService")
  public Response addService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String group,
      MemberAccess memberAccess
  ) throws AuthenticationException {
    return postUpdate(authPrincipal, group, ns -> groupService.add(ns, memberAccess));
  }

  @DELETE
  @Path("/{group}/access/services/{service_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeService")
  public Response removeService(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String groupKey,
      @PathParam("service_key") String serviceKey
  ) throws AuthenticationException {
    final long start = System.currentTimeMillis();

    final Optional<Group> maybe = groupService.loadByKey(groupKey);

    if (maybe.isPresent()) {
      final Group group = maybe.get();
      accessControlSupport.throwUnlessGrantedFor(authPrincipal, group);
      final Group updated = groupService.removeServiceAccess(group, serviceKey);

      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem(TITLE_NOT_FOUND, "", 404)), start).build();
  }

  @DELETE
  @Path("/{group}/access/members/{member_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeMember")
  public Response removeMember(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String groupKey,
      @PathParam("member_key") String memberKey
  ) throws AuthenticationException {
    final long start = System.currentTimeMillis();

    final Optional<Group> maybe = groupService.loadByKey(groupKey);

    if (maybe.isPresent()) {
      final Group group = maybe.get();
      accessControlSupport.throwUnlessGrantedFor(authPrincipal, group);
      final Group updated = groupService.removeMemberAccess(group, memberKey);

      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem(TITLE_NOT_FOUND, "", 404)), start).build();
  }

  @DELETE
  @Path("/{group}/owners/{owner_key}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  @Timed(name = "removeOwner")
  public Response removeOwner(
      @Auth AuthPrincipal authPrincipal,
      @PathParam("group") String groupKey,
      @PathParam("owner_key") String ownerKey
  ) throws AuthenticationException {
    final long start = System.currentTimeMillis();

    if (Strings.isNullOrEmpty(ownerKey)) {
      return headers.enrich(Response.status(404).entity(
          Problem.clientProblem("param_not_found", "", 404)), start).build();
    }

    final Optional<Group> maybe = groupService.loadByKey(groupKey);

    if (maybe.isPresent()) {
      final Group group = maybe.get();
      accessControlSupport.throwUnlessGrantedFor(authPrincipal, group);

      Group updated = groupService.removeOwner(group, ownerKey);
      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem(GroupResource.TITLE_NOT_FOUND, "", 404)), start).build();
  }

  /*
   * Test endpoint to see how our graph writes are doing.
   */
  @GET
  @Path("graph")
  public Response belongsTo(
      @Auth AuthPrincipal authPrincipal,
      @QueryParam("group") String group,
      @QueryParam("username") String username,
      @QueryParam("email") String email,
      @QueryParam("service_key") String serviceKey,
      @QueryParam("relation") String relation
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();

    final Optional<Group> maybe = groupService.loadByKey(group);
    if (maybe.isPresent()) {
      accessControlSupport.throwUnlessGrantedFor(authPrincipal, maybe.get());

      boolean found = false;

      if (GroupService.ACCESS_RELATION.equals(relation)) {
        if (!Strings.isNullOrEmpty(username)) {
          found = groupService.hasMemberAccess(group, username);
        } else if (!Strings.isNullOrEmpty(email)) {
          found = groupService.hasMemberAccess(group, email);
        } else {
          found = groupService.hasServiceAccess(group, serviceKey);
        }
      }

      if (GroupService.OWNER_RELATION.equals(relation)) {
        if (!Strings.isNullOrEmpty(username)) {
          found = groupService.hasOwner(group, username);
        } else if (!Strings.isNullOrEmpty(email)) {
          found = groupService.hasOwner(group, email);
        }
      }

      if (found) {
        return Response.ok().build();
      } else {
        return Response.status(404).build();
      }
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem(TITLE_NOT_FOUND, "", 404)), start).build();
  }

  private Response postUpdate(
      AuthPrincipal authPrincipal,
      String groupKey,
      Function<Group, Group> updater
  ) throws AuthenticationException {

    final long start = System.currentTimeMillis();
    final Optional<Group> maybe = groupService.loadByKey(groupKey);

    if (maybe.isPresent()) {
      final Group group = maybe.get();
      accessControlSupport.throwUnlessGrantedFor(authPrincipal, group);
      final Group updated = updater.apply(group);
      return headers.enrich(Response.ok(updated), start).build();
    }

    return headers.enrich(Response.status(404).entity(
        Problem.clientProblem(TITLE_NOT_FOUND, "", 404)), start).build();
  }
}
