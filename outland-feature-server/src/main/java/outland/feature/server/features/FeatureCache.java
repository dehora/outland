package outland.feature.server.features;

import java.util.Map;
import java.util.Optional;
import outland.feature.proto.Feature;

public interface FeatureCache {

  String buildCacheKeyByFeatureKey(String appId, String featureKey);

  Void addToCache(Feature feature);

  Optional<Map<String, String>> getCacheSet(String appId);

  Optional<Feature> findInCache(String key);

  Void flushCache(String appId, String featureKey, String id);

  Void flushAll();
}
