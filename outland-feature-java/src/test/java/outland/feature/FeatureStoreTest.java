package outland.feature;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

public class FeatureStoreTest implements FeatureStore {

  Map<String, FeatureRecord> db = Maps.newHashMap();

  @Override public Void put(Feature feature) throws FeatureException {
    db.put(FeatureStoreKeys.storageKey(
        feature.getGroup(), feature.getKey()), FeatureRecord.build(feature));
    return null;
  }

  @Override public FeatureRecord find(String group, String key) throws FeatureException {
    return db.get(FeatureStoreKeys.storageKey(group, key));
  }

  @Override public FeatureCollection findAll(String group) throws FeatureException {
    return null;
  }

  @Override public Void remove(String group, String featureKey) throws FeatureException {
    return null;
  }

  @Override public Void removeAll() throws FeatureException {
    return null;
  }

  @Override public void close() throws FeatureException {

  }
}
