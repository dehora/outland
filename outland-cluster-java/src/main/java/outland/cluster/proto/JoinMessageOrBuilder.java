// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland-cluster.proto

package outland.cluster.proto;

public interface JoinMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:outland.JoinMessage)
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
   * <code>optional .outland.Node sender = 2;</code>
   */
  boolean hasSender();
  /**
   * <code>optional .outland.Node sender = 2;</code>
   */
  outland.cluster.proto.Node getSender();
  /**
   * <code>optional .outland.Node sender = 2;</code>
   */
  outland.cluster.proto.NodeOrBuilder getSenderOrBuilder();
}
