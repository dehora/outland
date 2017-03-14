package outland.feature.server.apps;

import com.google.common.collect.Maps;
import java.util.Map;
import outland.feature.proto.App;

public class TestAppStorage implements AppStorage {

  Map<String, App> members = Maps.newHashMap();


  @Override public Void saveApp(App app) {
    return null;
  }

  @Override public Void saveRelation(App app, String relationHashKey, String relationRangeKey) {
    members.put(relationHashKey+relationRangeKey, app);
    return null;
  }

  @Override public boolean queryRelationExists(String relationHashKey, String relationRangeKey) {
    return members.get(relationHashKey+relationRangeKey) != null;
  }
}
