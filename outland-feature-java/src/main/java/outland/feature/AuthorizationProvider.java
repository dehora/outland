package outland.feature;

import java.util.Optional;

/**
 * Provides a authorization string that can be used to authorize requests against the server or an
 * outland node. Acts as a {@link FunctionalInterface} and can be supplied as a lambda expression.
 */
@FunctionalInterface
public interface AuthorizationProvider {

  String SCOPE_UID = "uid";

  /**
   * Provide an authorization string that can be used to authorize requests against the server or an
   * outland node.
   *
   * @param namespace the namespace for the feature request
   * @param scope an authorization scope such as an OAuth scope
   * @return a value suitable for use as an Authorization header value, or empty to suppress the
   * authorization being set
   */
  Optional<Authorization> authorization(String namespace, String scope);
}
