package outland.feature.server.namespaces;

import java.util.Optional;
import outland.feature.server.auth.AuthPrincipal;

public interface NamesapaceAuthService {

  String BEARER = "Bearer";

  Optional<AuthPrincipal> authenticate(String credentials, String tokenType);

}
