package outland.feature.server.features;

import java.util.List;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.server.Names;

class OptionsProcessor {

  void applyBooleanOptions(
      OptionCollection.Builder collectionBuilder, List<FeatureOption> options) {
    for (FeatureOption option : options) {
      applyOptions(collectionBuilder, option, OptionType.bool);
    }
  }

  void applyStringOptions(
      OptionCollection.Builder collectionBuilder, List<FeatureOption> options) {
    for (FeatureOption option : options) {
      applyOptions(collectionBuilder, option, OptionType.string);
    }
  }

  private void applyOptions(OptionCollection.Builder collectionBuilder, FeatureOption option,
      OptionType optionType) {
    final FeatureOption.Builder optionBuilder = FeatureOption.newBuilder().mergeFrom(option);
    optionBuilder.setOption(optionType);
    optionBuilder.setType(Names.optionType());
    optionBuilder.setId(Names.option());
    collectionBuilder.addItems(optionBuilder);
  }
}
