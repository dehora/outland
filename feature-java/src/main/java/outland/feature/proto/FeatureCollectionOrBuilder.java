// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

public interface FeatureCollectionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.FeatureCollection)
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
   * <code>optional string appId = 2;</code>
   */
  java.lang.String getAppId();
  /**
   * <code>optional string appId = 2;</code>
   */
  com.google.protobuf.ByteString
      getAppIdBytes();

  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  java.util.List<outland.feature.proto.Feature> 
      getItemsList();
  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  outland.feature.proto.Feature getItems(int index);
  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  int getItemsCount();
  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  java.util.List<? extends outland.feature.proto.FeatureOrBuilder> 
      getItemsOrBuilderList();
  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  outland.feature.proto.FeatureOrBuilder getItemsOrBuilder(
      int index);
}
