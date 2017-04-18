package outland.feature;

import java.util.List;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.State;

class OptionEvaluatorBool {

  boolean evaluateBooleanOptions(
      OptionCollection options,
      State state,
      OptionEvaluatorWeighted optionEvaluatorWeighted
  ) {
    if (!options.getOption().equals(OptionType.bool)) {
      return false; // todo: throw this?
    }

    if (!state.equals(State.on)) {
      return false; // todo: replace with a fallback when we add fallbacks for options
    }

    final List<FeatureOption> optionsList = options.getItemsList();

    if (optionsList.get(0).getWeight() == 0 && optionsList.get(1).getWeight() == 0) {
      // zero/missing weights treated as a plain toggle
      return state.equals(State.on);
    }

    return Boolean.parseBoolean(optionEvaluatorWeighted.select().getValue());
  }
}