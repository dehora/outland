package outland.feature.server.auth;

import com.google.common.base.MoreObjects;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

public class AuthPrincipal implements Principal {

  private final String type;
  private final String identifier;
  private final List<String> scopes;

  public AuthPrincipal(String type, String identifier, List<String> scopes
  ) {
    this.type = type;
    this.scopes = scopes;
    this.identifier = identifier;
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

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("type", type)
        .add("identifier", identifier)
        .add("scopes", scopes)
        .toString();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AuthPrincipal that = (AuthPrincipal) o;
    return Objects.equals(type, that.type) &&
        Objects.equals(identifier, that.identifier) &&
        Objects.equals(scopes, that.scopes);
  }

  @Override public int hashCode() {
    return Objects.hash(type, identifier, scopes);
  }
}
