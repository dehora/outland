// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland-cluster.proto

package outland.cluster.proto;

/**
 * Protobuf type {@code outland.DisconnectMessage}
 */
public  final class DisconnectMessage extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:outland.DisconnectMessage)
    DisconnectMessageOrBuilder {
  // Use DisconnectMessage.newBuilder() to construct.
  private DisconnectMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private DisconnectMessage() {
    id_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private DisconnectMessage(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    int mutable_bitField0_ = 0;
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!input.skipField(tag)) {
              done = true;
            }
            break;
          }
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            id_ = s;
            break;
          }
          case 18: {
            outland.cluster.proto.Node.Builder subBuilder = null;
            if (sender_ != null) {
              subBuilder = sender_.toBuilder();
            }
            sender_ = input.readMessage(outland.cluster.proto.Node.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(sender_);
              sender_ = subBuilder.buildPartial();
            }

            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return outland.cluster.proto.ClusterMessage.internal_static_outland_DisconnectMessage_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return outland.cluster.proto.ClusterMessage.internal_static_outland_DisconnectMessage_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            outland.cluster.proto.DisconnectMessage.class, outland.cluster.proto.DisconnectMessage.Builder.class);
  }

  public static final int ID_FIELD_NUMBER = 1;
  private volatile java.lang.Object id_;
  /**
   * <code>optional string id = 1;</code>
   */
  public java.lang.String getId() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      id_ = s;
      return s;
    }
  }
  /**
   * <code>optional string id = 1;</code>
   */
  public com.google.protobuf.ByteString
      getIdBytes() {
    java.lang.Object ref = id_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      id_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int SENDER_FIELD_NUMBER = 2;
  private outland.cluster.proto.Node sender_;
  /**
   * <code>optional .outland.Node sender = 2;</code>
   */
  public boolean hasSender() {
    return sender_ != null;
  }
  /**
   * <code>optional .outland.Node sender = 2;</code>
   */
  public outland.cluster.proto.Node getSender() {
    return sender_ == null ? outland.cluster.proto.Node.getDefaultInstance() : sender_;
  }
  /**
   * <code>optional .outland.Node sender = 2;</code>
   */
  public outland.cluster.proto.NodeOrBuilder getSenderOrBuilder() {
    return getSender();
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, id_);
    }
    if (sender_ != null) {
      output.writeMessage(2, getSender());
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, id_);
    }
    if (sender_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, getSender());
    }
    memoizedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof outland.cluster.proto.DisconnectMessage)) {
      return super.equals(obj);
    }
    outland.cluster.proto.DisconnectMessage other = (outland.cluster.proto.DisconnectMessage) obj;

    boolean result = true;
    result = result && getId()
        .equals(other.getId());
    result = result && (hasSender() == other.hasSender());
    if (hasSender()) {
      result = result && getSender()
          .equals(other.getSender());
    }
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptorForType().hashCode();
    hash = (37 * hash) + ID_FIELD_NUMBER;
    hash = (53 * hash) + getId().hashCode();
    if (hasSender()) {
      hash = (37 * hash) + SENDER_FIELD_NUMBER;
      hash = (53 * hash) + getSender().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static outland.cluster.proto.DisconnectMessage parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static outland.cluster.proto.DisconnectMessage parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static outland.cluster.proto.DisconnectMessage parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static outland.cluster.proto.DisconnectMessage parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static outland.cluster.proto.DisconnectMessage parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static outland.cluster.proto.DisconnectMessage parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static outland.cluster.proto.DisconnectMessage parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static outland.cluster.proto.DisconnectMessage parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static outland.cluster.proto.DisconnectMessage parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static outland.cluster.proto.DisconnectMessage parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(outland.cluster.proto.DisconnectMessage prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code outland.DisconnectMessage}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:outland.DisconnectMessage)
      outland.cluster.proto.DisconnectMessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return outland.cluster.proto.ClusterMessage.internal_static_outland_DisconnectMessage_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return outland.cluster.proto.ClusterMessage.internal_static_outland_DisconnectMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              outland.cluster.proto.DisconnectMessage.class, outland.cluster.proto.DisconnectMessage.Builder.class);
    }

    // Construct using outland.cluster.proto.DisconnectMessage.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      id_ = "";

      if (senderBuilder_ == null) {
        sender_ = null;
      } else {
        sender_ = null;
        senderBuilder_ = null;
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return outland.cluster.proto.ClusterMessage.internal_static_outland_DisconnectMessage_descriptor;
    }

    public outland.cluster.proto.DisconnectMessage getDefaultInstanceForType() {
      return outland.cluster.proto.DisconnectMessage.getDefaultInstance();
    }

    public outland.cluster.proto.DisconnectMessage build() {
      outland.cluster.proto.DisconnectMessage result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public outland.cluster.proto.DisconnectMessage buildPartial() {
      outland.cluster.proto.DisconnectMessage result = new outland.cluster.proto.DisconnectMessage(this);
      result.id_ = id_;
      if (senderBuilder_ == null) {
        result.sender_ = sender_;
      } else {
        result.sender_ = senderBuilder_.build();
      }
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof outland.cluster.proto.DisconnectMessage) {
        return mergeFrom((outland.cluster.proto.DisconnectMessage)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(outland.cluster.proto.DisconnectMessage other) {
      if (other == outland.cluster.proto.DisconnectMessage.getDefaultInstance()) return this;
      if (!other.getId().isEmpty()) {
        id_ = other.id_;
        onChanged();
      }
      if (other.hasSender()) {
        mergeSender(other.getSender());
      }
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      outland.cluster.proto.DisconnectMessage parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (outland.cluster.proto.DisconnectMessage) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object id_ = "";
    /**
     * <code>optional string id = 1;</code>
     */
    public java.lang.String getId() {
      java.lang.Object ref = id_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        id_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>optional string id = 1;</code>
     */
    public com.google.protobuf.ByteString
        getIdBytes() {
      java.lang.Object ref = id_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        id_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string id = 1;</code>
     */
    public Builder setId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      id_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional string id = 1;</code>
     */
    public Builder clearId() {
      
      id_ = getDefaultInstance().getId();
      onChanged();
      return this;
    }
    /**
     * <code>optional string id = 1;</code>
     */
    public Builder setIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      id_ = value;
      onChanged();
      return this;
    }

    private outland.cluster.proto.Node sender_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        outland.cluster.proto.Node, outland.cluster.proto.Node.Builder, outland.cluster.proto.NodeOrBuilder> senderBuilder_;
    /**
     * <code>optional .outland.Node sender = 2;</code>
     */
    public boolean hasSender() {
      return senderBuilder_ != null || sender_ != null;
    }
    /**
     * <code>optional .outland.Node sender = 2;</code>
     */
    public outland.cluster.proto.Node getSender() {
      if (senderBuilder_ == null) {
        return sender_ == null ? outland.cluster.proto.Node.getDefaultInstance() : sender_;
      } else {
        return senderBuilder_.getMessage();
      }
    }
    /**
     * <code>optional .outland.Node sender = 2;</code>
     */
    public Builder setSender(outland.cluster.proto.Node value) {
      if (senderBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        sender_ = value;
        onChanged();
      } else {
        senderBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>optional .outland.Node sender = 2;</code>
     */
    public Builder setSender(
        outland.cluster.proto.Node.Builder builderForValue) {
      if (senderBuilder_ == null) {
        sender_ = builderForValue.build();
        onChanged();
      } else {
        senderBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>optional .outland.Node sender = 2;</code>
     */
    public Builder mergeSender(outland.cluster.proto.Node value) {
      if (senderBuilder_ == null) {
        if (sender_ != null) {
          sender_ =
            outland.cluster.proto.Node.newBuilder(sender_).mergeFrom(value).buildPartial();
        } else {
          sender_ = value;
        }
        onChanged();
      } else {
        senderBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>optional .outland.Node sender = 2;</code>
     */
    public Builder clearSender() {
      if (senderBuilder_ == null) {
        sender_ = null;
        onChanged();
      } else {
        sender_ = null;
        senderBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>optional .outland.Node sender = 2;</code>
     */
    public outland.cluster.proto.Node.Builder getSenderBuilder() {
      
      onChanged();
      return getSenderFieldBuilder().getBuilder();
    }
    /**
     * <code>optional .outland.Node sender = 2;</code>
     */
    public outland.cluster.proto.NodeOrBuilder getSenderOrBuilder() {
      if (senderBuilder_ != null) {
        return senderBuilder_.getMessageOrBuilder();
      } else {
        return sender_ == null ?
            outland.cluster.proto.Node.getDefaultInstance() : sender_;
      }
    }
    /**
     * <code>optional .outland.Node sender = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        outland.cluster.proto.Node, outland.cluster.proto.Node.Builder, outland.cluster.proto.NodeOrBuilder> 
        getSenderFieldBuilder() {
      if (senderBuilder_ == null) {
        senderBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            outland.cluster.proto.Node, outland.cluster.proto.Node.Builder, outland.cluster.proto.NodeOrBuilder>(
                getSender(),
                getParentForChildren(),
                isClean());
        sender_ = null;
      }
      return senderBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:outland.DisconnectMessage)
  }

  // @@protoc_insertion_point(class_scope:outland.DisconnectMessage)
  private static final outland.cluster.proto.DisconnectMessage DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new outland.cluster.proto.DisconnectMessage();
  }

  public static outland.cluster.proto.DisconnectMessage getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DisconnectMessage>
      PARSER = new com.google.protobuf.AbstractParser<DisconnectMessage>() {
    public DisconnectMessage parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new DisconnectMessage(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<DisconnectMessage> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DisconnectMessage> getParserForType() {
    return PARSER;
  }

  public outland.cluster.proto.DisconnectMessage getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

