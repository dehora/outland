package outland.feature;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;

class FeatureRecord {

  private final Feature feature;
  private final Map<String, NamespaceFeature> namespaceFeatureMap = Maps.newHashMap();
  private final Map<String, FeatureOption> namespaceControlFeatureOptionMap = Maps.newHashMap();
  private final Map<String, OptionEvaluatorWeighted> namespaceOptionEvaluatorWeightedMap =
      Maps.newHashMap();
  private final Evaluator evaluator;
  private OptionEvaluatorWeighted optionEvaluatorWeighted;
  private FeatureOption controlFeatureOption;

  private FeatureRecord(Feature feature) {
    this.feature = feature;
    this.evaluator = new Evaluator();
    prepare();
  }

  static FeatureRecord build(Feature feature) {
    return new FeatureRecord(feature);
  }

  public Feature feature() {
    return feature;
  }

  NamespaceFeature namespace(String namespace) {
    return namespaceFeatureMap.get(namespace);
  }

  FeatureOption controlFeatureOption() {
    return controlFeatureOption;
  }

  FeatureOption controlFeatureOption(String namespace) {
    return namespaceControlFeatureOptionMap.get(namespace);
  }

  OptionEvaluatorWeighted optionEvaluatorWeighted() {
    return optionEvaluatorWeighted;
  }

  OptionEvaluatorWeighted optionEvaluatorWeighted(String namespace) {
    return namespaceOptionEvaluatorWeightedMap.get(namespace);
  }

  boolean evaluate(String namespace) {
    if (namespace.equals(ServerConfiguration.DEFAULT_NAMESPACE)) {
      return evaluator.evaluate(this);
    }
    return evaluator.evaluate(this, namespace);
  }

  private void prepare() {
    prepareDefault();
    prepareNamespaces();
  }

  private void prepareDefault() {
    if (isOptionalType()) {
      final OptionCollection options = feature.getOptions();
      final String control = options.getControl();
      for (FeatureOption featureOption : options.getItemsList()) {
        if (matchesControlOption(control, featureOption)) {
          controlFeatureOption = featureOption;
          break;
        }
      }
      optionEvaluatorWeighted = new OptionEvaluatorWeighted(options.getItemsList());
    }
  }

  private void prepareNamespaces() {
    for (NamespaceFeature namespaceFeature : feature.getNamespaces().getItemsList()) {
      namespaceFeatureMap.put(namespaceFeature.getNamespace(), namespaceFeature);
      final OptionCollection options = namespaceFeature.getFeature().getOptions();
      if (isOptionalType(options)) {
        final String control = options.getControl();
        for (FeatureOption option : options.getItemsList()) {
          if (matchesControlOption(control, option)) {
            namespaceControlFeatureOptionMap.put(namespaceFeature.getNamespace(), option);
            break;
          }
        }
        namespaceOptionEvaluatorWeightedMap.put(
            namespaceFeature.getNamespace(), new OptionEvaluatorWeighted(options.getItemsList()));
      }
    }
  }

  private boolean matchesControlOption(String control, FeatureOption featureOption) {
    return !Strings.isNullOrEmpty(control) && featureOption.getName().equals(control);
  }

  private boolean isOptionalType() {
    return isOptionalType(feature.getOptions());
  }

  private boolean isOptionalType(OptionCollection options) {
    return !options.getOption().equals(OptionType.flag);
  }

  @Override public int hashCode() {
    return Objects.hash(optionEvaluatorWeighted, feature, namespaceFeatureMap,
        namespaceControlFeatureOptionMap, namespaceOptionEvaluatorWeightedMap, controlFeatureOption,
        evaluator);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FeatureRecord record = (FeatureRecord) o;
    return Objects.equals(optionEvaluatorWeighted, record.optionEvaluatorWeighted) &&
        Objects.equals(feature, record.feature) &&
        Objects.equals(namespaceFeatureMap, record.namespaceFeatureMap) &&
        Objects.equals(namespaceControlFeatureOptionMap,
            record.namespaceControlFeatureOptionMap) &&
        Objects.equals(namespaceOptionEvaluatorWeightedMap,
            record.namespaceOptionEvaluatorWeightedMap) &&
        Objects.equals(controlFeatureOption, record.controlFeatureOption) &&
        Objects.equals(evaluator, record.evaluator);
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("optionEvaluatorWeighted", optionEvaluatorWeighted)
        .add("feature", feature)
        .add("namespaceFeatureMap", namespaceFeatureMap)
        .add("namespaceControlFeatureOptionMap", namespaceControlFeatureOptionMap)
        .add("namespaceOptionEvaluatorWeightedMap", namespaceOptionEvaluatorWeightedMap)
        .add("controlFeatureOption", controlFeatureOption)
        .add("evaluator", evaluator)
        .toString();
  }
}
