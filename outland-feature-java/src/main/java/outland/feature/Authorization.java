package outland.feature;

/**
 * Used to supply auth information to the client. See {@link AuthorizationProvider}
 */
public class Authorization {

  public static final String REALM_BASIC = "Basic";
  public static final String REALM_BEARER = "Bearer";

  private final String realm;
  private final String credential;

  /**
   * @param realm the realm defined for the authorization mechanism
   * @param credential the credential for the authorization mechanism, such as a token.
   */
  public Authorization(String realm, String credential) {
    this.realm = realm;
    this.credential = credential;
  }

  /**
   * The authorization realm. For example for OAuth2 this would typically be "Bearer".
   *
   * @return the realm
   */
  public String realm() {
    return realm;
  }

  /**
   * The authorization realm. For example for OAuth2 this would  be a token string.
   *
   * @return the credential or token.
   */
  public String credential() {
    return credential;
  }
}
