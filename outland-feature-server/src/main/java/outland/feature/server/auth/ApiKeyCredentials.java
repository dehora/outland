package outland.feature.server.auth;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

public class ApiKeyCredentials {

  private Map<String, String> apiKeyMap;

  @Inject
  public ApiKeyCredentials(AuthConfiguration configuration) {
    loadApiKeys(configuration);
  }

  Optional<AuthPrincipal> authenticated(String apiKey, String identity, String kind) {
    if (apiKeyMap.containsKey(identity) && apiKeyMap.get(identity).equals(apiKey)) {
      final ArrayList<String> scopes = Lists.newArrayList(TokenAuthorizer.WILDCARD_SCOPE);
      final AuthPrincipal value = new AuthPrincipal(kind, identity, scopes);
      return Optional.of(value);
    }

    return Optional.empty();
  }

  private void loadApiKeys(AuthConfiguration configuration) {
    apiKeyMap = Splitter.on(",")
        .withKeyValueSeparator('=')
        .split(configuration.basicAuthenticationKeys);
  }
}
