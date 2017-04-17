package outland.feature.server.features;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.NamespaceFeatureCollection;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.Owner;
import outland.feature.proto.State;
import outland.feature.server.Names;

import static outland.feature.server.features.DefaultFeatureService.DEFAULT_MAXWEIGHT;

class FeatureUpdateProcessor {

  private final VersionService versionService;
  private final VersionSupport versionSupport;
  private final FeatureValidator featureValidator;
  private final OptionsProcessor optionsProcessor;

  FeatureUpdateProcessor(VersionService versionService) {
    this.versionService = versionService;
    this.versionSupport = new VersionSupport(versionService);
    this.featureValidator = new FeatureValidator();
    this.optionsProcessor = new OptionsProcessor();
  }

  Feature prepareUpdateNamespaceFeatureThrowing(Feature feature,
      List<NamespaceFeature> namespaceFeatures) {
    final NamespaceFeatureCollection.Builder nfcBuilder = NamespaceFeatureCollection.newBuilder()
        .setType(Names.namespaceFeatureCollectionType())
        .addAllItems(namespaceFeatures);
    final Feature.Builder featureBuilder = feature.toBuilder()
        .clearNamespaces()
        .setNamespaces(nfcBuilder);
    featureBuilder.setVersion(versionSupport.nextFeatureVersion(feature));
    featureBuilder.setUpdated(timeNow());
    final Feature updated = featureBuilder.build();
    featureValidator.validateFeatureRegistrationThrowing(updated);
    return updated;
  }

  Feature prepareUpdateFeatureThrowing(Feature existing, Feature incoming) {

    FeatureValidator featureValidator = new FeatureValidator();

    String now = TimeSupport.asString(OffsetDateTime.now());

    featureValidator.validateOptionIdsForUpdate(existing.getOptions(), incoming.getOptions());

    Feature.Builder wipBuilder = existing.toBuilder()
        .mergeFrom(incoming)
        .setUpdated(now);

    // can't change some values in update
    wipBuilder.setType(Names.featureType());
    wipBuilder.setCreated(existing.getCreated());
    wipBuilder.setId(existing.getId());
    wipBuilder.setGroup(existing.getGroup());
    wipBuilder.setKey(existing.getKey());

    wipBuilder.setVersion(versionSupport.nextFeatureVersion(incoming));

    // process options if we received some
    if (incoming.getOptions().getOption().equals(OptionType.bool)
        && incoming.getOptions().getItemsCount() != 0) {

      // clear out options, we can rebuild them
      wipBuilder.clearOptions();
      final OptionCollection.Builder wipOptionsBuilder = OptionCollection.newBuilder();

      List<FeatureOption> options = buildFeaturesOptionsUpdate(existing, incoming);
      wipOptionsBuilder.addAllItems(options);

      // can't change some values in update
      wipOptionsBuilder.setOption(existing.getOptions().getOption());
      wipOptionsBuilder.setType(Names.optionCollectionType());
      wipOptionsBuilder.setMaxweight(DEFAULT_MAXWEIGHT);

      wipBuilder.setOptions(wipOptionsBuilder);
    }

    if (incoming.hasOwner()) {
      Owner foundOwner = existing.getOwner();
      wipBuilder.clearOwner();
      applyOwnerUpdate(incoming, foundOwner, wipBuilder);
    }

    // a value other than none indicates the client sent something
    if (!incoming.getState().equals(State.none)) {
      wipBuilder.setState(incoming.getState());
    }

    // process namespaces if we received some
    if (incoming.hasNamespaces()) {
      applyFeatureNamespaceUpdate(existing, incoming, wipBuilder);
    }

    Feature updated = wipBuilder.build();

    // post check everything
    featureValidator.validateFeatureUpdateThrowing(updated);
    return updated;
  }

  List<NamespaceFeature> buildMergedNamespaceFeatures(Feature feature, NamespaceFeature incoming) {

    if (!feature.hasNamespaces()) {
      return Lists.newArrayList(prepareNewNamespaceFeature(incoming));
    }

    final NamespaceFeatureCollection existingNamespaced = feature.getNamespaces();
    final List<NamespaceFeature> existingNamespacedItemsList = existingNamespaced.getItemsList();

    final ArrayList<NamespaceFeature> updatedNamespacedItemsList = Lists.newArrayList();

    boolean found = false;
    for (NamespaceFeature existing : existingNamespacedItemsList) {
      if (isMatching(existing, incoming)) {
        updatedNamespacedItemsList.add(mergeNamespaceFeature(existing, incoming));
        found = true;
      } else {
        updatedNamespacedItemsList.add(existing);
      }
    }

    if (!found) {
      updatedNamespacedItemsList.add(prepareNewNamespaceFeature(incoming));
    }

    return updatedNamespacedItemsList;
  }

  private List<FeatureOption> buildNamespaceFeatureOptionsUpdate(
      FeatureData existing, FeatureData incoming) {
    return buildOptionsUpdate(existing.getOptions(), incoming.getOptions());
  }

