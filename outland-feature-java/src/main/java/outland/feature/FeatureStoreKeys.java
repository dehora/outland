package outland.feature;

class FeatureStoreKeys {

  private static final String DELIM = "@";

  static String storageKey(String appKey, String featureKey) {
    return storageKeyPrefix(appKey) + featureKey;
  }

  static String storageKeyPrefix(String appKey) {
    return appKey + DELIM;
  }

  static String[] storageKeySplit(String storageKey) {
    return storageKey.split(DELIM);
  }
}
