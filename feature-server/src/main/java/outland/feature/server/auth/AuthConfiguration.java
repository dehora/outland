package outland.feature.server.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
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

  @NotNull
  @JsonProperty
  public Boolean oauthEnabled = true;


  @NotEmpty
  @JsonProperty
  public String oauthAuthenticationPolicy = AUTHENTICATION_POLICY_OAUTH_BEARER_CHECK;

  @NotEmpty
  @JsonProperty
  public String oauthScopePolicy = SCOPE_OAUTH_POLICY_ENFORCED;

}