  private List<FeatureOption> buildFeaturesOptionsUpdate(Feature existing, Feature incoming) {
    return buildOptionsUpdate(existing.getOptions(), incoming.getOptions());
  }

  private List<FeatureOption> buildOptionsUpdate(
      OptionCollection existingOptions, OptionCollection incomingOptions) {

    final ArrayList<FeatureOption> results = Lists.newArrayList();

    if (incomingOptions.getItemsCount() == 0) {
      results.addAll(existingOptions.getItemsList());
      return results;
    }

    final List<FeatureOption> updatedOptionsList = incomingOptions.getItemsList();
    final List<FeatureOption> foundOptionsList = existingOptions.getItemsList();

    for (FeatureOption updateOption : updatedOptionsList) {
      final String updateId = updateOption.getId();
      for (FeatureOption foundOption : foundOptionsList) {
        final FeatureOption.Builder builder = foundOption.toBuilder();
        if (foundOption.getId().equals(updateId)) {
          builder.setWeight(updateOption.getWeight());
          results.add(builder.build());
          break;
        }
      }
    }
    return results;
  }

  private void applyOwnerUpdate(Feature updates, Owner foundOwner, Feature.Builder builder) {
    final Owner updateOwner = updates.getOwner();
    new FeatureValidator().validateOwner(updateOwner);

    if ((!Strings.isNullOrEmpty(updateOwner.getEmail())
        && !updateOwner.getEmail().equals(foundOwner.getEmail()))
        &&
        (!Strings.isNullOrEmpty(updateOwner.getUsername())
            && !updateOwner.getUsername().equals(foundOwner.getEmail()))) {
      // treat as new owner
      final Owner.Builder replacementOwnerBuilder = updateOwner.toBuilder();
      replacementOwnerBuilder.setType(Names.ownerType());
      replacementOwnerBuilder.setId(Names.owner());
      builder.setOwner(replacementOwnerBuilder.buildPartial());
    } else {

      final Owner.Builder wipOwnerBuilder = foundOwner.toBuilder().mergeFrom(updateOwner);
      // some fields can't be changed
      wipOwnerBuilder.setType(Names.ownerType());
      wipOwnerBuilder.setId(foundOwner.getId());

      builder.setOwner(wipOwnerBuilder.buildPartial());
    }
  }

  private void applyFeatureNamespaceUpdate(
      Feature existing, Feature incoming, Feature.Builder wipBuilder) {

    if (!incoming.hasNamespaces()) {
      return;
    }

    wipBuilder.clearNamespaces();

    FeatureUpdateProcessor processor = new FeatureUpdateProcessor(versionService);
    List<NamespaceFeature> merged = processor.buildMergedNamespaceFeatures(existing, incoming);

    final NamespaceFeatureCollection.Builder nfcBuilder = NamespaceFeatureCollection.newBuilder()
        .setType(Names.namespaceFeatureCollectionType())
        .addAllItems(merged);

    wipBuilder.setNamespaces(nfcBuilder);
  }

  private List<NamespaceFeature> buildMergedNamespaceFeatures(Feature existingFeature,
      Feature incomingFeature) {

    if (incomingFeature.getNamespaces().getItemsCount() == 0) {
      return Lists.newArrayList(existingFeature.getNamespaces().getItemsList());
    }

    FeatureValidator validator = new FeatureValidator();

    final List<NamespaceFeature> existingList = existingFeature.getNamespaces().getItemsList();
    final List<NamespaceFeature> incomingList = incomingFeature.getNamespaces().getItemsList();

    final Map<String, NamespaceFeature> existingMap = existingList.stream()
        .collect(Collectors.toMap(NamespaceFeature::getNamespace, Function.identity()));

    final Map<String, NamespaceFeature> updateMap = Maps.newHashMap();

    for (NamespaceFeature incoming : incomingList) {
      if (existingMap.containsKey(incoming.getNamespace())) {
        // merge
        final NamespaceFeature existing = existingMap.get(incoming.getNamespace());
        final FeatureData existingFeatureData = existing.getFeature();
        final FeatureData incomingFeatureData = incoming.getFeature();
        validator.validateFeatureDataMergeCandidates(existingFeatureData, incomingFeatureData);
        updateMap.put(incoming.getNamespace(), mergeNamespaceFeature(existing, incoming));
      } else {
        validator.validateFeatureDataNewCandidateThrowing(existingFeature, incoming);
        updateMap.put(incoming.getNamespace(), prepareNewNamespaceFeature(incoming));
      }
    }

    for (NamespaceFeature existing : existingList) {
      if (updateMap.containsKey(existing.getNamespace())) {
        continue;
      }
      updateMap.put(existing.getNamespace(), existing);
    }

    return ImmutableList.copyOf(updateMap.values());
  }

