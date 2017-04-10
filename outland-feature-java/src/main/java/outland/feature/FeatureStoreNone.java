package outland.feature;

import java.util.Optional;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

class FeatureStoreNone implements FeatureStore {

  @Override public Void put(Feature feature) {
    return null;
  }

  @Override public Optional<Feature> find(String group, String key) {
    return Optional.empty();
  }

  @Override public FeatureCollection findAll(String group) {
    return null;
  }

  @Override public Void remove(String group, String featureKey) {
    return null;
  }

  @Override public Void removeAll() {
    return null;
  }

  @Override public void close() {

  }
}
