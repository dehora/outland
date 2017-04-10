package outland.feature;

class FeatureStoreKeys {

  private static final String DELIM = "@";

  static String storageKey(String group, String featureKey) {
    return storageKeyPrefix(group) + featureKey;
  }

  static String storageKeyPrefix(String group) {
    return group + DELIM;
  }

  static String[] storageKeySplit(String storageKey) {
    return storageKey.split(DELIM);
  }
}
