package outland.feature.server.apps;

import com.google.common.base.Strings;
import outland.feature.proto.Namespace;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

class NamespaceValidator {

  void validateAppRegistrationThrowing(Namespace namespace) throws ServiceException {
    if (namespace.getOwners().getItemsCount() == 0) {
      throw new ServiceException(Problem.clientProblem("no_owner_for_namespace",
          "An app must have at least one one owner", 422));
    }

    namespace.getOwners().getItemsList().forEach(owner -> {
      if (Strings.isNullOrEmpty(owner.getEmail()) && Strings.isNullOrEmpty(owner.getUsername())) {
        throw new ServiceException(Problem.clientProblem("incomplete_owner_for_namespace",
            "An app owner must have an email or a username", 422));
      }
    });

    if (Strings.isNullOrEmpty(namespace.getKey())) {
      throw new ServiceException(Problem.clientProblem("missing_key_for_namespace",
          "An app must have a key", 422));
    }

    if (Strings.isNullOrEmpty(namespace.getName())) {
      throw new ServiceException(Problem.clientProblem("missing_name_for_namespace",
          "An app must have a name", 422));
    }
  }

}
