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
import outland.feature.proto.State;

class FeatureRecord {

  private final Feature feature;
  private final Map<String, NamespaceFeature> namespaceFeatureMap = Maps.newHashMap();
  private final Map<String, FeatureOption> namespaceControlFeatureOptionMap = Maps.newHashMap();
  private final Map<String, OptionEvaluatorWeighted> namespaceOptionEvaluatorWeightedMap =
      Maps.newHashMap();
  private final FeatureRecordStateChecker featureRecordStateChecker;
  private OptionEvaluatorWeighted optionEvaluatorWeighted;
  private FeatureOption controlFeatureOption;

  private FeatureRecord(Feature feature) {
    this.feature = feature;
    this.featureRecordStateChecker = new FeatureRecordStateChecker();
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

  boolean enabled() {
    return feature().getState().equals(State.on);
  }

  boolean enabled(String namespace) {
    if (isDefaultNamespace(namespace)) {
      return enabled();
    }

    final NamespaceFeature target = namespace(namespace);

    if (target == null) {
      return enabled();
    }

    return target.getFeature().getState().equals(State.on);
  }

  String evaluate() {
    if (enabled()) {
      return optionEvaluatorWeighted().select().getValue();
    }

    return controlFeatureOption.getValue();
  }

  String evaluate(String namespace) {

    if (isDefaultNamespace(namespace)) {
      return evaluate();
    }


    if(! enabled(namespace)) {

      // if the namespace is off, try its control option, or fallback to the default control

      final FeatureOption featureOption = namespaceControlFeatureOptionMap.get(namespace);

      if (featureOption != null) {
        return featureOption.getValue();
      }

      return controlFeatureOption.getValue();
    }

    // from here, we're in an enabled namespace

    final OptionEvaluatorWeighted namespaceEvaluator = optionEvaluatorWeighted(namespace);

    if (namespaceEvaluator != null) {
      return namespaceEvaluator.select().getValue();
    }

    // this is probably bad/missing feature data

    final FeatureOption featureOption = namespaceControlFeatureOptionMap.get(namespace);

    if (featureOption != null) {
      return featureOption.getValue();
    }
    return controlFeatureOption.getValue();
  }

  boolean evaluateBoolean() {
    return Boolean.parseBoolean(evaluate());
  }

  boolean evaluateBoolean(String namespace) {
    return Boolean.parseBoolean(evaluate(namespace));
  }

  private boolean isDefaultNamespace(String namespace) {
    return ServerConfiguration.DEFAULT_NAMESPACE.equals(namespace);
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
        featureRecordStateChecker);
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
        Objects.equals(featureRecordStateChecker, record.featureRecordStateChecker);
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("optionEvaluatorWeighted", optionEvaluatorWeighted)
        .add("feature", feature)
        .add("namespaceFeatureMap", namespaceFeatureMap)
        .add("namespaceControlFeatureOptionMap", namespaceControlFeatureOptionMap)
        .add("namespaceOptionEvaluatorWeightedMap", namespaceOptionEvaluatorWeightedMap)
        .add("controlFeatureOption", controlFeatureOption)
        .add("featureRecordStateChecker", featureRecordStateChecker)
        .toString();
  }
}