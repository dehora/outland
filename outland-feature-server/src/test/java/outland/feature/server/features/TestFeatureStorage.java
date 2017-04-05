package outland.feature.server.features;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureVersion;

public class TestFeatureStorage implements FeatureStorage {

  static Map<String, Feature> features = Maps.newHashMap();

  @Override public Void createFeature(Feature feature) {

    if(features.containsKey(feature.getKey())) {
      throwConflictAlreadyExists(feature);
    }

    features.put(feature.getKey(), feature);
    return null;
  }

  @Override public Void updateFeature(Feature feature, FeatureVersion foundVersion) {

    features.put(feature.getKey(), feature);
    return null;
  }

  @Override public Optional<Feature> loadFeatureByKey(String appKey, String key) {

    Set<Map.Entry<String, Feature>> entries = features.entrySet();
    for (Map.Entry<String, Feature> entry : entries) {
      if (entry.getValue().getKey().equals(key) && entry.getValue().getAppkey().equals(appKey)) {
        return Optional.of(entry.getValue());
      }
    }

    return Optional.empty();
  }

  @Override public List<Feature> loadFeatures(String appKey) {

    return features.values().stream().collect(Collectors.toList());
  }
}
