package outland.feature;

import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

public class FeatureStoreLocalNone implements FeatureStoreLocal {

  @Override public Void put(Feature feature) throws FeatureException {
    return null;
  }

  @Override public FeatureCollection loadAll() throws FeatureException {
    return FeatureCollection.newBuilder().build();
  }

  @Override public FeatureCollection findAll(String appId) throws FeatureException {
    return FeatureCollection.newBuilder().build();
  }

  @Override public void close() throws FeatureException {

  }
}
