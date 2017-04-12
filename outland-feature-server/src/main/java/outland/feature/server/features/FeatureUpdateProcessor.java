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
import outland.feature.proto.FeatureVersion;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.NamespaceFeatureCollection;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.Owner;

import static outland.feature.server.features.DefaultFeatureService.DEFAULT_MAXWEIGHT;

public class FeatureUpdateProcessor {

  private final VersionService versionService;

  public FeatureUpdateProcessor(VersionService versionService) {
    this.versionService = versionService;
  }

  Feature prepareUpdateFeature(Feature existing, Feature incoming) {

    FeatureValidator featureValidator = new FeatureValidator();

    String now = TimeSupport.asString(OffsetDateTime.now());

    featureValidator.validateOptionIdsForUpdate(existing.getOptions(), incoming.getOptions());

    Feature.Builder wipBuilder = existing.toBuilder()
        .mergeFrom(incoming)
        .setUpdated(now);

    // can't change some values in update
    wipBuilder.setType("feature");
    wipBuilder.setCreated(existing.getCreated());
    wipBuilder.setId(existing.getId());
    wipBuilder.setGroup(existing.getGroup());
    wipBuilder.setKey(existing.getKey());

    applyVersion(incoming, wipBuilder);

    // process options if we received some
    if (incoming.getOptions().getOption().equals(OptionType.bool)
        && incoming.getOptions().getItemsCount() != 0) {

      // clear out options, we can rebuild them
      wipBuilder.clearOptions();
      final OptionCollection.Builder wipOptionsBuilder = OptionCollection.newBuilder();

      List<FeatureOption> options = applyOptionsUpdate(incoming, existing);
      wipOptionsBuilder.addAllItems(options);

      // can't change some values in update
      wipOptionsBuilder.setOption(existing.getOptions().getOption());
      wipOptionsBuilder.setType("options.collection");
      wipOptionsBuilder.setMaxweight(DEFAULT_MAXWEIGHT);

      wipBuilder.setOptions(wipOptionsBuilder);
    }

    if (incoming.hasOwner()) {
      Owner foundOwner = existing.getOwner();
      wipBuilder.clearOwner();
      applyOwnerUpdate(incoming, foundOwner, wipBuilder);
    }

    // a value other than none indicates the client sent something
    if (!incoming.getState().equals(Feature.State.none)) {
      wipBuilder.setState(incoming.getState());
    }

    // process namespaces if we received some
    if(incoming.hasNamespaces()) {
      applyFeatureNamespaceUpdate(existing, incoming, wipBuilder);
    }

    Feature updated = wipBuilder.build();

    // post check everything
    featureValidator.validateFeatureRegistrationThrowing(updated);
    return updated;
  }

  private List<FeatureOption> applyOptionsUpdate(Feature updated, Feature found) {

    final ArrayList<FeatureOption> results = Lists.newArrayList();

    // nothing in the update, return what we have
    if (updated.getOptions().getItemsCount() == 0) {
      results.addAll(found.getOptions().getItemsList());
      return results;
    }

    final List<FeatureOption> updatedOptionsList = updated.getOptions().getItemsList();
    final List<FeatureOption> foundOptionsList = found.getOptions().getItemsList();

    for (FeatureOption updateOption : updatedOptionsList) {
      final String updateId = updateOption.getId();

      for (FeatureOption foundOption : foundOptionsList) {
        final FeatureOption.Builder builder = foundOption.toBuilder();
        if (foundOption.getId().equals(updateId)) {
          // weight is the only field we change
          builder.setWeight(updateOption.getWeight());
          results.add(builder.build());
          break;
        }
      }
    }

    return results;
  }

  private void applyVersion(Feature registering, Feature.Builder builder) {

    VersionService.HybridLogicalTimestamp next;
    if (registering.hasVersion()) {
      next = versionService.nextVersionUpdate(new VersionService.HybridLogicalTimestamp(
          registering.getVersion().getTimestamp(),
          registering.getVersion().getCounter()));
    } else {
      next = versionService.nextVersion();
    }

    builder.setVersion(buildVersion(next));
  }

