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

  FeatureCollection listFeatures(String nsKey);

  Feature findByKey(String appKey, String featureKey);

  FeatureCollection listFeaturesSince(String appKey, long timestamp, TimeUnit timeUnit);
}
