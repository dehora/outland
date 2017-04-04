package outland.feature.server.apps;

import java.util.Optional;
import outland.feature.proto.App;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

public interface AppStorage {

  String SUBJECT_KEY = "subject";
  String OBJECT_RELATION_KEY = "object_relation";

  default void throwConflictAlreadyExists(App app) {
    throw new ServiceException(Problem.clientProblem("conflict_app_already_exists",
        String.format("The app named %s already exists and can't be recreated. "
            + "Please consider submitting requests with an Idempotency-Key header. "
            + "If you are already sending the header please check your client "
            + "for highly delayed resubmissions.", app.getKey()), 409));
  }

  Void createApp(App app);

  Void saveApp(App app);

  Void saveRelation(App app, String relationHashKey, String relationRangeKey);

  Void removeRelation(App app, String relationHashKey, String relationRangeKey);

  boolean queryRelationExists(String relationHashKey, String relationRangeKey);

  Optional<App> loadAppByKey(String appKey);
}
