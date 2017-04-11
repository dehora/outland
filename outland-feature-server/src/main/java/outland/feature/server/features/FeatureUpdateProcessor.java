package outland.feature.server.features;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.FeatureVersion;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.NamespaceFeatureCollection;

public class FeatureUpdateProcessor {

  private final VersionService versionService;

  public FeatureUpdateProcessor(VersionService versionService) {
    this.versionService = versionService;
  }

  public List<NamespaceFeature> mergeNamespaceFeatures(Feature feature, NamespaceFeature incoming) {

    if(! feature.hasNamespaced()) {
      return Lists.newArrayList(prepareNewNamespaceFeature(incoming));
    }

    final NamespaceFeatureCollection existingNamespaced = feature.getNamespaced();
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

  private NamespaceFeature prepareNewNamespaceFeature(NamespaceFeature incoming) {

    final FeatureData.Builder builder = incoming.getFeature().toBuilder()
        .setVersion(buildNextFeatureVersion(
            buildNextHybridLogicalTimestamp()))
        .setId("nsfeat_"+Ulid.random());

    return incoming.toBuilder().setType("namespace.feature").setFeature(builder).build();

  }

  private NamespaceFeature mergeNamespaceFeature(
      NamespaceFeature existing, NamespaceFeature incoming) {

    final FeatureData existingFeature = existing.getFeature();
    final FeatureData incomingFeature = incoming.getFeature();

    final FeatureData.Builder mergedFeatureDataBuilder =
        mergeFeatureData(existingFeature, incomingFeature);

    return existing.toBuilder()
        .mergeFrom(incoming)
        .setType(existing.getType())
        .setFeature(mergedFeatureDataBuilder)
        .build()
        ;
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

  private VersionService.HybridLogicalTimestamp buildNextHybridLogicalTimestamp() {
    return versionService.nextVersion();
  }

  private VersionService.HybridLogicalTimestamp buildNextHybridLogicalTimestamp(
      FeatureVersion version) {
    return versionService.nextVersionUpdate(new VersionService.HybridLogicalTimestamp(
        version.getTimestamp(),
        version.getCounter()));
  }

  private boolean isMatching(NamespaceFeature existing, NamespaceFeature incoming) {
    return existing.getNamespace().equals(incoming.getNamespace());
  }
}
