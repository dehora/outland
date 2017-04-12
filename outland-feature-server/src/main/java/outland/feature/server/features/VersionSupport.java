package outland.feature.server.features;

import outland.feature.proto.Feature;
import outland.feature.proto.FeatureVersion;

public class VersionSupport {

  private final VersionService versionService;

  public VersionSupport(VersionService versionService) {
    this.versionService = versionService;
  }

  void applyVersion(Feature registering, Feature.Builder builder) {
    builder.setVersion(generateVersion(registering));
  }

  FeatureVersion generateVersion(Feature registering) {
    VersionService.HybridLogicalTimestamp next;
    if (registering.hasVersion()) {
      next = versionService.nextVersionUpdate(new VersionService.HybridLogicalTimestamp(
          registering.getVersion().getTimestamp(),
          registering.getVersion().getCounter()));
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
