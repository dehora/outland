package outland.feature.server.features;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

public interface FeatureService {

  DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  static String asString(OffsetDateTime src) {
    return ISO.format(src);
  }

  static OffsetDateTime asOffsetDateTime(String raw) {
    return ISO.parse(raw, OffsetDateTime::from);
  }

  Optional<Feature> registerFeature(Feature feature);

  Optional<Feature> updateFeature(String appKey, String featureKey, Feature feature);

  Optional<Feature> loadFeatureByKey(String appKey, String featureKey);

  FeatureCollection loadFeatures(String appKey);

  FeatureCollection loadFeaturesChangedSince(String appKey, OffsetDateTime since);
}
