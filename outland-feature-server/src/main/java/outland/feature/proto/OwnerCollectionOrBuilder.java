// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

public interface OwnerCollectionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.OwnerCollection)
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
   * <code>repeated .outland.Owner items = 11;</code>
   */
  java.util.List<outland.feature.proto.Owner> 
      getItemsList();
  /**
   * <code>repeated .outland.Owner items = 11;</code>
   */
  outland.feature.proto.Owner getItems(int index);
  /**
   * <code>repeated .outland.Owner items = 11;</code>
   */
  int getItemsCount();
  /**
   * <code>repeated .outland.Owner items = 11;</code>
   */
  java.util.List<? extends outland.feature.proto.OwnerOrBuilder> 
      getItemsOrBuilderList();
  /**
   * <code>repeated .outland.Owner items = 11;</code>
   */
  outland.feature.proto.OwnerOrBuilder getItemsOrBuilder(
      int index);
}
