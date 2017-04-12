package outland.feature.server.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

public class AuthConfiguration {

  public static final String SCOPE_OAUTH_POLICY_DISABLED = "oauth_disable_scope_check";
  public static final String SCOPE_OAUTH_POLICY_ENFORCED = "oauth_enforce_scope_check";

  @NotNull
  @JsonProperty
  public Boolean basicEnabled = false;

  @JsonProperty
  public String basicAuthenticationKeys;

  @Min(1L)
  @Max(3600L)
  @JsonProperty
  public long basicCacheCredentialSeconds = 60;

  @NotNull
  @JsonProperty
  public Boolean oauthEnabled = true;

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
  @NotNull
  @JsonProperty
  String multipleGroupAccessList = "";

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
