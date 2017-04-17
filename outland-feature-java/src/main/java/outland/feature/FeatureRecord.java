package outland.feature;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Map;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;

public class FeatureRecord {

  private final Feature feature;
  private final Map<String, NamespaceFeature> namespaceFeatureMap = Maps.newHashMap();
  private final Map<String, FeatureOption> namespaceControlFeatureOptionMap = Maps.newHashMap();
  private FeatureOption controlFeatureOption;

  public FeatureRecord(Feature feature) {
    this.feature = feature;
    prepare();
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

}
