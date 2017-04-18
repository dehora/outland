package outland.feature;

import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.State;

class FeatureRecordStateChecker {

  boolean enabled(FeatureRecord record, String namespace) {

    final NamespaceFeature target = record.namespace(namespace);

    if (target == null) {
      return record.feature().getState().equals(State.on);
    }

    return target.getFeature().getState().equals(State.on);
  }
}