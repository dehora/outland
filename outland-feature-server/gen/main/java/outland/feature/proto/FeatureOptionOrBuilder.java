// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

public interface FeatureOptionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.FeatureOption)
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
   * <code>string id = 2;</code>
   */
  java.lang.String getId();
  /**
   * <code>string id = 2;</code>
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>.outland.OptionType option = 10;</code>
   */
  int getOptionValue();
  /**
   * <code>.outland.OptionType option = 10;</code>
   */
  outland.feature.proto.OptionType getOption();

  /**
   * <code>string name = 11;</code>
   */
  java.lang.String getName();
  /**
   * <code>string name = 11;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string value = 12;</code>
   */
  java.lang.String getValue();
  /**
   * <code>string value = 12;</code>
   */
  com.google.protobuf.ByteString
      getValueBytes();

  /**
   * <code>int32 weight = 13;</code>
   */
  int getWeight();
}