  private NamespaceFeature prepareNewNamespaceFeature(NamespaceFeature incoming) {

    final FeatureData incomingFeatureData = incoming.getFeature();

    final FeatureData.Builder featureDataBuilder = incomingFeatureData.toBuilder()
        .setId(Names.namespaceFeature())
        .setType(Names.namespaceFeatureType())
        .setVersion(versionSupport.nextFeatureVersion())
        .setKey(incomingFeatureData.getKey())
        // always off on create
        .setState(State.off);

    if (incomingFeatureData.getOptions().getOption().equals(OptionType.flag)) {
      return incoming.toBuilder()
          .setType(Names.namespaceFeatureType())
          .setFeature(featureDataBuilder)
          .build();
    }

    featureDataBuilder.clearOptions();

    OptionCollection.Builder optionCollectionBuilder = OptionCollection.newBuilder();
    optionCollectionBuilder.setMaxweight(DEFAULT_MAXWEIGHT);
    optionCollectionBuilder.setType(Names.optionCollectionType());
    optionCollectionBuilder.setOption(incomingFeatureData.getOptions().getOption());
    optionCollectionBuilder.setControl(incomingFeatureData.getOptions().getControl());

    if (isBoolOption(incomingFeatureData)) {
      if (incomingFeatureData.getOptions().getItemsCount() != 0) {

        final List<FeatureOption> options = incomingFeatureData.getOptions().getItemsList();
        optionsProcessor.applyBooleanOptions(optionCollectionBuilder, options);
        featureDataBuilder.setOptions(optionCollectionBuilder);
      }
    }

    if(isStringOption(incomingFeatureData)) {
      if (incomingFeatureData.getOptions().getItemsCount() != 0) {
        final List<FeatureOption> options = incomingFeatureData.getOptions().getItemsList();
        optionsProcessor.applyBooleanOptions(optionCollectionBuilder, options);
        featureDataBuilder.setOptions(optionCollectionBuilder);
      }
    }

    return incoming.toBuilder()
        .setType(Names.namespaceFeatureType())
        .setFeature(featureDataBuilder)
        .build();
  }

  private NamespaceFeature mergeNamespaceFeature(
      NamespaceFeature existing, NamespaceFeature incoming) {

    final FeatureData existingFeatureData = existing.getFeature();
    final FeatureData incomingFeatureData = incoming.getFeature();

    final FeatureData.Builder featureDataBuilder =
        mergeFeatureData(existingFeatureData, incomingFeatureData);

    featureDataBuilder.setType(Names.namespaceFeatureType());

    // process options if we received some
    if (isBoolOption(incomingFeatureData)
        && incomingFeatureData.getOptions().getItemsCount() != 0) {

      // clear out options, we can rebuild them
      featureDataBuilder.clearOptions();
      final OptionCollection.Builder wipOptionsBuilder = OptionCollection.newBuilder();

      List<FeatureOption> merged = buildNamespaceFeatureOptionsUpdate(existingFeatureData,
          incomingFeatureData);

      wipOptionsBuilder.addAllItems(merged);

      // can't change some values in update
      wipOptionsBuilder.setOption(existingFeatureData.getOptions().getOption());
      wipOptionsBuilder.setType(Names.optionCollectionType());
      wipOptionsBuilder.setMaxweight(DEFAULT_MAXWEIGHT);

      featureDataBuilder.setOptions(wipOptionsBuilder);
    }

    if(isStringOption(incomingFeatureData)
        && incomingFeatureData.getOptions().getItemsCount() != 0) {
      featureDataBuilder.clearOptions();
      final OptionCollection.Builder wipOptionsBuilder = OptionCollection.newBuilder();

      List<FeatureOption> merged = buildNamespaceFeatureOptionsUpdate(existingFeatureData,
          incomingFeatureData);

      wipOptionsBuilder.addAllItems(merged);

      wipOptionsBuilder.setOption(existingFeatureData.getOptions().getOption());
      wipOptionsBuilder.setType(Names.optionCollectionType());
      wipOptionsBuilder.setMaxweight(DEFAULT_MAXWEIGHT);

      featureDataBuilder.setOptions(wipOptionsBuilder);
    }

    final NamespaceFeature.Builder namespaceFeaturebuilder = existing.toBuilder();
    namespaceFeaturebuilder.setType(existing.getType());
    namespaceFeaturebuilder.setFeature(featureDataBuilder);
    return namespaceFeaturebuilder.build();
  }

  private boolean isBoolOption(FeatureData incomingFeatureData) {
    return incomingFeatureData.getOptions().getOption().equals(OptionType.bool);
  }

  private boolean isStringOption(FeatureData featureData) {
    return featureData.getOptions().getOption().equals(OptionType.string);
  }

  private FeatureData.Builder mergeFeatureData(FeatureData existing, FeatureData incoming) {
    return existing.toBuilder()
        .mergeFrom(incoming)
        .setId(existing.getId())
        .setType(Names.namespaceFeatureType())
        .setVersion(versionSupport.nextFeatureVersion(existing.getVersion()));
  }

  private boolean isMatching(NamespaceFeature existing, NamespaceFeature incoming) {
    return existing.getNamespace().equals(incoming.getNamespace());
  }

  private String timeNow() {
    return TimeSupport.asString(OffsetDateTime.now());
  }
}
