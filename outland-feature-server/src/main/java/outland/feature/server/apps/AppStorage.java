package outland.feature.server.apps;

import outland.feature.proto.App;

public interface AppStorage {

  String SUBJECT_KEY = "subject";
  String OBJECT_RELATION_KEY = "object_relation";

  Void saveApp(App app);

  Void saveRelation(App app, String relationHashKey, String relationRangeKey);

  boolean queryRelationExists(String relationHashKey, String relationRangeKey);
}
