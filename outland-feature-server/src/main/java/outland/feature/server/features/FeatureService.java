package outland.feature.server.features;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;
import outland.feature.proto.NamespaceFeature;

public interface FeatureService {

  DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  static String asString(OffsetDateTime src) {
    return ISO.format(src);
  }

  static OffsetDateTime asOffsetDateTime(String raw) {
    return ISO.parse(raw, OffsetDateTime::from);
  }

  Optional<Feature> registerFeature(Feature feature);

  Optional<Feature> updateFeature(String group, String featureKey, Feature feature);

  Optional<Feature> loadFeatureByKey(String group, String featureKey);

  FeatureCollection loadFeatures(String group);

  FeatureCollection loadFeaturesChangedSince(String group, OffsetDateTime since);

  Feature add(Feature feature, NamespaceFeature namespaceFeature);

  Feature removeNamespaceFeature(String group, String featureKey, String namespace);
}
