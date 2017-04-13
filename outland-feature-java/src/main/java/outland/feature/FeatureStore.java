package outland.feature;

import java.util.Optional;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

interface FeatureStore {

  Void put(Feature feature) throws FeatureException;

  Feature find(String group, String key) throws FeatureException;

  FeatureCollection findAll(String group) throws FeatureException;

  Void remove(String group, String featureKey) throws FeatureException;

  Void removeAll() throws FeatureException;

  void close() throws FeatureException;
}
