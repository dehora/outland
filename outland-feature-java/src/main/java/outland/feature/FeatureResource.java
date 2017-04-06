package outland.feature;

import java.util.concurrent.TimeUnit;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

public interface FeatureResource {

  Feature register(Feature feature);

  Feature update(Feature feature);

  Feature findByKey(String key);

  FeatureCollection listFeatures();

  FeatureCollection listFeaturesSince(long timestamp, TimeUnit timeUnit);

  FeatureCollection listFeatures(String namespace);

  Feature findByKey(String namespace, String featureKey);

  FeatureCollection listFeaturesSince(String namespace, long timestamp, TimeUnit timeUnit);
}
