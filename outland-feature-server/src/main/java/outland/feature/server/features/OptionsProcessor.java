package outland.feature.server.features;

import java.util.List;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;

class OptionsProcessor {

  void applyBooleanOptions(
      OptionCollection.Builder collectionBuilder, List<FeatureOption> options) {
    for (FeatureOption option : options) {
      final FeatureOption.Builder optionBuilder = FeatureOption.newBuilder().mergeFrom(option);
      optionBuilder.setType("option");
      optionBuilder.setId("opt_" + Ulid.random());
      optionBuilder.setOption(OptionType.bool);
      collectionBuilder.addItems(optionBuilder);
    }
  }
}