  private FeatureVersion.Builder buildVersion(VersionService.HybridLogicalTimestamp next) {
    return FeatureVersion.newBuilder()
        .setType("hlcver")
        .setCounter(next.counter())
        .setTimestamp(next.logicalTime())
        .setId(next.id());
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
      replacementOwnerBuilder.setType("featureowner");
      replacementOwnerBuilder.setId("own_" + Ulid.random());
      builder.setOwner(replacementOwnerBuilder.buildPartial());
    } else {

      final Owner.Builder wipOwnerBuilder = foundOwner.toBuilder().mergeFrom(updateOwner);
      // some fields can't be changed
      wipOwnerBuilder.setType("featureowner");
      wipOwnerBuilder.setId(foundOwner.getId());

      builder.setOwner(wipOwnerBuilder.buildPartial());
    }
  }

  private void applyFeatureNamespaceUpdate(
      Feature existing, Feature incoming, Feature.Builder wipBuilder) {

    if(! incoming.hasNamespaces()) {
      return;
    }

    wipBuilder.clearNamespaces();

    FeatureUpdateProcessor processor = new FeatureUpdateProcessor(versionService);
    List<NamespaceFeature> merged = processor.buildMergedNamespaceFeatures(existing, incoming);

    final NamespaceFeatureCollection.Builder nfcBuilder = NamespaceFeatureCollection.newBuilder()
        .setType("namespace.feature.collection")
        .addAllItems(merged);

    wipBuilder.setNamespaces(nfcBuilder);
  }

  public List<NamespaceFeature> buildMergedNamespaceFeatures(Feature existingFeature, Feature incomingFeature) {

    if(incomingFeature.getNamespaces().getItemsCount() == 0) {
      return Lists.newArrayList(existingFeature.getNamespaces().getItemsList());
    }

    FeatureValidator validator = new FeatureValidator();

    final List<NamespaceFeature> existingList = existingFeature.getNamespaces().getItemsList();
    final List<NamespaceFeature> incomingList = incomingFeature.getNamespaces().getItemsList();

    final Map<String, NamespaceFeature> existingMap = existingList.stream()
        .collect(Collectors.toMap(NamespaceFeature::getNamespace, Function.identity()));

    final Map<String, NamespaceFeature> updateMap = Maps.newHashMap();

    for (NamespaceFeature incoming: incomingList) {
      if(existingMap.containsKey(incoming.getNamespace())) {
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
      if(updateMap.containsKey(existing.getNamespace())) {
        continue;
      }
      updateMap.put(existing.getNamespace(), existing);
    }

    return ImmutableList.copyOf(updateMap.values());
  }

  public List<NamespaceFeature> buildMergedNamespaceFeatures(Feature feature, NamespaceFeature incoming) {

    if(! feature.hasNamespaces()) {
      return Lists.newArrayList(prepareNewNamespaceFeature(incoming));
    }

    final NamespaceFeatureCollection existingNamespaced = feature.getNamespaces();
    final List<NamespaceFeature> existingNamespacedItemsList = existingNamespaced.getItemsList();

    final ArrayList<NamespaceFeature> updatedNamespacedItemsList = Lists.newArrayList();

    boolean found = false;
    for (NamespaceFeature existing : existingNamespacedItemsList) {
      if(isMatching(existing, incoming)) {
        updatedNamespacedItemsList.add(mergeNamespaceFeature(existing, incoming));
        found = true;
      } else {
        updatedNamespacedItemsList.add(existing);
      }
    }

    if(! found) {
      updatedNamespacedItemsList.add(prepareNewNamespaceFeature(incoming));
    }

    return updatedNamespacedItemsList;
  }

  NamespaceFeature prepareNewNamespaceFeature(NamespaceFeature incoming) {

    final FeatureData incomingFeatureData = incoming.getFeature();

    final FeatureData.Builder featureDataBuilder = incomingFeatureData.toBuilder()
        .setId("nsfeature_"+Ulid.random())
        .setVersion(buildNextFeatureVersion())
        .setKey(incomingFeatureData.getKey())
        // always off on create
        .setState(FeatureData.State.off)
        ;


    if (incomingFeatureData.getOptions().getOption().equals(OptionType.flag)) {
      return incoming.toBuilder()
          .setType("namespace.feature")
          .setFeature(featureDataBuilder)
          .build();
    }

    featureDataBuilder.clearOptions();

    OptionCollection.Builder optionCollectionBuilder = OptionCollection.newBuilder();
    optionCollectionBuilder.setMaxweight(DEFAULT_MAXWEIGHT);
    optionCollectionBuilder.setType("options.collection");
    optionCollectionBuilder.setOption(incomingFeatureData.getOptions().getOption());

    if (incomingFeatureData.getOptions().getOption().equals(OptionType.bool)) {
      if (incomingFeatureData.getOptions().getItemsCount() != 0) {

        final List<FeatureOption> options = incomingFeatureData.getOptions().getItemsList();
        applyBooleanOptions(optionCollectionBuilder, options);
        featureDataBuilder.setOptions(optionCollectionBuilder);
      }
    }

    return incoming.toBuilder()
        .setType("namespace.feature")
        .setFeature(featureDataBuilder)
        .build();

  }

  private void applyBooleanOptions(
      OptionCollection.Builder collectionBuilder, List<FeatureOption> options) {
    for (FeatureOption option : options) {
      final FeatureOption.Builder optionBuilder = FeatureOption.newBuilder().mergeFrom(option);
      optionBuilder.setType("option");
      optionBuilder.setId("opt_" + Ulid.random());
      optionBuilder.setOption(OptionType.bool);
      collectionBuilder.addItems(optionBuilder);
    }
  }

  private NamespaceFeature mergeNamespaceFeature(
      NamespaceFeature existing, NamespaceFeature incoming) {

    final FeatureData existingFeatureData = existing.getFeature();
    final FeatureData incomingFeatureData = incoming.getFeature();

    final FeatureData.Builder featureDataBuilder =
        mergeFeatureData(existingFeatureData, incomingFeatureData);

    // process options if we received some
    if (incomingFeatureData.getOptions().getOption().equals(OptionType.bool)
        && incomingFeatureData.getOptions().getItemsCount() != 0) {

      // clear out options, we can rebuild them
      featureDataBuilder.clearOptions();
      final OptionCollection.Builder wipOptionsBuilder = OptionCollection.newBuilder();

      List<FeatureOption> merged = applyNamespaceFeatureOptionsUpdate(existingFeatureData,
          incomingFeatureData);

      wipOptionsBuilder.addAllItems(merged);

      // can't change some values in update
      wipOptionsBuilder.setOption(existingFeatureData.getOptions().getOption());
      wipOptionsBuilder.setType("options.collection");
      wipOptionsBuilder.setMaxweight(DEFAULT_MAXWEIGHT);

      featureDataBuilder.setOptions(wipOptionsBuilder);
    }

    final NamespaceFeature.Builder namespaceFeaturebuilder = existing.toBuilder();
    namespaceFeaturebuilder.setType(existing.getType());
    namespaceFeaturebuilder.setFeature(featureDataBuilder);
    return namespaceFeaturebuilder.build();
  }


    private List<FeatureOption> applyNamespaceFeatureOptionsUpdate(
        FeatureData existing, FeatureData updated) {

    final ArrayList<FeatureOption> results = Lists.newArrayList();

    // nothing in the update, return what we have
    if (updated.getOptions().getItemsCount() == 0) {
      results.addAll(existing.getOptions().getItemsList());
      return results;
    }

    final List<FeatureOption> updatedOptionsList = updated.getOptions().getItemsList();
    final List<FeatureOption> foundOptionsList = existing.getOptions().getItemsList();

    for (FeatureOption updateOption : updatedOptionsList) {
      final String updateId = updateOption.getId();

      for (FeatureOption foundOption : foundOptionsList) {
        final FeatureOption.Builder builder = foundOption.toBuilder();
        if (foundOption.getId().equals(updateId)) {
          // weight is the only field we change
          builder.setWeight(updateOption.getWeight());
          results.add(builder.build());
          break;
        }
      }
    }

    return results;
  }

  private FeatureData.Builder mergeFeatureData(FeatureData existing, FeatureData incoming) {
    return existing.toBuilder()
          .mergeFrom(incoming)
          .setId(existing.getId())
          .setVersion(nextFeatureVersion(existing.getVersion()));
  }

  private FeatureVersion nextFeatureVersion(FeatureVersion version) {
    return buildNextFeatureVersion(buildNextHybridLogicalTimestamp(version));
  }

  private FeatureVersion buildNextFeatureVersion(VersionService.HybridLogicalTimestamp nextVersion) {
    return FeatureVersion.newBuilder()
        .setType("hlcver")
        .setCounter(nextVersion.counter())
        .setTimestamp(nextVersion.logicalTime())
        .setId(nextVersion.id())
        .build()
        ;
  }

  FeatureVersion buildNextFeatureVersion() {

    final VersionService.HybridLogicalTimestamp timestamp = buildNextHybridLogicalTimestamp();

    return FeatureVersion.newBuilder()
        .setType("hlcver")
        .setCounter(timestamp.counter())
        .setTimestamp(timestamp.logicalTime())
        .setId(timestamp.id())
        .build()
        ;
  }

  VersionService.HybridLogicalTimestamp buildNextHybridLogicalTimestamp() {
    return versionService.nextVersion();
  }

  VersionService.HybridLogicalTimestamp buildNextHybridLogicalTimestamp(
      FeatureVersion version) {
    return versionService.nextVersionUpdate(new VersionService.HybridLogicalTimestamp(
        version.getTimestamp(),
        version.getCounter()));
  }

  private boolean isMatching(NamespaceFeature existing, NamespaceFeature incoming) {
    return existing.getNamespace().equals(incoming.getNamespace());
  }
}
