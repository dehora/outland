// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

public interface AppOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.App)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional string type = 8;</code>
   */
  java.lang.String getType();
  /**
   * <code>optional string type = 8;</code>
   */
  com.google.protobuf.ByteString
      getTypeBytes();

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
   * <code>optional string name = 3;</code>
   */
  java.lang.String getName();
  /**
   * <code>optional string name = 3;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

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
   * <code>optional .outland.OwnerCollection owners = 5;</code>
   */
  boolean hasOwners();
  /**
   * <code>optional .outland.OwnerCollection owners = 5;</code>
   */
  outland.feature.proto.OwnerCollection getOwners();
  /**
   * <code>optional .outland.OwnerCollection owners = 5;</code>
   */
  outland.feature.proto.OwnerCollectionOrBuilder getOwnersOrBuilder();

  /**
   * <code>optional .outland.GrantCollection granted = 9;</code>
   */
  boolean hasGranted();
  /**
   * <code>optional .outland.GrantCollection granted = 9;</code>
   */
  outland.feature.proto.GrantCollection getGranted();
  /**
   * <code>optional .outland.GrantCollection granted = 9;</code>
   */
  outland.feature.proto.GrantCollectionOrBuilder getGrantedOrBuilder();
}
