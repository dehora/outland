package outland.feature;

public final class Authorization {

  public static final String REALM_BASIC = "Basic";
  public static final String REALM_BEARER = "Bearer";

  private final String realm;
  private final String credential;

  public Authorization(String realm, String credential) {
    this.realm = realm;
    this.credential = credential;
  }

  public String realm() {
    return realm;
  }

  public String credential() {
    return credential;
  }
}
