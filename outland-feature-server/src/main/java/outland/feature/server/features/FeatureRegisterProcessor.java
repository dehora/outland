package outland.feature.server.features;

import com.google.common.collect.Lists;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.NamespaceFeatureCollection;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.Owner;
import outland.feature.server.Names;

class FeatureRegisterProcessor {

  private static final int DEFAULT_MAXWEIGHT = FeatureService.DEFAULT_MAXWEIGHT;

  private final VersionService versionService;
  private final VersionSupport versionSupport;
  private final OptionsProcessor optionsProcessor;

  FeatureRegisterProcessor(VersionService versionService) {
    this.versionService = versionService;
    this.versionSupport = new VersionSupport(versionService);
    this.optionsProcessor = new OptionsProcessor();
  }

  Feature prepareNewFeature(Feature registering) {
    FeatureValidator featureValidator = new FeatureValidator();

    // catch bad input before merging
    featureValidator.validateFeatureRegistrationThrowing(registering);

    OffsetDateTime now = OffsetDateTime.now();
    String id = Names.feature(now);
    String created = TimeSupport.asString(now);

    Feature.Builder builder = registering.toBuilder();
    builder.setType(Names.featureType());
    builder.setId(id);
    builder.setCreated(created);
    builder.setUpdated(builder.getCreated());
    builder.setState(Feature.State.off); // always disabled on registerFeature

    builder.setVersion(versionSupport.nextFeatureVersion(registering));
    builder.clearOptions();
    applyOptionsRegister(registering, builder);

    builder.clearOwner();
    applyOwnerRegister(registering, builder);

    builder.clearNamespaces();
    applyFeatureNamespaceRegister(registering, builder);

    return builder.build();
  }

  private void applyOptionsRegister(Feature feature, Feature.Builder builder) {

    if (feature.getOptions().getOption().equals(OptionType.flag)) {
      // flags don't have weighted options
      return;
    }

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder();
    collectionBuilder.setMaxweight(DEFAULT_MAXWEIGHT);
    collectionBuilder.setType(Names.optionCollectionType());
    collectionBuilder.setOption(feature.getOptions().getOption());

    if (feature.getOptions().getOption().equals(OptionType.bool)) {
      if (feature.getOptions().getItemsCount() != 0) {

        final List<FeatureOption> options = feature.getOptions().getItemsList();
        optionsProcessor.applyBooleanOptions(collectionBuilder, options);
        builder.setOptions(collectionBuilder);
      } else {

        collectionBuilder.addItems(FeatureOption.newBuilder()
            .setType(Names.optionType())
            .setId(Names.option())
            .setName("false")
            .setValue("false")
            .setOption(OptionType.bool)
            .setWeight(5_000));

        collectionBuilder.addItems(FeatureOption.newBuilder()
            .setType(Names.optionType())
            .setId(Names.option())
            .setName("true")
            .setValue("true")
            .setOption(OptionType.bool)
            .setWeight(5_000));

        builder.setOptions(collectionBuilder);
      }
    }
  }

  private void applyOwnerRegister(Feature registering, Feature.Builder builder) {
    final Owner owner = registering.getOwner();
    final Owner.Builder ownerBuilder = owner.toBuilder();
    ownerBuilder.setType(Names.ownerType());
    ownerBuilder.setId(Names.owner());
    builder.setOwner(ownerBuilder.buildPartial());
  }

  private void applyNamespaceFeatureOptionsRegister(
      NamespaceFeature incoming, FeatureData.Builder featureDataBuilder) {

    if (incoming.getFeature().getOptions().getOption().equals(OptionType.flag)) {
      // flags don't have weighted options
      return;
    }

    OptionCollection.Builder optionCollectionBuilder = OptionCollection.newBuilder();
    optionCollectionBuilder.setMaxweight(DEFAULT_MAXWEIGHT);
    optionCollectionBuilder.setType(Names.optionCollectionType());
    optionCollectionBuilder.setOption(incoming.getFeature().getOptions().getOption());

    if (incoming.getFeature().getOptions().getOption().equals(OptionType.bool)) {

      if (incoming.getFeature().getOptions().getItemsCount() != 0) {

        final List<FeatureOption> options = incoming.getFeature().getOptions().getItemsList();
        optionsProcessor.applyBooleanOptions(optionCollectionBuilder, options);
        featureDataBuilder.setOptions(optionCollectionBuilder);
      }
    }
  }

  private void applyFeatureNamespaceRegister(Feature registering, Feature.Builder builder) {

    if (!registering.hasNamespaces()) {
      return;
    }

    final FeatureUpdateProcessor processor = new FeatureUpdateProcessor(versionService);

    final NamespaceFeatureCollection registeringNamespaced = registering.getNamespaces();
    final List<NamespaceFeature> incomingFeaturesList = registeringNamespaced.getItemsList();
    final ArrayList<NamespaceFeature> registeringNamespaceFeatures = Lists.newArrayList();

    for (NamespaceFeature incoming : incomingFeaturesList) {

      final FeatureData.Builder featureDataBuilder = FeatureData.newBuilder()
          .mergeFrom(incoming.getFeature())
          .setType(Names.namespaceFeatureType())
          .setId(Names.namespaceFeature())
          .setVersion(versionSupport.nextFeatureVersion())
          .setKey(incoming.getFeature().getKey())
          // always off on create
          .setState(FeatureData.State.off);

      featureDataBuilder.clearOptions();
      applyNamespaceFeatureOptionsRegister(incoming, featureDataBuilder);

      final NamespaceFeature.Builder namespaceFeatureBuilder = NamespaceFeature.newBuilder();

      namespaceFeatureBuilder.mergeFrom(incoming)
          .setType(Names.namespaceFeatureType())
          .setNamespace(incoming.getNamespace())
          .setFeature(featureDataBuilder);

      registeringNamespaceFeatures.add(namespaceFeatureBuilder.buildPartial());
    }

    NamespaceFeatureCollection.Builder nfcBuilder = NamespaceFeatureCollection.newBuilder()
        .setType(Names.optionCollectionType())
        .addAllItems(registeringNamespaceFeatures);

    builder.setNamespaces(nfcBuilder);
  }
}
