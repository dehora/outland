package outland.feature.server.auth;

import java.security.Principal;

public class App implements Principal {

  private String appId;

  public App(String appId) {
    this.appId = appId;
  }

  public String appId() {
    return appId;
  }

  @Override public String getName() {
    return appId();
  }
}
