package outland.feature.server.features;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

import static outland.feature.server.features.DefaultFeatureService.DEFAULT_MAXWEIGHT;

public class FeatureUpdateProcessor {

  private final VersionService versionService;

  public FeatureUpdateProcessor(VersionService versionService) {
    this.versionService = versionService;
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
