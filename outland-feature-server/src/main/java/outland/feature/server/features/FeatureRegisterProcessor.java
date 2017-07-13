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
import outland.feature.proto.Status;
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

    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .mergeFrom(registering.getData())
        .setStatus(Status.off) // always disabled on registerFeature
        .setVersion(versionSupport.nextFeatureVersion(registering));

    Feature.Builder builder = registering.toBuilder();
    builder.setId(id);
    builder.setCreated(created);
    builder.setUpdated(builder.getCreated());
    applyOptionsRegister(registering, featureData);

    builder.setData(featureData);

    builder.clearOwner();
    applyOwnerRegister(registering, builder);

    builder.clearNamespaces();
    applyFeatureNamespaceRegister(registering, builder);

    return builder.build();
  }

  private void applyOptionsRegister(Feature registering, FeatureData.Builder builder) {

    if (registering.getData().getOptions().getOption().equals(OptionType.flag)) {
      // flags don't have weighted options
      return;
    }

    OptionCollection.Builder collectionBuilder = OptionCollection.newBuilder();
    collectionBuilder.setMaxweight(DEFAULT_MAXWEIGHT);
    collectionBuilder.setOption(registering.getData().getOptions().getOption());
    collectionBuilder.setControl(registering.getData().getOptions().getControl());

    if (isBoolOption(registering)) {
      applyBooleanOptions(registering, builder, collectionBuilder);
    }

    if (isStringOption(registering)) {
      applyStringOptions(registering, builder, collectionBuilder);
    }
  }

  private boolean isStringOption(Feature feature) {
    return feature.getData().getOptions().getOption().equals(OptionType.string);
  }

  private boolean isStringOption(NamespaceFeature incoming) {
    return incoming.getData().getOptions().getOption().equals(OptionType.string);
  }

  private boolean isBoolOption(Feature feature) {
    return feature.getData().getOptions().getOption().equals(OptionType.bool);
  }

  private boolean isBoolOption(NamespaceFeature incoming) {
    return incoming.getData().getOptions().getOption().equals(OptionType.bool);
  }

  private void applyBooleanOptions(
      Feature feature, FeatureData.Builder featureDataBuilder, OptionCollection.Builder collectionBuilder) {


    final FeatureData.Builder featureData = FeatureData.newBuilder()
        .mergeFrom(feature.getData())
        .setVersion(versionSupport.nextFeatureVersion(feature));

    if (feature.getData().getOptions().getItemsCount() != 0) {

      final List<FeatureOption> options = feature.getData().getOptions().getItemsList();
      optionsProcessor.applyBooleanOptions(collectionBuilder, options);


      featureDataBuilder.setOptions(collectionBuilder);
    } else {

      collectionBuilder.addItems(FeatureOption.newBuilder()
          .setId(Names.option())
          .setKey("false")
          .setValue("false")
          .setOption(OptionType.bool)
          .setWeight(5_000));

      collectionBuilder.addItems(FeatureOption.newBuilder()
          .setId(Names.option())
          .setKey("true")
          .setValue("true")
          .setOption(OptionType.bool)
          .setWeight(5_000));

      featureDataBuilder.setOptions(collectionBuilder);
    }
  }

  private void applyStringOptions(
      Feature feature, FeatureData.Builder featureDataBuilder, OptionCollection.Builder collectionBuilder) {

    final List<FeatureOption> options = feature.getData().getOptions().getItemsList();
    optionsProcessor.applyStringOptions(collectionBuilder, options);
    featureDataBuilder.setOptions(collectionBuilder);
  }

  private void applyOwnerRegister(Feature registering, Feature.Builder builder) {
    final Owner owner = registering.getOwner();
    final Owner.Builder ownerBuilder = owner.toBuilder();
    ownerBuilder.setId(Names.owner());
    builder.setOwner(ownerBuilder.buildPartial());
  }

  private void applyNamespaceFeatureOptionsRegister(
      NamespaceFeature incoming, FeatureData.Builder featureDataBuilder) {

    if (incoming.getData().getOptions().getOption().equals(OptionType.flag)) {
      // flags don't have weighted options
      return;
    }

    OptionCollection.Builder optionCollectionBuilder = OptionCollection.newBuilder();
    optionCollectionBuilder.setMaxweight(DEFAULT_MAXWEIGHT);
    optionCollectionBuilder.setOption(incoming.getData().getOptions().getOption());
    optionCollectionBuilder.setControl(incoming.getData().getOptions().getControl());

    if (isBoolOption(incoming)) {

      if (incoming.getData().getOptions().getItemsCount() != 0) {

        final List<FeatureOption> options = incoming.getData().getOptions().getItemsList();
        optionsProcessor.applyBooleanOptions(optionCollectionBuilder, options);
        featureDataBuilder.setOptions(optionCollectionBuilder);
      }
    }

    if (isStringOption(incoming)) {

      final List<FeatureOption> options = incoming.getData().getOptions().getItemsList();
      optionsProcessor.applyStringOptions(optionCollectionBuilder, options);
      featureDataBuilder.setOptions(optionCollectionBuilder);
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
          .mergeFrom(incoming.getData())
          .setVersion(versionSupport.nextFeatureVersion())
          // always off on create
          .setStatus(Status.off);

      featureDataBuilder.clearOptions();
      applyNamespaceFeatureOptionsRegister(incoming, featureDataBuilder);

      final NamespaceFeature.Builder namespaceFeatureBuilder = NamespaceFeature.newBuilder();

      namespaceFeatureBuilder.mergeFrom(incoming)
          .setNamespace(incoming.getNamespace())
          .setData(featureDataBuilder);

      registeringNamespaceFeatures.add(namespaceFeatureBuilder.buildPartial());
    }

    NamespaceFeatureCollection.Builder nfcBuilder = NamespaceFeatureCollection.newBuilder()
        .addAllItems(registeringNamespaceFeatures);

    builder.setNamespaces(nfcBuilder);
  }
}
