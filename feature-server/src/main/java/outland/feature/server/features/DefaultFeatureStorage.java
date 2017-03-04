package outland.feature.server.features;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import outland.feature.proto.Feature;

public class DefaultFeatureStorage implements FeatureStorage {

  @Override public Void saveFeature(Feature feature) {
    return null;
  }

  @Override public Void updateFeature(Feature feature) {
    return null;
  }

  @Override public Optional<Feature> loadFeatureByKey(String appId, String key) {
    return Optional.empty();
  }

  @Override public List<Feature> loadFeatures(String appId) {
    return Lists.newArrayList();
  }
}
