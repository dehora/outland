package outland.feature;

import java.util.Optional;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

interface FeatureStore {

  Void put(Feature feature) throws FeatureException;

  Optional<Feature> find(String namespace, String key) throws FeatureException;

  FeatureCollection findAll(String namespace) throws FeatureException;

  Void remove(String namespace, String featureKey) throws FeatureException;

  Void removeAll() throws FeatureException;

  void close() throws FeatureException;
}
