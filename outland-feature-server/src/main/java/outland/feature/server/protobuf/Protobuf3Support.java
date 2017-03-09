package outland.feature.server.protobuf;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

public class Protobuf3Support {

  public static String toJsonString(Message message) {
    try {
      return JsonFormat.printer()
          .includingDefaultValueFields()
          .preservingProtoFieldNames()
          .print(message);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
