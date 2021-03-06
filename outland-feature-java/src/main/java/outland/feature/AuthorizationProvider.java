package outland.feature;

import java.util.Optional;

/**
 * Provides a authorization string that can be used to authorize requests.
 * Acts as a {@link FunctionalInterface} and can be supplied as a lambda expression.
 */
@FunctionalInterface
public interface AuthorizationProvider {

  /**
   * Provide an authorization string that can be used to authorize requests.
   *
   * @param group the group for the feature request
   * @param scope an authorization scope such as an OAuth scope
   * @return a value suitable for use as an Authorization header value, or empty to suppress the
   * authorization being set
   */
  Optional<Authorization> authorization(String group, String scope);
}
