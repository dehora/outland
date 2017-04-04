package outland.feature.server.features;

import java.util.List;
import java.util.Optional;
import outland.feature.proto.Feature;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

public interface FeatureStorage {

  default void throwConflictAlreadyExists(Feature feature) {
    throw new ServiceException(Problem.clientProblem("conflict_feature_already_exists",
        String.format("The feature named %s already exists and can't be recreated. "
            + "Please consider submitting requests with an Idempotency-Key header. "
            + "If you are already sending the header please check your client "
            + "for highly delayed resubmissions.", feature.getKey()), 409));
  }

  Void createFeature(Feature feature);

  Void updateFeature(Feature feature);

  Optional<Feature> loadFeatureByKey(String appKey, String key);

  List<Feature> loadFeatures(String appKey);
}
