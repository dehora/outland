package outland.feature.server.apps;

import java.util.Optional;
import outland.feature.server.auth.AppMember;

public interface AppAuthService {

  String BEARER = "Bearer";

  Optional<AppMember> authenticate(String credentials, String tokenType);

}
