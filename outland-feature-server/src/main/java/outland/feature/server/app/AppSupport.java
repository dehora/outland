package outland.feature.server.app;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import outland.feature.proto.App;
import outland.feature.proto.Feature;

public class AppSupport {

  public static App toApp(String json) {

    try {
      App.Builder builder = App.newBuilder();
      JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
      return builder.build();
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }
}
