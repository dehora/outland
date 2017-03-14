package outland.feature.server.auth;

import io.dropwizard.auth.AuthenticationException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.App;
import outland.feature.server.apps.AppAuthServiceViaPlanBServer;
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

  public void throwUnlessMember(AuthPrincipal authPrincipal, App app)
      throws AuthenticationException {

    if (multipleAppAccessList.contains(authPrincipal.identifier())) {
      logger.info(kvp("op", "member_access_check",
          "multiple_app_access_granted", authPrincipal.identifier()));
      return;
    }

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

  public void throwUnlessMember(AuthPrincipal authPrincipal, String appKey)
      throws AuthenticationException {


    if (multipleAppAccessList.contains(authPrincipal.identifier())) {
      logger.info(kvp("op", "member_access_check",
          "multiple_app_access_granted", authPrincipal.identifier()));
      return;
    }

    boolean member;
    if (authPrincipal.type().equals(AppService.OWNER)) {
      member = appService.appHasOwner(appKey, authPrincipal.identifier());
    } else {
      member = appService.appHasService(appKey, authPrincipal.identifier());
    }

    if (!member) {
      throw new AuthenticationException("Membership not authenticated for request");
    }
  }

  private void logMultipleAppAccessGrants() {
    multipleAppAccessList.forEach(serviceName -> logger.info(
        kvp("op", "configure_access_control",
            "multiple_app_access_granted", serviceName
        ))
    );
  }
}
