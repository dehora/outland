package outland.feature.server.features;

import java.util.Map;
import java.util.Optional;
import outland.feature.proto.Feature;

public class TestFeatureCache implements FeatureCache {

  @Override public String buildCacheKeyByFeatureKey(String nsKey, String featureKey) {
    return "";
  }

  @Override public Void addToCache(Feature feature) {
    return null;
  }

  @Override public Optional<Map<String, String>> getCacheSet(String nsKey) {
    return Optional.empty();
  }

  @Override public Optional<Feature> findInCache(String key) {
    return Optional.empty();
  }

  @Override public Void flushCache(String nsKey, String featureKey, String id) {
    return null;
  }

  @Override public Void flushAll() {
    return null;
  }
}
