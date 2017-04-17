package outland.feature;

import java.util.List;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.State;

class OptionEvaluator {

  @VisibleForTesting
  boolean evaluateBooleanOptions(Feature feature) {
    return evaluateBooleanOptions(feature.getOptions(), feature.getState());
  }

  boolean evaluateBooleanOptions(OptionCollection options, State state) {
    if (!options.getOption().equals(OptionType.bool)) {
      return false; // todo: throw this?
    }

    if(! state.equals(State.on)) {
      return false; // todo: replace with a fallback when we add fallbacks for options
    }

    final List<FeatureOption> optionsList = options.getItemsList();

    // only two options for a flag type
    final FeatureOption option1 = optionsList.get(0);
    final FeatureOption option2 = optionsList.get(1);

    final int weight1 = option1.getWeight();
    final int weight2 = option2.getWeight();

    if (weight1 == 0 && weight2 == 0) { // zero/missing waits treated as a plain toggle
      return state.equals(State.on);
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
    return Weights.normalize(weight);
  }
}
