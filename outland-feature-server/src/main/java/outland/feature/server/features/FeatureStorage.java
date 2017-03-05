package outland.feature.server.features;

import java.util.List;
import java.util.Optional;
import outland.feature.proto.Feature;

public interface FeatureStorage {

  Void saveFeature(Feature feature);

  Void updateFeature(Feature feature);

  Optional<Feature> loadFeatureByKey(String appId, String key);

  List<Feature> loadFeatures(String appId);
}
