package outland.feature.server.apps;

import java.util.Optional;
import outland.feature.server.auth.AuthPrincipal;

public interface AppAuthService {

  String BEARER = "Bearer";

  Optional<AuthPrincipal> authenticate(String credentials, String tokenType);

}
