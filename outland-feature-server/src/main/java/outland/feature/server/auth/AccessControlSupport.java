package outland.feature.server.auth;

import io.dropwizard.auth.AuthenticationException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Namespace;
import outland.feature.server.namespaces.NamespaceService;

import static outland.feature.server.StructLog.kvp;

public class AccessControlSupport {

  private static final Logger logger = LoggerFactory.getLogger(AccessControlSupport.class);

  private final NamespaceService namespaceService;
  private final List<String> multipleNamespaceAccessList;

  @Inject
  public AccessControlSupport(
      NamespaceService namespaceService,
      @Named("multipleNamespaceAccessList") List<String> multipleNamespaceAccessList
  ) {
    this.namespaceService = namespaceService;
    this.multipleNamespaceAccessList = multipleNamespaceAccessList;
    logMultipleAppAccessGrants();
  }

  public void throwUnlessGrantedForNamespace(AuthPrincipal authPrincipal, Namespace namespace)
      throws AuthenticationException {

    if (multipleNamespaceAccessList.contains(authPrincipal.identifier())) {
      logger.info(kvp("op", "member_access_check",
          "multiple_namespace_access_granted", authPrincipal.identifier()));
      return;
    }

    if (! NamespaceService.MEMBER.equals(authPrincipal.type()) && ! NamespaceService.SERVICE.equals(authPrincipal.type())) {
      throw new AuthenticationException("Unknown type "+ authPrincipal.type());
    }

    if (NamespaceService.MEMBER.equals(authPrincipal.type()) && ! memberHasGrant(authPrincipal, namespace)) {
      throw new AuthenticationException("Member not authenticated");
    }

    if(NamespaceService.SERVICE.equals(authPrincipal.type()) && ! serviceHasGrant(authPrincipal, namespace)) {
      throw new AuthenticationException("Service not authenticated");
    }
  }

  public void throwUnlessGrantedForNamespace(AuthPrincipal authPrincipal, String appKey)
      throws AuthenticationException {

    if (multipleNamespaceAccessList.contains(authPrincipal.identifier())) {
      logger.info(kvp("op", "member_access_check",
          "multiple_namespace_access_granted", authPrincipal.identifier()));
      return;
    }

    if (! NamespaceService.MEMBER.equals(authPrincipal.type()) && ! NamespaceService.SERVICE.equals(authPrincipal.type())) {
      throw new AuthenticationException("Unknown access type "+ authPrincipal.type());
    }

    if (NamespaceService.MEMBER.equals(authPrincipal.type()) && !memberHasGrant(authPrincipal, appKey)) {
      throw new AuthenticationException("Member not authenticated");
    }

    if (NamespaceService.SERVICE.equals(authPrincipal.type()) && !serviceHasGrant(authPrincipal, appKey)) {
      throw new AuthenticationException("Service not authenticated");
    }
  }

  private boolean serviceHasGrant(AuthPrincipal authPrincipal, String appKey) {
    return namespaceService.hasServiceAccess(appKey, authPrincipal.identifier());
  }

  private boolean memberHasGrant(AuthPrincipal authPrincipal, String appKey) {
    return namespaceService.hasMemberAccess(appKey, authPrincipal.identifier());
  }

  // todo: move these into namespaceService

  private boolean serviceHasGrant(AuthPrincipal authPrincipal, Namespace app) {
    return app.getGranted().getServicesList().stream()
        .anyMatch(service -> service.getKey().equals(authPrincipal.identifier()));
  }

  private boolean memberHasGrant(AuthPrincipal authPrincipal, Namespace app) {
    return app.getOwners().getItemsList().stream()
        .anyMatch(owner ->
            owner.getUsername().equals(authPrincipal.identifier())
                ||
                owner.getEmail().equals(authPrincipal.identifier()));
  }

  private void logMultipleAppAccessGrants() {
    multipleNamespaceAccessList.forEach(serviceName -> logger.info(
        kvp("op", "configure_access_control",
            "multiple_app_access_granted", serviceName
        ))
    );
  }
}
