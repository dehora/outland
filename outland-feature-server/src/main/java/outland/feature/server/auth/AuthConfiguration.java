package outland.feature.server.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

public class AuthConfiguration {

  public static final String AUTHENTICATION_POLICY_OAUTH_BEARER_CHECK = "oauth_bearer_check";
  public static final String AUTHENTICATION_POLICY_OAUTH_BEARER_REFLECT = "oauth_bearer_reflect";

  public static final String AUTHENTICATION_POLICY_BASIC_APP_USERNAME_PASSWORD_KEYS = "basic_app_username_password_keys";

  public static final String SCOPE_OAUTH_POLICY_DISABLED = "oauth_disable_scope_check";
  public static final String SCOPE_OAUTH_POLICY_ENFORCED = "oauth_enforce_scope_check";
  public static final String SCOPE_BASIC_POLICY_DISABLED = "basic_disable_scope_check";
  public static final String SCOPE_BASIC_POLICY_ENFORCED = "basic_enforce_scope_check";

  @NotNull
  @JsonProperty
  public Boolean basicEnabled = false;

  @JsonProperty
  public String basicAuthenticationKeys;

  @NotEmpty
  @JsonProperty
  public String basicAuthenticationPolicy = AUTHENTICATION_POLICY_BASIC_APP_USERNAME_PASSWORD_KEYS;

  @NotEmpty
  @JsonProperty
  public String basicScopePolicy = SCOPE_BASIC_POLICY_ENFORCED;

  @Min(1L)
  @Max(3600L)
  @JsonProperty
  public long basicCacheCredentialSeconds = 60;

  @NotNull
  @JsonProperty
  public Boolean oauthEnabled = true;

  @NotEmpty
  @JsonProperty
  public String oauthAuthenticationPolicy = AUTHENTICATION_POLICY_OAUTH_BEARER_CHECK;

  @NotEmpty
  @JsonProperty
  public String oauthScopePolicy = SCOPE_OAUTH_POLICY_ENFORCED;

  @Min(1L)
  @Max(3600L)
  @JsonProperty
  public long oauthCacheTokenSeconds = 60;


  @NotEmpty
  @JsonProperty
  public RemoteOAuthServerConfiguration remoteOAuthServer = new RemoteOAuthServerConfiguration();

  static class RemoteOAuthServerConfiguration {

    @NotNull
    @JsonProperty
    public URI tokenLookupURI;

    @Min(100L)
    @Max(8_000L)
    @JsonProperty
    long connectTimeout = 1_000L;

    @Min(200L)
    @Max(8_000L)
    @JsonProperty
    long readTimeout = 1_000L;
  }

}
