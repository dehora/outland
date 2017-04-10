package outland.feature.server.groups;

import java.util.Optional;
import outland.feature.proto.Group;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

public interface GroupStorage {

  String SUBJECT_KEY = "subject";
  String OBJECT_RELATION_KEY = "object_relation";

  default void throwConflictAlreadyExists(Group group) {
    throw new ServiceException(Problem.clientProblem("conflict_group_already_exists",
        String.format("The Group named %s already exists and can't be recreated. "
            + "Please consider submitting requests with an Idempotency-Key header. "
            + "If you are already sending the header please check your client "
            + "for highly delayed resubmissions.", group.getKey()), 409));
  }

  Void create(Group group);

  Void save(Group group);

  Void saveRelation(Group group, String relationHashKey, String relationRangeKey);

  Void removeRelation(Group group, String relationHashKey, String relationRangeKey);

  boolean queryRelationExists(String relationHashKey, String relationRangeKey);

  Optional<Group> loadByKey(String key);
}
