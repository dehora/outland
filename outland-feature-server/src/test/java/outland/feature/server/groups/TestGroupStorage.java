package outland.feature.server.groups;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import outland.feature.proto.Group;

public class TestGroupStorage implements GroupStorage {

  Map<String, Group> members = Maps.newHashMap();

  @Override public Void create(Group group) {
    if(members.containsKey(group.getKey())) {
      throwConflictAlreadyExists(group);
    }

    members.put(group.getKey(), group);
    return null;
  }

  @Override public Void save(Group group) {
    return null;
  }

  @Override public Void saveRelation(Group group, String relationHashKey, String relationRangeKey) {
    members.put(group.getKey(), group);
    members.put(relationHashKey+relationRangeKey, group);
    return null;
  }

  @Override public boolean queryRelationExists(String relationHashKey, String relationRangeKey) {
    return members.get(relationHashKey+relationRangeKey) != null;
  }

  @Override public Optional<Group> loadByKey(String key) {
    return Optional.ofNullable(members.get(key));
  }

  @Override public Void removeRelation(Group group, String relationHashKey, String relationRangeKey) {
    members.remove(relationHashKey+relationRangeKey);
    return null;
  }
}
