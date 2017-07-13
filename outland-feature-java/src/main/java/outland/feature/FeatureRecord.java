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
import outland.feature.proto.Status;

class FeatureRecord {

  private final Feature feature;
  private final Map<String, NamespaceFeature> namespaceFeatureMap = Maps.newHashMap();
  private final Map<String, FeatureOption> namespaceControlFeatureOptionMap = Maps.newHashMap();
  private final Map<String, OptionSelectorWeighted> namespaceOptionSelectorWeightedMap =
      Maps.newHashMap();
  private OptionSelectorWeighted optionSelectorWeighted;
  private FeatureOption controlFeatureOption;

  private FeatureRecord(Feature feature) {
    this.feature = feature;
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

  OptionSelectorWeighted optionSelectorWeighted() {
    return optionSelectorWeighted;
  }

  OptionSelectorWeighted optionSelectorWeighted(String namespace) {
    return namespaceOptionSelectorWeightedMap.get(namespace);
  }

  boolean enabled() {
    return feature().getStatus().equals(Status.on);
  }

  boolean enabled(String namespace) {
    if (isDefaultNamespace(namespace)) {
      return enabled();
    }

    final NamespaceFeature target = namespace(namespace);

    if (target == null) {
      return enabled();
    }

    return target.getFeature().getStatus().equals(Status.on);
  }

  String select() {
    if (enabled()) {
      return optionSelectorWeighted().select().getValue();
    }

    return controlFeatureOption.getValue();
  }

  String select(String namespace) {

    if (isDefaultNamespace(namespace)) {
      return select();
    }

    if (!enabled(namespace)) {
      // if the namespace is off, try its control option, or fallback to the default control
      return selectControlOption(namespace);
    }

    // from here, we're in an enabled namespace

    final OptionSelectorWeighted selector = optionSelectorWeighted(namespace);

    if (selector != null) {
      return selector.select().getValue();
    }

    // this is probably bad/missing feature data
    return selectControlOption(namespace);
  }

  boolean selectBoolean() {
    return Boolean.parseBoolean(select());
  }

  boolean selectBoolean(String namespace) {
    return Boolean.parseBoolean(select(namespace));
  }

  private String selectControlOption(String namespace) {
    final FeatureOption featureOption = namespaceControlFeatureOptionMap.get(namespace);

    if (featureOption != null) {
      return featureOption.getValue();
    }

    return controlFeatureOption.getValue();
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
      optionSelectorWeighted = new OptionSelectorWeighted(options.getItemsList());
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
        namespaceOptionSelectorWeightedMap.put(
            namespaceFeature.getNamespace(), new OptionSelectorWeighted(options.getItemsList()));
      }
    }
  }

  private boolean matchesControlOption(String control, FeatureOption featureOption) {
    return !Strings.isNullOrEmpty(control) && featureOption.getKey().equals(control);
  }

  private boolean isOptionalType() {
    return isOptionalType(feature.getOptions());
  }

  private boolean isOptionalType(OptionCollection options) {
    return !options.getOption().equals(OptionType.flag);
  }

  @Override public int hashCode() {
    return Objects.hash(optionSelectorWeighted, feature, namespaceFeatureMap,
        namespaceControlFeatureOptionMap, namespaceOptionSelectorWeightedMap,
        controlFeatureOption);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FeatureRecord record = (FeatureRecord) o;
    return Objects.equals(optionSelectorWeighted, record.optionSelectorWeighted) &&
        Objects.equals(feature, record.feature) &&
        Objects.equals(namespaceFeatureMap, record.namespaceFeatureMap) &&
        Objects.equals(namespaceControlFeatureOptionMap,
            record.namespaceControlFeatureOptionMap) &&
        Objects.equals(namespaceOptionSelectorWeightedMap,
            record.namespaceOptionSelectorWeightedMap) &&
        Objects.equals(controlFeatureOption, record.controlFeatureOption);
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("optionSelectorWeighted", optionSelectorWeighted)
        .add("feature", feature)
        .add("namespaceFeatureMap", namespaceFeatureMap)
        .add("namespaceControlFeatureOptionMap", namespaceControlFeatureOptionMap)
        .add("namespaceOptionSelectorWeightedMap", namespaceOptionSelectorWeightedMap)
        .add("controlFeatureOption", controlFeatureOption)
        .toString();
  }
}