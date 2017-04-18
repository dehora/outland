package outland.feature;

import java.util.List;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.State;

class OptionEvaluatorBool {

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

    if (optionsList.get(0).getWeight() == 0 && optionsList.get(1).getWeight() == 0) {
      // zero/missing weights treated as a plain toggle
      return state.equals(State.on);
    }

    return select(optionsList);
  }

  private boolean select(List<FeatureOption> optionsList) {
    final FeatureOption select = new OptionEvaluatorWeighted(optionsList).select();
    return Boolean.parseBoolean(select.getValue());
  }
}
