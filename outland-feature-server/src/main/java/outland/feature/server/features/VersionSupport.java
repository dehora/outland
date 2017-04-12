package outland.feature.server.features;

import outland.feature.proto.Feature;
import outland.feature.proto.FeatureVersion;

class VersionSupport {

  private final VersionService versionService;

  VersionSupport(VersionService versionService) {
    this.versionService = versionService;
  }

  FeatureVersion generateVersion(Feature feature) {
    VersionService.HybridLogicalTimestamp next;
    if (feature.hasVersion()) {
      next = versionService.nextVersionUpdate(new VersionService.HybridLogicalTimestamp(
          feature.getVersion().getTimestamp(),
          feature.getVersion().getCounter()));
    } else {
      next = versionService.nextVersion();
    }

    return buildVersion(next).buildPartial();
  }

  private FeatureVersion.Builder buildVersion(VersionService.HybridLogicalTimestamp next) {
    return FeatureVersion.newBuilder()
        .setType("hlcver")
        .setCounter(next.counter())
        .setTimestamp(next.logicalTime())
        .setId(next.id());
  }

}
