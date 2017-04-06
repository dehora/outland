package outland.feature;

class FeatureStoreKeys {

  private static final String DELIM = "@";

  static String storageKey(String namespace, String featureKey) {
    return storageKeyPrefix(namespace) + featureKey;
  }

  static String storageKeyPrefix(String namespace) {
    return namespace + DELIM;
  }

  static String[] storageKeySplit(String storageKey) {
    return storageKey.split(DELIM);
  }
}
