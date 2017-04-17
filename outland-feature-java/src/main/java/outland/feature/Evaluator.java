package outland.feature;

import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.State;

public class Evaluator {

  boolean evaluate(FeatureRecord record) {
    return evaluate(record.feature().getOptions(), record.feature().getState());
  }

  boolean evaluate(FeatureRecord record, String namespace) {

    final NamespaceFeature target = record.namespace(namespace);
    if (target != null) {
      return evaluate(target.getFeature().getOptions(), target.getFeature().getState());
    } else {
      // use the feature's default options
      return evaluate(record.feature().getOptions(), record.feature().getState());
    }
  }

  private boolean evaluate(OptionCollection options, State state) {
    if (options.getOption().equals(OptionType.flag)) {
      return state.equals(State.on);
    }

    if (options.getOption().equals(OptionType.bool)) {
      return new OptionEvaluator().evaluateBooleanOptions(options, state);
    }

    return false;
  }
}
