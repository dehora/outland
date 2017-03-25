// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland-cluster.proto

package outland.cluster.proto;

/**
 * Protobuf type {@code outland.ShuffleReplyMessage}
 */
public  final class ShuffleReplyMessage extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:outland.ShuffleReplyMessage)
    ShuffleReplyMessageOrBuilder {
  // Use ShuffleReplyMessage.newBuilder() to construct.
  private ShuffleReplyMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ShuffleReplyMessage() {
    id_ = "";
    originalMessageId_ = "";
    exchangeList_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private ShuffleReplyMessage(
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
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();

            originalMessageId_ = s;
            break;
          }
          case 34: {
            if (!((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
              exchangeList_ = new java.util.ArrayList<outland.cluster.proto.Node>();
              mutable_bitField0_ |= 0x00000008;
            }
            exchangeList_.add(
                input.readMessage(outland.cluster.proto.Node.parser(), extensionRegistry));
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
      if (((mutable_bitField0_ & 0x00000008) == 0x00000008)) {
        exchangeList_ = java.util.Collections.unmodifiableList(exchangeList_);
      }
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return outland.cluster.proto.ClusterMessage.internal_static_outland_ShuffleReplyMessage_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return outland.cluster.proto.ClusterMessage.internal_static_outland_ShuffleReplyMessage_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            outland.cluster.proto.ShuffleReplyMessage.class, outland.cluster.proto.ShuffleReplyMessage.Builder.class);
  }

  private int bitField0_;
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

  public static final int ORIGINALMESSAGEID_FIELD_NUMBER = 3;
  private volatile java.lang.Object originalMessageId_;
  /**
   * <code>optional string originalMessageId = 3;</code>
   */
  public java.lang.String getOriginalMessageId() {
    java.lang.Object ref = originalMessageId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      originalMessageId_ = s;
      return s;
    }
  }
  /**
   * <code>optional string originalMessageId = 3;</code>
   */
  public com.google.protobuf.ByteString
      getOriginalMessageIdBytes() {
    java.lang.Object ref = originalMessageId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      originalMessageId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int EXCHANGELIST_FIELD_NUMBER = 4;
  private java.util.List<outland.cluster.proto.Node> exchangeList_;
  /**
   * <code>repeated .outland.Node exchangeList = 4;</code>
   */
  public java.util.List<outland.cluster.proto.Node> getExchangeListList() {
    return exchangeList_;
  }
  /**
   * <code>repeated .outland.Node exchangeList = 4;</code>
   */
  public java.util.List<? extends outland.cluster.proto.NodeOrBuilder> 
      getExchangeListOrBuilderList() {
    return exchangeList_;
  }
  /**
   * <code>repeated .outland.Node exchangeList = 4;</code>
   */
  public int getExchangeListCount() {
    return exchangeList_.size();
  }
  /**
   * <code>repeated .outland.Node exchangeList = 4;</code>
   */
  public outland.cluster.proto.Node getExchangeList(int index) {
    return exchangeList_.get(index);
  }
  /**
   * <code>repeated .outland.Node exchangeList = 4;</code>
   */
  public outland.cluster.proto.NodeOrBuilder getExchangeListOrBuilder(
      int index) {
    return exchangeList_.get(index);
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
    if (!getOriginalMessageIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, originalMessageId_);
    }
    for (int i = 0; i < exchangeList_.size(); i++) {
      output.writeMessage(4, exchangeList_.get(i));
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
    if (!getOriginalMessageIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, originalMessageId_);
    }
    for (int i = 0; i < exchangeList_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, exchangeList_.get(i));
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
    if (!(obj instanceof outland.cluster.proto.ShuffleReplyMessage)) {
      return super.equals(obj);
    }
    outland.cluster.proto.ShuffleReplyMessage other = (outland.cluster.proto.ShuffleReplyMessage) obj;

    boolean result = true;
    result = result && getId()
        .equals(other.getId());
    result = result && (hasSender() == other.hasSender());
    if (hasSender()) {
      result = result && getSender()
          .equals(other.getSender());
    }
    result = result && getOriginalMessageId()
        .equals(other.getOriginalMessageId());
    result = result && getExchangeListList()
        .equals(other.getExchangeListList());
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
    hash = (37 * hash) + ORIGINALMESSAGEID_FIELD_NUMBER;
    hash = (53 * hash) + getOriginalMessageId().hashCode();
    if (getExchangeListCount() > 0) {
      hash = (37 * hash) + EXCHANGELIST_FIELD_NUMBER;
      hash = (53 * hash) + getExchangeListList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static outland.cluster.proto.ShuffleReplyMessage parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static outland.cluster.proto.ShuffleReplyMessage parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static outland.cluster.proto.ShuffleReplyMessage parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static outland.cluster.proto.ShuffleReplyMessage parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static outland.cluster.proto.ShuffleReplyMessage parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static outland.cluster.proto.ShuffleReplyMessage parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static outland.cluster.proto.ShuffleReplyMessage parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static outland.cluster.proto.ShuffleReplyMessage parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static outland.cluster.proto.ShuffleReplyMessage parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static outland.cluster.proto.ShuffleReplyMessage parseFrom(
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
  public static Builder newBuilder(outland.cluster.proto.ShuffleReplyMessage prototype) {
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
   * Protobuf type {@code outland.ShuffleReplyMessage}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:outland.ShuffleReplyMessage)
      outland.cluster.proto.ShuffleReplyMessageOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return outland.cluster.proto.ClusterMessage.internal_static_outland_ShuffleReplyMessage_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return outland.cluster.proto.ClusterMessage.internal_static_outland_ShuffleReplyMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              outland.cluster.proto.ShuffleReplyMessage.class, outland.cluster.proto.ShuffleReplyMessage.Builder.class);
    }

    // Construct using outland.cluster.proto.ShuffleReplyMessage.newBuilder()
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
        getExchangeListFieldBuilder();
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
      originalMessageId_ = "";

      if (exchangeListBuilder_ == null) {
        exchangeList_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000008);
      } else {
        exchangeListBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return outland.cluster.proto.ClusterMessage.internal_static_outland_ShuffleReplyMessage_descriptor;
    }

    public outland.cluster.proto.ShuffleReplyMessage getDefaultInstanceForType() {
      return outland.cluster.proto.ShuffleReplyMessage.getDefaultInstance();
    }

    public outland.cluster.proto.ShuffleReplyMessage build() {
      outland.cluster.proto.ShuffleReplyMessage result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public outland.cluster.proto.ShuffleReplyMessage buildPartial() {
      outland.cluster.proto.ShuffleReplyMessage result = new outland.cluster.proto.ShuffleReplyMessage(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.id_ = id_;
      if (senderBuilder_ == null) {
        result.sender_ = sender_;
      } else {
        result.sender_ = senderBuilder_.build();
      }
      result.originalMessageId_ = originalMessageId_;
      if (exchangeListBuilder_ == null) {
        if (((bitField0_ & 0x00000008) == 0x00000008)) {
          exchangeList_ = java.util.Collections.unmodifiableList(exchangeList_);
          bitField0_ = (bitField0_ & ~0x00000008);
        }
        result.exchangeList_ = exchangeList_;
      } else {
        result.exchangeList_ = exchangeListBuilder_.build();
      }
      result.bitField0_ = to_bitField0_;
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
      if (other instanceof outland.cluster.proto.ShuffleReplyMessage) {
        return mergeFrom((outland.cluster.proto.ShuffleReplyMessage)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(outland.cluster.proto.ShuffleReplyMessage other) {
      if (other == outland.cluster.proto.ShuffleReplyMessage.getDefaultInstance()) return this;
      if (!other.getId().isEmpty()) {
        id_ = other.id_;
        onChanged();
      }
      if (other.hasSender()) {
        mergeSender(other.getSender());
      }
      if (!other.getOriginalMessageId().isEmpty()) {
        originalMessageId_ = other.originalMessageId_;
        onChanged();
      }
      if (exchangeListBuilder_ == null) {
        if (!other.exchangeList_.isEmpty()) {
          if (exchangeList_.isEmpty()) {
            exchangeList_ = other.exchangeList_;
            bitField0_ = (bitField0_ & ~0x00000008);
          } else {
            ensureExchangeListIsMutable();
            exchangeList_.addAll(other.exchangeList_);
          }
          onChanged();
        }
      } else {
        if (!other.exchangeList_.isEmpty()) {
          if (exchangeListBuilder_.isEmpty()) {
            exchangeListBuilder_.dispose();
            exchangeListBuilder_ = null;
            exchangeList_ = other.exchangeList_;
            bitField0_ = (bitField0_ & ~0x00000008);
            exchangeListBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getExchangeListFieldBuilder() : null;
          } else {
            exchangeListBuilder_.addAllMessages(other.exchangeList_);
          }
        }
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
      outland.cluster.proto.ShuffleReplyMessage parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (outland.cluster.proto.ShuffleReplyMessage) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

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

    private java.lang.Object originalMessageId_ = "";
    /**
     * <code>optional string originalMessageId = 3;</code>
     */
    public java.lang.String getOriginalMessageId() {
      java.lang.Object ref = originalMessageId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        originalMessageId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>optional string originalMessageId = 3;</code>
     */
    public com.google.protobuf.ByteString
        getOriginalMessageIdBytes() {
      java.lang.Object ref = originalMessageId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        originalMessageId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string originalMessageId = 3;</code>
     */
    public Builder setOriginalMessageId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      originalMessageId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional string originalMessageId = 3;</code>
     */
    public Builder clearOriginalMessageId() {
      
      originalMessageId_ = getDefaultInstance().getOriginalMessageId();
      onChanged();
      return this;
    }
    /**
     * <code>optional string originalMessageId = 3;</code>
     */
    public Builder setOriginalMessageIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      originalMessageId_ = value;
      onChanged();
      return this;
    }

    private java.util.List<outland.cluster.proto.Node> exchangeList_ =
      java.util.Collections.emptyList();
    private void ensureExchangeListIsMutable() {
      if (!((bitField0_ & 0x00000008) == 0x00000008)) {
        exchangeList_ = new java.util.ArrayList<outland.cluster.proto.Node>(exchangeList_);
        bitField0_ |= 0x00000008;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        outland.cluster.proto.Node, outland.cluster.proto.Node.Builder, outland.cluster.proto.NodeOrBuilder> exchangeListBuilder_;

    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public java.util.List<outland.cluster.proto.Node> getExchangeListList() {
      if (exchangeListBuilder_ == null) {
        return java.util.Collections.unmodifiableList(exchangeList_);
      } else {
        return exchangeListBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public int getExchangeListCount() {
      if (exchangeListBuilder_ == null) {
        return exchangeList_.size();
      } else {
        return exchangeListBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public outland.cluster.proto.Node getExchangeList(int index) {
      if (exchangeListBuilder_ == null) {
        return exchangeList_.get(index);
      } else {
        return exchangeListBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public Builder setExchangeList(
        int index, outland.cluster.proto.Node value) {
      if (exchangeListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureExchangeListIsMutable();
        exchangeList_.set(index, value);
        onChanged();
      } else {
        exchangeListBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public Builder setExchangeList(
        int index, outland.cluster.proto.Node.Builder builderForValue) {
      if (exchangeListBuilder_ == null) {
        ensureExchangeListIsMutable();
        exchangeList_.set(index, builderForValue.build());
        onChanged();
      } else {
        exchangeListBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public Builder addExchangeList(outland.cluster.proto.Node value) {
      if (exchangeListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureExchangeListIsMutable();
        exchangeList_.add(value);
        onChanged();
      } else {
        exchangeListBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public Builder addExchangeList(
        int index, outland.cluster.proto.Node value) {
      if (exchangeListBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureExchangeListIsMutable();
        exchangeList_.add(index, value);
        onChanged();
      } else {
        exchangeListBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public Builder addExchangeList(
        outland.cluster.proto.Node.Builder builderForValue) {
      if (exchangeListBuilder_ == null) {
        ensureExchangeListIsMutable();
        exchangeList_.add(builderForValue.build());
        onChanged();
      } else {
        exchangeListBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public Builder addExchangeList(
        int index, outland.cluster.proto.Node.Builder builderForValue) {
      if (exchangeListBuilder_ == null) {
        ensureExchangeListIsMutable();
        exchangeList_.add(index, builderForValue.build());
        onChanged();
      } else {
        exchangeListBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public Builder addAllExchangeList(
        java.lang.Iterable<? extends outland.cluster.proto.Node> values) {
      if (exchangeListBuilder_ == null) {
        ensureExchangeListIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, exchangeList_);
        onChanged();
      } else {
        exchangeListBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public Builder clearExchangeList() {
      if (exchangeListBuilder_ == null) {
        exchangeList_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000008);
        onChanged();
      } else {
        exchangeListBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public Builder removeExchangeList(int index) {
      if (exchangeListBuilder_ == null) {
        ensureExchangeListIsMutable();
        exchangeList_.remove(index);
        onChanged();
      } else {
        exchangeListBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public outland.cluster.proto.Node.Builder getExchangeListBuilder(
        int index) {
      return getExchangeListFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public outland.cluster.proto.NodeOrBuilder getExchangeListOrBuilder(
        int index) {
      if (exchangeListBuilder_ == null) {
        return exchangeList_.get(index);  } else {
        return exchangeListBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public java.util.List<? extends outland.cluster.proto.NodeOrBuilder> 
         getExchangeListOrBuilderList() {
      if (exchangeListBuilder_ != null) {
        return exchangeListBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(exchangeList_);
      }
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public outland.cluster.proto.Node.Builder addExchangeListBuilder() {
      return getExchangeListFieldBuilder().addBuilder(
          outland.cluster.proto.Node.getDefaultInstance());
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public outland.cluster.proto.Node.Builder addExchangeListBuilder(
        int index) {
      return getExchangeListFieldBuilder().addBuilder(
          index, outland.cluster.proto.Node.getDefaultInstance());
    }
    /**
     * <code>repeated .outland.Node exchangeList = 4;</code>
     */
    public java.util.List<outland.cluster.proto.Node.Builder> 
         getExchangeListBuilderList() {
      return getExchangeListFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        outland.cluster.proto.Node, outland.cluster.proto.Node.Builder, outland.cluster.proto.NodeOrBuilder> 
        getExchangeListFieldBuilder() {
      if (exchangeListBuilder_ == null) {
        exchangeListBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            outland.cluster.proto.Node, outland.cluster.proto.Node.Builder, outland.cluster.proto.NodeOrBuilder>(
                exchangeList_,
                ((bitField0_ & 0x00000008) == 0x00000008),
                getParentForChildren(),
                isClean());
        exchangeList_ = null;
      }
      return exchangeListBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:outland.ShuffleReplyMessage)
  }

  // @@protoc_insertion_point(class_scope:outland.ShuffleReplyMessage)
  private static final outland.cluster.proto.ShuffleReplyMessage DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new outland.cluster.proto.ShuffleReplyMessage();
  }

  public static outland.cluster.proto.ShuffleReplyMessage getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ShuffleReplyMessage>
      PARSER = new com.google.protobuf.AbstractParser<ShuffleReplyMessage>() {
    public ShuffleReplyMessage parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new ShuffleReplyMessage(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<ShuffleReplyMessage> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ShuffleReplyMessage> getParserForType() {
    return PARSER;
  }

  public outland.cluster.proto.ShuffleReplyMessage getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
