package outland.feature;

import outland.feature.proto.Feature;
import outland.feature.proto.OptionType;
import outland.feature.proto.State;

public class Evaluator {


  boolean evaluate(Feature feature) {
    if (feature.getOptions().getOption().equals(OptionType.flag)) {
      return feature.getState().equals(State.on);
    }

    if (feature.getOptions().getOption().equals(OptionType.bool)) {
      return new OptionEvaluator().evaluateBooleanOptions(feature);
    }

    return false;
  }

}
