package outland.feature.server.resources;

import outland.feature.proto.Feature;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

import static outland.feature.server.StructLog.kvp;

public class GroupValidator {

  void throwUnlessFeatureKeyMatch(Feature feature, String featureKey) {
    if (!feature.getKey().equals(featureKey)) {
      throw new ServiceException(Problem.clientProblem(
          "Resource and entity feature keys do not match.",
          kvp("url_feature_key", featureKey, "data_feature_key", feature.getKey()),
          422));
    }
  }

  void throwUnlessGroupKeyMatch(Feature feature, String group) {
    if (!feature.getGroup().equals(group)) {
      throw new ServiceException(Problem.clientProblem(
          "Resource and entity group ids do not match.",
          kvp("url_group", group, "data_group", feature.getGroup()),
          422));
    }
  }
}
