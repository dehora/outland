package outland.feature.server.apps;

import java.util.Optional;
import outland.feature.proto.Namespace;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

public interface NamespaceStorage {

  String SUBJECT_KEY = "subject";
  String OBJECT_RELATION_KEY = "object_relation";

  default void throwConflictAlreadyExists(Namespace app) {
    throw new ServiceException(Problem.clientProblem("conflict_namespace_already_exists",
        String.format("The Namespace named %s already exists and can't be recreated. "
            + "Please consider submitting requests with an Idempotency-Key header. "
            + "If you are already sending the header please check your client "
            + "for highly delayed resubmissions.", app.getKey()), 409));
  }

  Void createNamespace(Namespace namespace);

  Void saveNamespace(Namespace namespace);

  Void saveRelation(Namespace namespace, String relationHashKey, String relationRangeKey);

  Void removeRelation(Namespace namespace, String relationHashKey, String relationRangeKey);

  boolean queryRelationExists(String relationHashKey, String relationRangeKey);

  Optional<Namespace> loadNamespaceByKey(String nsKey);
}
