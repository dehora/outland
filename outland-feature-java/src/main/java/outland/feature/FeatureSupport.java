package outland.feature;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

class FeatureSupport {

  FeatureCollection toFeatureCollection(String json) throws InvalidProtocolBufferException {
    FeatureCollection.Builder builder = FeatureCollection.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
    return builder.build();
  }

  Feature toFeature(String json) throws InvalidProtocolBufferException {
    Feature.Builder builder = Feature.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
    return builder.build();
  }

}
