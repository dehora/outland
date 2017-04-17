package outland.feature;

import outland.feature.proto.Feature;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.State;

class OptionEvaluator {

  @VisibleForTesting
  boolean evaluateBooleanOptions(Feature feature) {
    return evaluateBooleanOptions(feature.getOptions(), feature.getState());
  }

  boolean evaluateBooleanOptions(OptionCollection options, State state) {
    return new OptionEvaluatorBool().evaluateBooleanOptions(options, state);
  }
}
