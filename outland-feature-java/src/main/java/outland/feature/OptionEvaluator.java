package outland.feature;

import java.util.List;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.OptionType;

class OptionEvaluator {

  private static final int MAX_WEIGHT = 10_000;

  boolean evaluateBooleanOptions(Feature feature) {

    if (!feature.getOption().equals(OptionType.bool)) {
      return false; // todo: throw this?
    }

    if(! feature.getState().equals(Feature.State.on)) {
      return false; // todo: replace with a fallback when we add fallbacks for options
    }

    final List<FeatureOption> optionsList = feature.getOptionsList();

    // only two options for a flag type
    final FeatureOption option1 = optionsList.get(0);
    final FeatureOption option2 = optionsList.get(1);

    final int weight1 = option1.getWeight();
    final int weight2 = option2.getWeight();

    if (weight1 == 0 && weight2 == 0) { // zero/missing waits treated as a plain toggle
      return feature.getState().equals(Feature.State.on);
    }

    // normalise to 0.0..1.0
    double n1 = normalize(weight1);
    double n2 = normalize(weight2);

    if (Math.random() < n1) {
      return Boolean.parseBoolean(option1.getValue());
    } else {
      return Boolean.parseBoolean(option2.getValue());
    }
  }

  double normalize(int weight) {
    double v = (weight - 0.0d) / (MAX_WEIGHT - 0.0d);

    if (v > 1.0d) {
      return 1.0d;
    }

    if (v < 0.0d) {
      return 0.0d;
    }

    return v;
  }
}
