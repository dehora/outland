package outland.feature.server.auth;

import io.dropwizard.auth.AuthenticationException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Group;
import outland.feature.server.groups.GroupService;

import static outland.feature.server.StructLog.kvp;

public class AccessControlSupport {

  private static final Logger logger = LoggerFactory.getLogger(AccessControlSupport.class);

  private final GroupService groupService;
  private final List<String> multipleGroupAccessList;

  @Inject
  public AccessControlSupport(
      GroupService groupService,
      @Named("multipleGroupAccessList") List<String> multipleGroupAccessList
  ) {
    this.groupService = groupService;
    this.multipleGroupAccessList = multipleGroupAccessList;
    logMultipleAppAccessGrants();
  }

  public void throwUnlessGrantedFor(AuthPrincipal authPrincipal, Group group)
      throws AuthenticationException {

    if (multipleGroupAccessList.contains(authPrincipal.identifier())) {
      logger.info(kvp("op", "member_access_check",
          "multiple_group_access_granted", authPrincipal.identifier()));
      return;
    }

    if (! GroupService.MEMBER.equals(authPrincipal.type()) && ! GroupService.SERVICE.equals(authPrincipal.type())) {
      throw new AuthenticationException("Unknown type "+ authPrincipal.type());
    }

    if (GroupService.MEMBER.equals(authPrincipal.type()) && ! memberHasGrant(authPrincipal, group)) {
      throw new AuthenticationException("Member not authenticated");
    }

    if(GroupService.SERVICE.equals(authPrincipal.type()) && ! serviceHasGrant(authPrincipal, group)) {
      throw new AuthenticationException("Service not authenticated");
    }
  }

  public void throwUnlessGrantedFor(AuthPrincipal authPrincipal, String appKey)
      throws AuthenticationException {

    if (multipleGroupAccessList.contains(authPrincipal.identifier())) {
      logger.info(kvp("op", "member_access_check",
          "multiple_group_access_granted", authPrincipal.identifier()));
      return;
    }

    if (! GroupService.MEMBER.equals(authPrincipal.type()) && ! GroupService.SERVICE.equals(authPrincipal.type())) {
      throw new AuthenticationException("Unknown access type "+ authPrincipal.type());
    }

    if (GroupService.MEMBER.equals(authPrincipal.type()) && !memberHasGrant(authPrincipal, appKey)) {
      throw new AuthenticationException("Member not authenticated");
    }

    if (GroupService.SERVICE.equals(authPrincipal.type()) && !serviceHasGrant(authPrincipal, appKey)) {
      throw new AuthenticationException("Service not authenticated");
    }
  }

  private boolean serviceHasGrant(AuthPrincipal authPrincipal, String appKey) {
    return groupService.hasServiceAccess(appKey, authPrincipal.identifier());
  }

  private boolean memberHasGrant(AuthPrincipal authPrincipal, String appKey) {
    return groupService.hasMemberAccess(appKey, authPrincipal.identifier());
  }

  // todo: move these into groupService

  private boolean serviceHasGrant(AuthPrincipal authPrincipal, Group group) {
    return group.getGranted().getServicesList().stream()
        .anyMatch(service -> service.getKey().equals(authPrincipal.identifier()));
  }

  private boolean memberHasGrant(AuthPrincipal authPrincipal, Group group) {
    return group.getOwners().getItemsList().stream()
        .anyMatch(owner ->
            owner.getUsername().equals(authPrincipal.identifier())
                ||
                owner.getEmail().equals(authPrincipal.identifier()));
  }

  private void logMultipleAppAccessGrants() {
    multipleGroupAccessList.forEach(serviceName -> logger.info(
        kvp("op", "configure_access_control",
            "multiple_app_access_granted", serviceName
        ))
    );
  }
}
