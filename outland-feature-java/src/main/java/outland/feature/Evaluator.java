package outland.feature;

import java.util.List;
import outland.feature.proto.Feature;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.NamespaceFeatureCollection;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.State;

public class Evaluator {

  boolean evaluate(Feature feature, String namespace) {

    final NamespaceFeatureCollection namespaces = feature.getNamespaces();
    final List<NamespaceFeature> features = namespaces.getItemsList();

    NamespaceFeature target = null;

    // todo: o(1) this

    for (NamespaceFeature namespaceFeature : features) {
      if(namespaceFeature.getNamespace().equals(namespace)) {
        target = namespaceFeature;
        break;
      }
    }

    if(target == null) {
      return evaluate(feature.getOptions(), feature.getState());
    } else {
      return evaluate(target.getFeature().getOptions(), target.getFeature().getState());
    }
  }

  boolean evaluate(Feature feature) {
    return evaluate(feature.getOptions(), feature.getState());
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

  public String evaluateStringOption(Feature feature) {
    return new OptionEvaluator().evaluateStringOptions(feature.getOptions(), feature.getState());
  }

  public String evaluateStringOption(Feature feature, String namespace) {
    return null;
  }
}
