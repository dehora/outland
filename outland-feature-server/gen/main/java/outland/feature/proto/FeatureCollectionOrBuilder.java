// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

public interface FeatureCollectionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.FeatureCollection)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string type = 1;</code>
   */
  java.lang.String getType();
  /**
   * <code>string type = 1;</code>
   */
  com.google.protobuf.ByteString
      getTypeBytes();

  /**
   * <code>string group = 10;</code>
   */
  java.lang.String getGroup();
  /**
   * <code>string group = 10;</code>
   */
  com.google.protobuf.ByteString
      getGroupBytes();

  /**
   * <code>repeated .outland.Feature items = 11;</code>
   */
  java.util.List<outland.feature.proto.Feature> 
      getItemsList();
  /**
   * <code>repeated .outland.Feature items = 11;</code>
   */
  outland.feature.proto.Feature getItems(int index);
  /**
   * <code>repeated .outland.Feature items = 11;</code>
   */
  int getItemsCount();
  /**
   * <code>repeated .outland.Feature items = 11;</code>
   */
  java.util.List<? extends outland.feature.proto.FeatureOrBuilder> 
      getItemsOrBuilderList();
  /**
   * <code>repeated .outland.Feature items = 11;</code>
   */
  outland.feature.proto.FeatureOrBuilder getItemsOrBuilder(
      int index);
}
