package outland.feature;

import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

public class FeatureStoreLocalFailing implements FeatureStoreLocal {

  @Override public Void put(Feature feature) throws FeatureException {
    throw new FeatureException(Problem.localProblem("failstore", ""));
  }

  @Override public FeatureCollection loadAll() throws FeatureException {
    throw new FeatureException(Problem.localProblem("failstore", ""));
  }

  @Override public FeatureCollection findAll(String appId) throws FeatureException {
    throw new FeatureException(Problem.localProblem("failstore", ""));
  }

  @Override public void close() throws FeatureException {
    throw new FeatureException(Problem.localProblem("failstore", ""));
  }
}
