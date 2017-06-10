package outland.feature.server.features;

import outland.feature.proto.Feature;
import outland.feature.proto.FeatureVersion;
import outland.feature.server.Names;

class VersionSupport {

  private final VersionService versionService;

  VersionSupport(VersionService versionService) {
    this.versionService = versionService;
  }

  FeatureVersion nextFeatureVersion(Feature feature) {
    if (feature.hasVersion()) {
      return nextFeatureVersion(feature.getVersion());
    } else {
      return nextFeatureVersion();
    }
  }

  FeatureVersion nextFeatureVersion() {
    return nextFeatureVersion(nextTimestamp());
  }

  FeatureVersion nextFeatureVersion(FeatureVersion version) {
    return nextFeatureVersion(nextTimestamp(version));
  }

  private FeatureVersion nextFeatureVersion(VersionService.HybridLogicalTimestamp nextVersion) {
    return buildVersionBuilder(nextVersion).buildPartial();
  }

  private FeatureVersion.Builder buildVersionBuilder(VersionService.HybridLogicalTimestamp next) {
    return FeatureVersion.newBuilder()
        .setCounter(next.counter())
        .setTimestamp(next.logicalTime())
        ;
  }

  private VersionService.HybridLogicalTimestamp nextTimestamp(FeatureVersion version) {
    return versionService.nextVersionUpdate(new VersionService.HybridLogicalTimestamp(
        version.getTimestamp(),
        version.getCounter()));
  }

  private VersionService.HybridLogicalTimestamp nextTimestamp() {
    return versionService.nextVersion();
  }
}
