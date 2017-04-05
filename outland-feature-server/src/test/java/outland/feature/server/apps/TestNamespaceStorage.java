package outland.feature.server.apps;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import outland.feature.proto.Namespace;

public class TestNamespaceStorage implements NamespaceStorage {

  Map<String, Namespace> members = Maps.newHashMap();

  @Override public Void createNamespace(Namespace namespace) {
    if(members.containsKey(namespace.getKey())) {
      throwConflictAlreadyExists(namespace);
    }

    members.put(namespace.getKey(), namespace);
    return null;
  }

  @Override public Void saveNamespace(Namespace namespace) {
    return null;
  }

  @Override public Void saveRelation(Namespace namespace, String relationHashKey, String relationRangeKey) {
    members.put(namespace.getKey(), namespace);
    members.put(relationHashKey+relationRangeKey, namespace);
    return null;
  }

  @Override public boolean queryRelationExists(String relationHashKey, String relationRangeKey) {
    return members.get(relationHashKey+relationRangeKey) != null;
  }

  @Override public Optional<Namespace> loadNamespaceByKey(String nsKey) {
    return Optional.ofNullable(members.get(nsKey));
  }

  @Override public Void removeRelation(Namespace namespace, String relationHashKey, String relationRangeKey) {
    members.remove(relationHashKey+relationRangeKey);
    return null;
  }
}
