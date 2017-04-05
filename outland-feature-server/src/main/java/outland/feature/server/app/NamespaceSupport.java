package outland.feature.server.app;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import outland.feature.proto.Namespace;

public class NamespaceSupport {

  public static Namespace toApp(String json) {

    try {
      Namespace.Builder builder = Namespace.newBuilder();
      JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
      return builder.build();
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }
}
