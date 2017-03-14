package outland.feature.server.apps;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import outland.feature.proto.App;

public class TestAppStorage implements AppStorage {

  Map<String, App> members = Maps.newHashMap();


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
}
