// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland-cluster.proto

package outland.cluster.proto;

public interface ImokOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.Imok)
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
   * <code>optional string cid = 2;</code>
   */
  java.lang.String getCid();
  /**
   * <code>optional string cid = 2;</code>
   */
  com.google.protobuf.ByteString
      getCidBytes();

  /**
   * <code>optional .outland.Node sender = 3;</code>
   */
  boolean hasSender();
  /**
   * <code>optional .outland.Node sender = 3;</code>
   */
  outland.cluster.proto.Node getSender();
  /**
   * <code>optional .outland.Node sender = 3;</code>
   */
  outland.cluster.proto.NodeOrBuilder getSenderOrBuilder();
}
