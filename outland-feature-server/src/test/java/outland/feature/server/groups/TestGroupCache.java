package outland.feature.server.groups;

import java.util.Optional;
import outland.feature.proto.Group;

public class TestGroupCache implements GroupCache {
  @Override public String buildCacheKey(String group) {
    return null;
  }

  @Override public Void addToCache(Group group) {
    return null;
  }

  @Override public Optional<Group> findInCache(String key) {
    return Optional.empty();
  }

  @Override public Void flushCache(String group) {
    return null;
  }

  @Override public Void flushAll() {
    return null;
  }
}
