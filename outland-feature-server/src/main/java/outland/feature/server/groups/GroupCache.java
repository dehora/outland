package outland.feature.server.groups;

import java.util.Optional;
import outland.feature.proto.Group;

public interface GroupCache {

  String buildCacheKey(String group);

  Void addToCache(Group group);

  Optional<Group> findInCache(String key);

  Void flushCache(String group);

  Void flushAll();

}
