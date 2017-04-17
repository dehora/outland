package outland.feature;

import com.google.common.base.Charsets;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import outland.feature.proto.Feature;

class TestSupport {

  static String load(String name) {
    try {
      return com.google.common.io.Resources.toString(com.google.common.io.Resources.getResource(name), Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static Feature loadFeature(String name) {
    try {
      return new FeatureSupport().toFeature(load(name));
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }
}
