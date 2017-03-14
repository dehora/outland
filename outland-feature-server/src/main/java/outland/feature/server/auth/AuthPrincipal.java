package outland.feature.server.auth;

import java.security.Principal;
import java.util.List;

public class AuthPrincipal implements Principal {

  private String type;
  private List<String> scopes;
  private String identifier;
  private String token;
  private long expiresSeconds;

  public AuthPrincipal(
      String type,
      String identifier,
      List<String> scopes,
      String token,
      long expiresSeconds
  ) {
    this.type = type;
    this.scopes = scopes;
    this.identifier = identifier;
    this.token = token;
    this.expiresSeconds = expiresSeconds;
  }

  @Override public String getName() {
    return identifier();
  }

  public String type() {
    return type;
  }

  public List<String> scopes() {
    return scopes;
  }

  public String identifier() {
    return identifier;
  }

  public String token() { // not sure about this one
    return token;
  }

  public long expiresSeconds() {
    return expiresSeconds;
  }
}
