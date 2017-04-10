package outland.feature.server.app;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import outland.feature.proto.Group;

public class GroupSupport {

  public static Group toApp(String json) {

    try {
      Group.Builder builder = Group.newBuilder();
      JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
      return builder.build();
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }
}
