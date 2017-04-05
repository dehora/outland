package outland.feature.server.auth;

import io.dropwizard.auth.AuthenticationException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.App;
import outland.feature.server.apps.AppService;

import static outland.feature.server.StructLog.kvp;

public class AccessControlSupport {

  private static final Logger logger = LoggerFactory.getLogger(AccessControlSupport.class);

  private final AppService appService;
  private final List<String> multipleAppAccessList;

  @Inject
  public AccessControlSupport(
      AppService appService,
      @Named("multipleAppAccessList") List<String> multipleAppAccessList
  ) {
    this.appService = appService;
    this.multipleAppAccessList = multipleAppAccessList;
    logMultipleAppAccessGrants();
  }

  public void throwUnlessGrantedForApp(AuthPrincipal authPrincipal, App app)
      throws AuthenticationException {

    if (multipleAppAccessList.contains(authPrincipal.identifier())) {
      logger.info(kvp("op", "member_access_check",
          "multiple_app_access_granted", authPrincipal.identifier()));
      return;
    }

    if (! AppService.MEMBER.equals(authPrincipal.type()) && ! AppService.SERVICE.equals(authPrincipal.type())) {
      throw new AuthenticationException("Unknown grant type "+ authPrincipal.type());
    }

    if (AppService.MEMBER.equals(authPrincipal.type()) && ! memberHasGrant(authPrincipal, app)) {
      throw new AuthenticationException("Member grant not authenticated");
    }

    if(AppService.SERVICE.equals(authPrincipal.type()) && ! serviceHasGrant(authPrincipal, app)) {
      throw new AuthenticationException("Service grant not authenticated");
    }
  }

  public void throwUnlessGrantedForApp(AuthPrincipal authPrincipal, String appKey)
      throws AuthenticationException {

    if (multipleAppAccessList.contains(authPrincipal.identifier())) {
      logger.info(kvp("op", "member_access_check",
          "multiple_app_access_granted", authPrincipal.identifier()));
      return;
    }

    if (! AppService.MEMBER.equals(authPrincipal.type()) && ! AppService.SERVICE.equals(authPrincipal.type())) {
      throw new AuthenticationException("Unknown grant type "+ authPrincipal.type());
    }

    if (AppService.MEMBER.equals(authPrincipal.type()) && !memberHasGrant(authPrincipal, appKey)) {
      throw new AuthenticationException("Member grant not authenticated");
    }

    if (AppService.SERVICE.equals(authPrincipal.type()) && !serviceHasGrant(authPrincipal, appKey)) {
      throw new AuthenticationException("Service grant not authenticated");
    }
  }

  private boolean serviceHasGrant(AuthPrincipal authPrincipal, String appKey) {
    return appService.appHasServiceGrant(appKey, authPrincipal.identifier());
  }

  private boolean memberHasGrant(AuthPrincipal authPrincipal, String appKey) {
    return appService.appHasMemberGrant(appKey, authPrincipal.identifier());
  }

  // todo: move these into appService

  private boolean serviceHasGrant(AuthPrincipal authPrincipal, App app) {
    return app.getGranted().getServicesList().stream()
        .anyMatch(service -> service.getKey().equals(authPrincipal.identifier()));
  }

  private boolean memberHasGrant(AuthPrincipal authPrincipal, App app) {
    return app.getOwners().getItemsList().stream()
        .anyMatch(owner ->
            owner.getUsername().equals(authPrincipal.identifier())
                ||
                owner.getEmail().equals(authPrincipal.identifier()));
  }

  private void logMultipleAppAccessGrants() {
    multipleAppAccessList.forEach(serviceName -> logger.info(
        kvp("op", "configure_access_control",
            "multiple_app_access_granted", serviceName
        ))
    );
  }
}
