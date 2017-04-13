// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

public interface FeatureOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.Feature)
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
   * <code>optional string id = 2;</code>
   */
  java.lang.String getId();
  /**
   * <code>optional string id = 2;</code>
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <code>optional string created = 3;</code>
   */
  java.lang.String getCreated();
  /**
   * <code>optional string created = 3;</code>
   */
  com.google.protobuf.ByteString
      getCreatedBytes();

  /**
   * <code>optional string updated = 4;</code>
   */
  java.lang.String getUpdated();
  /**
   * <code>optional string updated = 4;</code>
   */
  com.google.protobuf.ByteString
      getUpdatedBytes();

  /**
   * <code>map&lt;string, string&gt; properties = 5;</code>
   */
  int getPropertiesCount();
  /**
   * <code>map&lt;string, string&gt; properties = 5;</code>
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
   * <code>map&lt;string, string&gt; properties = 5;</code>
   */
  java.util.Map<java.lang.String, java.lang.String>
  getPropertiesMap();
  /**
   * <code>map&lt;string, string&gt; properties = 5;</code>
   */

  java.lang.String getPropertiesOrDefault(
      java.lang.String key,
      java.lang.String defaultValue);
  /**
   * <code>map&lt;string, string&gt; properties = 5;</code>
   */

  java.lang.String getPropertiesOrThrow(
      java.lang.String key);

  /**
   * <code>optional string key = 10;</code>
   */
  java.lang.String getKey();
  /**
   * <code>optional string key = 10;</code>
   */
  com.google.protobuf.ByteString
      getKeyBytes();

  /**
   * <code>optional string group = 11;</code>
   */
  java.lang.String getGroup();
  /**
   * <code>optional string group = 11;</code>
   */
  com.google.protobuf.ByteString
      getGroupBytes();

  /**
   * <code>optional .outland.State state = 12;</code>
   */
  int getStateValue();
  /**
   * <code>optional .outland.State state = 12;</code>
   */
  outland.feature.proto.State getState();

  /**
   * <code>optional string description = 13;</code>
   */
  java.lang.String getDescription();
  /**
   * <code>optional string description = 13;</code>
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>optional .outland.Owner owner = 14;</code>
   */
  boolean hasOwner();
  /**
   * <code>optional .outland.Owner owner = 14;</code>
   */
  outland.feature.proto.Owner getOwner();
  /**
   * <code>optional .outland.Owner owner = 14;</code>
   */
  outland.feature.proto.OwnerOrBuilder getOwnerOrBuilder();

  /**
   * <code>optional .outland.OptionCollection options = 15;</code>
   */
  boolean hasOptions();
  /**
   * <code>optional .outland.OptionCollection options = 15;</code>
   */
  outland.feature.proto.OptionCollection getOptions();
  /**
   * <code>optional .outland.OptionCollection options = 15;</code>
   */
  outland.feature.proto.OptionCollectionOrBuilder getOptionsOrBuilder();

  /**
   * <code>optional .outland.FeatureVersion version = 16;</code>
   */
  boolean hasVersion();
  /**
   * <code>optional .outland.FeatureVersion version = 16;</code>
   */
  outland.feature.proto.FeatureVersion getVersion();
  /**
   * <code>optional .outland.FeatureVersion version = 16;</code>
   */
  outland.feature.proto.FeatureVersionOrBuilder getVersionOrBuilder();

  /**
   * <code>optional .outland.NamespaceFeatureCollection namespaces = 17;</code>
   */
  boolean hasNamespaces();
  /**
   * <code>optional .outland.NamespaceFeatureCollection namespaces = 17;</code>
   */
  outland.feature.proto.NamespaceFeatureCollection getNamespaces();
  /**
   * <code>optional .outland.NamespaceFeatureCollection namespaces = 17;</code>
   */
  outland.feature.proto.NamespaceFeatureCollectionOrBuilder getNamespacesOrBuilder();
}
