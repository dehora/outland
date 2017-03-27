// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

public interface FeatureOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.Feature)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string id = 1;</code>
   */
  java.lang.String getId();
  /**
   * <code>optional string id = 1;</code>
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>optional string key = 2;</code>
   */
  java.lang.String getKey();
  /**
   * <code>optional string key = 2;</code>
   */
  com.google.protobuf.ByteString
      getKeyBytes();

  /**
   * <code>optional string appkey = 3;</code>
   */
  java.lang.String getAppkey();
  /**
   * <code>optional string appkey = 3;</code>
   */
  com.google.protobuf.ByteString
      getAppkeyBytes();

  /**
   * <code>optional .outland.Feature.State state = 4;</code>
   */
  int getStateValue();
  /**
   * <code>optional .outland.Feature.State state = 4;</code>
   */
  outland.feature.proto.Feature.State getState();

  /**
   * <code>optional string description = 5;</code>
   */
  java.lang.String getDescription();
  /**
   * <code>optional string description = 5;</code>
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>optional string created = 6;</code>
   */
  java.lang.String getCreated();
  /**
   * <code>optional string created = 6;</code>
   */
  com.google.protobuf.ByteString
      getCreatedBytes();

  /**
   * <code>optional string updated = 7;</code>
   */
  java.lang.String getUpdated();
  /**
   * <code>optional string updated = 7;</code>
   */
  com.google.protobuf.ByteString
      getUpdatedBytes();

  /**
   * <code>optional .outland.FeatureOwner owner = 8;</code>
   */
  boolean hasOwner();
  /**
   * <code>optional .outland.FeatureOwner owner = 8;</code>
   */
  outland.feature.proto.FeatureOwner getOwner();
  /**
   * <code>optional .outland.FeatureOwner owner = 8;</code>
   */
  outland.feature.proto.FeatureOwnerOrBuilder getOwnerOrBuilder();

  /**
   * <code>map&lt;string, string&gt; properties = 9;</code>
   */
  int getPropertiesCount();
  /**
   * <code>map&lt;string, string&gt; properties = 9;</code>
   */
  boolean containsProperties(
      java.lang.String key);
  /**
   * Use {@link #getPropertiesMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, java.lang.String>
  getProperties();
  /**
   * <code>map&lt;string, string&gt; properties = 9;</code>
   */
  java.util.Map<java.lang.String, java.lang.String>
  getPropertiesMap();
  /**
   * <code>map&lt;string, string&gt; properties = 9;</code>
   */

  java.lang.String getPropertiesOrDefault(
      java.lang.String key,
      java.lang.String defaultValue);
  /**
   * <code>map&lt;string, string&gt; properties = 9;</code>
   */

  java.lang.String getPropertiesOrThrow(
      java.lang.String key);

  /**
   * <code>optional .outland.OptionType optionType = 10;</code>
   */
  int getOptionTypeValue();
  /**
   * <code>optional .outland.OptionType optionType = 10;</code>
   */
  outland.feature.proto.OptionType getOptionType();

  /**
   * <code>repeated .outland.FeatureOption options = 11;</code>
   */
  java.util.List<outland.feature.proto.FeatureOption> 
      getOptionsList();
  /**
   * <code>repeated .outland.FeatureOption options = 11;</code>
   */
  outland.feature.proto.FeatureOption getOptions(int index);
  /**
   * <code>repeated .outland.FeatureOption options = 11;</code>
   */
  int getOptionsCount();
  /**
   * <code>repeated .outland.FeatureOption options = 11;</code>
   */
  java.util.List<? extends outland.feature.proto.FeatureOptionOrBuilder> 
      getOptionsOrBuilderList();
  /**
   * <code>repeated .outland.FeatureOption options = 11;</code>
   */
  outland.feature.proto.FeatureOptionOrBuilder getOptionsOrBuilder(
      int index);

  /**
   * <code>optional .outland.FeatureVersion version = 12;</code>
   */
  boolean hasVersion();
  /**
   * <code>optional .outland.FeatureVersion version = 12;</code>
   */
  outland.feature.proto.FeatureVersion getVersion();
  /**
   * <code>optional .outland.FeatureVersion version = 12;</code>
   */
  outland.feature.proto.FeatureVersionOrBuilder getVersionOrBuilder();
}
