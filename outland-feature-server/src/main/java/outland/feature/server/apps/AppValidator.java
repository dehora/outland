package outland.feature.server.apps;

import com.google.common.base.Strings;
import outland.feature.proto.App;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

class AppValidator {

  void validateAppRegistrationThrowing(App app) throws ServiceException {
    if (app.getOwnersCount() == 0) {
      throw new ServiceException(Problem.clientProblem("no_owner_for_app",
          "An app must have at least one one owner", 422));
    }

    app.getOwnersList().forEach(owner -> {
      if (Strings.isNullOrEmpty(owner.getEmail()) && Strings.isNullOrEmpty(owner.getUsername())) {
        throw new ServiceException(Problem.clientProblem("incomplete_owner_for_app",
            "An app owner must have an email or a username", 422));
      }
    });

    if (Strings.isNullOrEmpty(app.getKey())) {
      throw new ServiceException(Problem.clientProblem("missing_key_for_app",
          "An app must have a key", 422));
    }

    if (Strings.isNullOrEmpty(app.getName())) {
      throw new ServiceException(Problem.clientProblem("missing_name_for_app",
          "An app must have a name", 422));
    }
  }

}
