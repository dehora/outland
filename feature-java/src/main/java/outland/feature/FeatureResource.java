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

  FeatureCollection listFeatures(String appId);

  Feature findByKey(String appId, String featureKey);

  FeatureCollection listFeaturesSince(String appId, long timestamp, TimeUnit timeUnit);
}
