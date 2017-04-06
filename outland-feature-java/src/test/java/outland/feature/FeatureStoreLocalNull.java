package outland.feature;

import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

public class FeatureStoreLocalNull implements FeatureStoreLocal {

  @Override public Void put(Feature feature) throws FeatureException {
    return null;
  }

  @Override public FeatureCollection loadAll() throws FeatureException {
    return null;
  }

  @Override public FeatureCollection findAll(String namespace) throws FeatureException {
    return null;
  }

  @Override public void close() throws FeatureException {

  }
}
