// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

public interface ZoneCollectionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.ZoneCollection)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string type = 1;</code>
   */
  java.lang.String getType();
  /**
   * <code>optional string type = 1;</code>
   */
  com.google.protobuf.ByteString
      getTypeBytes();

  /**
   * <code>repeated .outland.Zone items = 2;</code>
   */
  java.util.List<outland.feature.proto.Zone> 
      getItemsList();
  /**
   * <code>repeated .outland.Zone items = 2;</code>
   */
  outland.feature.proto.Zone getItems(int index);
  /**
   * <code>repeated .outland.Zone items = 2;</code>
   */
  int getItemsCount();
  /**
   * <code>repeated .outland.Zone items = 2;</code>
   */
  java.util.List<? extends outland.feature.proto.ZoneOrBuilder> 
      getItemsOrBuilderList();
  /**
   * <code>repeated .outland.Zone items = 2;</code>
   */
  outland.feature.proto.ZoneOrBuilder getItemsOrBuilder(
      int index);
}
