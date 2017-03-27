package outland.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class ResourceOptions {

  private final Map<String, Object> headers = new HashMap<>();
  private AuthorizationProvider provider;
  private String scope;
  private String appKey;

  public ResourceOptions securityTokenProvider(AuthorizationProvider provider) {
    FeatureException.throwIfNull(provider, "Please provide a AuthorizationProvider");
    this.provider = provider;
    return this;
  }

  public ResourceOptions header(String name, Object value) {
    FeatureException.throwIfNull(name, "Please provide a header name");
    FeatureException.throwIfNull(value, "Please provide a header value");
    this.headers.put(name, value);
    return this;
  }

  public ResourceOptions headers(Map<String, Object> headers) {
    FeatureException.throwIfNull(headers, "Please provide some headers");
    this.headers.putAll(headers);
    return this;
  }

  public ResourceOptions scope(String scope) {
    FeatureException.throwIfNull(scope, "Please provide a scope");
    this.scope = scope;
    return this;
  }

  public ResourceOptions appKey(String appKey) {
    FeatureException.throwIfNull(appKey, "Please provide an appKey");
    this.appKey = appKey;
    return this;
  }

  public Map<String, Object> headers() {
    return headers;
  }

  public Optional<Authorization> supplyToken() {
    return provider.authorization(this.appKey, this.scope);
  }

  public String scope() {
    return scope;
  }
}
