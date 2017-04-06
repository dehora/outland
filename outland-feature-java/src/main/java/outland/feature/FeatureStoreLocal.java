package outland.feature;

import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

interface FeatureStoreLocal {

  Void put(Feature feature) throws FeatureException;

  FeatureCollection loadAll() throws FeatureException;

  FeatureCollection findAll(String namespace)  throws FeatureException;

  void close() throws FeatureException;
}
