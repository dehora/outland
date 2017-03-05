package outland.feature;

class FeatureStoreKeys {

  private static final String DELIM = "@";

  static String storageKey(String appId, String featureKey) {
    return storageKeyPrefix(appId) + featureKey;
  }

  static String storageKeyPrefix(String appId) {
    return appId + DELIM;
  }

  static String[] storageKeySplit(String storageKey) {
    return storageKey.split(DELIM);
  }
}
