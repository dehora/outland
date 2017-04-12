package outland.feature.server.groups;

import com.google.common.base.Strings;
import outland.feature.proto.Group;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

class GroupValidator {

  void validateRegistrationThrowing(Group group) throws ServiceException {
    if (group.getOwners().getItemsCount() == 0) {
      throw new ServiceException(Problem.clientProblem("no_owner_for_group",
          "A group must have at least one one owner", 422));
    }

    group.getOwners().getItemsList().forEach(owner -> {
      if (Strings.isNullOrEmpty(owner.getEmail()) && Strings.isNullOrEmpty(owner.getUsername())) {
        throw new ServiceException(Problem.clientProblem("incomplete_owner_for_group",
            "A group owner must have an email or a username", 422));
      }
    });

    if (Strings.isNullOrEmpty(group.getKey())) {
      throw new ServiceException(Problem.clientProblem("missing_key_for_group",
          "A group must have a key", 422));
    }

    if (Strings.isNullOrEmpty(group.getName())) {
      throw new ServiceException(Problem.clientProblem("missing_name_for_group",
          "A group must have a name", 422));
    }
  }
}
