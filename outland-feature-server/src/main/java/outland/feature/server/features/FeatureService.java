package outland.feature.server.features;

import java.time.OffsetDateTime;
import java.util.Optional;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;
import outland.feature.proto.NamespaceFeature;

public interface FeatureService {

  int DEFAULT_MAXWEIGHT = 10_000;

  Feature registerFeature(Feature feature);

  Optional<Feature> updateFeature(String group, String featureKey, Feature feature);

  Optional<Feature> loadFeatureByKey(String group, String featureKey);

  FeatureCollection loadFeatures(String group);

  FeatureCollection loadChangedSince(String group, OffsetDateTime since);

  Feature add(Feature feature, NamespaceFeature namespaceFeature);

  Feature updateNamespaceFeature(Feature feature, NamespaceFeature namespaceFeature);

  Feature removeNamespaceFeature(String group, String featureKey, String namespace);
}
