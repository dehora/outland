package outland.feature.server.apps;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import outland.feature.proto.App;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

public class TestAppStorage implements AppStorage {

  Map<String, App> members = Maps.newHashMap();

  @Override public Void createApp(App app) {
    if(members.containsKey(app.getKey())) {
      throwConflictAlreadyExists(app);
    }

    members.put(app.getKey(), app);
    return null;
  }

  @Override public Void saveApp(App app) {
    return null;
  }

  @Override public Void saveRelation(App app, String relationHashKey, String relationRangeKey) {
    members.put(app.getKey(), app);
    members.put(relationHashKey+relationRangeKey, app);
    return null;
  }

  @Override public boolean queryRelationExists(String relationHashKey, String relationRangeKey) {
    return members.get(relationHashKey+relationRangeKey) != null;
  }

  @Override public Optional<App> loadAppByKey(String appKey) {
    return Optional.ofNullable(members.get(appKey));
  }

  @Override public Void removeRelation(App app, String relationHashKey, String relationRangeKey) {
    members.remove(relationHashKey+relationRangeKey);
    return null;
  }
}
