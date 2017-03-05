package outland.feature;

import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

public interface FeatureStoreLocal {

  Void put(Feature feature) throws FeatureException;

  FeatureCollection loadAll() throws FeatureException;

  FeatureCollection findAll(String appId)  throws FeatureException;

  void close() throws FeatureException;
}
