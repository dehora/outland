package outland.feature.server.features;

import java.util.Map;
import java.util.Optional;
import outland.feature.proto.Feature;

public class TestFeatureCache implements FeatureCache {

  @Override public String buildCacheKeyByFeatureKey(String appId, String featureKey) {
    return "";
  }

  @Override public Void addToCache(Feature feature) {
    return null;
  }

  @Override public Optional<Map<String, String>> getCacheSet(String appId) {
    return Optional.empty();
  }

  @Override public Optional<Feature> findInCache(String key) {
    return Optional.empty();
  }

  @Override public Void flushCache(String appId, String featureKey, String id) {
    return null;
  }

  @Override public Void flushAll() {
    return null;
  }
}
