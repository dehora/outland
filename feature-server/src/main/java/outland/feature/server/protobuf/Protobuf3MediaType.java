package outland.feature.server.protobuf;

import javax.ws.rs.core.MediaType;

public class Protobuf3MediaType extends MediaType {

  /**
   * "application/protobuf"
   */
  public final static MediaType APPLICATION_PROTOBUF_TYPE = new MediaType(
      "application", "protobuf");

  /**
   * "application/protobuf"
   */
  public final static String APPLICATION_PROTOBUF = "application/protobuf";

  /**
   * "application/protobuf+text"
   */
  public final static MediaType APPLICATION_PROTOBUF_TEXT_TYPE = new MediaType(
      "application", "protobuf+text");

  /**
   * "application/protobuf+text"
   */
  public final static String APPLICATION_PROTOBUF_TEXT = "application/protobuf+text";

  /**
   * "application/protobuf+text"
   */
  public final static MediaType APPLICATION_PROTOBUF_JSON_TYPE = new MediaType(
      "application", "protobuf+json");

  /**
   * "application/protobuf+json"
   */
  public final static String APPLICATION_PROTOBUF_JSON = "application/protobuf+json";
}
