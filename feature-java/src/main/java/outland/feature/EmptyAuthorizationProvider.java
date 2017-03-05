package outland.feature;

import java.util.Optional;

/**
 * Returns optional empty instead of a authorization.
 */
public class EmptyAuthorizationProvider implements AuthorizationProvider {

  @Override public Optional<Authorization> authorization(String appId, String scope) {
    return Optional.empty();
  }
}
