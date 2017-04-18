package outland.feature;

import outland.feature.proto.Feature;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.State;

class Evaluator {

  boolean evaluate(FeatureRecord record) {
    final Feature feature = record.feature();
    final State state = feature.getState();
    final OptionCollection options = feature.getOptions();
    final OptionEvaluatorWeighted evaluator = record.optionEvaluatorWeighted();

    return evaluate(options, state, evaluator);
  }

  boolean evaluate(FeatureRecord record, String namespace) {

    final NamespaceFeature target = record.namespace(namespace);

    if (target == null) {
      return evaluate(record);
    }

    final OptionCollection options = target.getFeature().getOptions();
    final State state = target.getFeature().getState();
    final OptionEvaluatorWeighted evaluator = record.optionEvaluatorWeighted(namespace);

    return evaluate(options, state, evaluator);
  }

  private boolean evaluate(
      OptionCollection options, State state, OptionEvaluatorWeighted evaluator) {
    if (options.getOption().equals(OptionType.flag)) {
      return state.equals(State.on);
    }

    if (options.getOption().equals(OptionType.bool)) {
      return new OptionEvaluatorBool().evaluateBooleanOptions(options, state, evaluator);
    }

    return false;
  }
}
