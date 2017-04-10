// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

/**
 * Protobuf type {@code outland.FeatureCollection}
 */
public  final class FeatureCollection extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:outland.FeatureCollection)
    FeatureCollectionOrBuilder {
  // Use FeatureCollection.newBuilder() to construct.
  private FeatureCollection(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private FeatureCollection() {
    type_ = "";
    group_ = "";
    items_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private FeatureCollection(
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

            type_ = s;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            group_ = s;
            break;
          }
          case 26: {
            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
              items_ = new java.util.ArrayList<outland.feature.proto.Feature>();
              mutable_bitField0_ |= 0x00000004;
            }
            items_.add(
                input.readMessage(outland.feature.proto.Feature.parser(), extensionRegistry));
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
      if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
        items_ = java.util.Collections.unmodifiableList(items_);
      }
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return outland.feature.proto.FeatureMessage.internal_static_outland_FeatureCollection_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return outland.feature.proto.FeatureMessage.internal_static_outland_FeatureCollection_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            outland.feature.proto.FeatureCollection.class, outland.feature.proto.FeatureCollection.Builder.class);
  }

  private int bitField0_;
  public static final int TYPE_FIELD_NUMBER = 1;
  private volatile java.lang.Object type_;
  /**
   * <code>optional string type = 1;</code>
   */
  public java.lang.String getType() {
    java.lang.Object ref = type_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      type_ = s;
      return s;
    }
  }
  /**
   * <code>optional string type = 1;</code>
   */
  public com.google.protobuf.ByteString
      getTypeBytes() {
    java.lang.Object ref = type_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      type_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int GROUP_FIELD_NUMBER = 2;
  private volatile java.lang.Object group_;
  /**
   * <code>optional string group = 2;</code>
   */
  public java.lang.String getGroup() {
    java.lang.Object ref = group_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      group_ = s;
      return s;
    }
  }
  /**
   * <code>optional string group = 2;</code>
   */
  public com.google.protobuf.ByteString
      getGroupBytes() {
    java.lang.Object ref = group_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      group_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ITEMS_FIELD_NUMBER = 3;
  private java.util.List<outland.feature.proto.Feature> items_;
  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  public java.util.List<outland.feature.proto.Feature> getItemsList() {
    return items_;
  }
  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  public java.util.List<? extends outland.feature.proto.FeatureOrBuilder> 
      getItemsOrBuilderList() {
    return items_;
  }
  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  public int getItemsCount() {
    return items_.size();
  }
  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  public outland.feature.proto.Feature getItems(int index) {
    return items_.get(index);
  }
  /**
   * <code>repeated .outland.Feature items = 3;</code>
   */
  public outland.feature.proto.FeatureOrBuilder getItemsOrBuilder(
      int index) {
    return items_.get(index);
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
    if (!getTypeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, type_);
    }
    if (!getGroupBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, group_);
    }
    for (int i = 0; i < items_.size(); i++) {
      output.writeMessage(3, items_.get(i));
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getTypeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, type_);
    }
    if (!getGroupBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, group_);
    }
    for (int i = 0; i < items_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, items_.get(i));
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
    if (!(obj instanceof outland.feature.proto.FeatureCollection)) {
      return super.equals(obj);
    }
    outland.feature.proto.FeatureCollection other = (outland.feature.proto.FeatureCollection) obj;

    boolean result = true;
    result = result && getType()
        .equals(other.getType());
    result = result && getGroup()
        .equals(other.getGroup());
    result = result && getItemsList()
        .equals(other.getItemsList());
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptorForType().hashCode();
    hash = (37 * hash) + TYPE_FIELD_NUMBER;
    hash = (53 * hash) + getType().hashCode();
    hash = (37 * hash) + GROUP_FIELD_NUMBER;
    hash = (53 * hash) + getGroup().hashCode();
    if (getItemsCount() > 0) {
      hash = (37 * hash) + ITEMS_FIELD_NUMBER;
      hash = (53 * hash) + getItemsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static outland.feature.proto.FeatureCollection parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static outland.feature.proto.FeatureCollection parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static outland.feature.proto.FeatureCollection parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static outland.feature.proto.FeatureCollection parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static outland.feature.proto.FeatureCollection parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static outland.feature.proto.FeatureCollection parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static outland.feature.proto.FeatureCollection parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static outland.feature.proto.FeatureCollection parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static outland.feature.proto.FeatureCollection parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static outland.feature.proto.FeatureCollection parseFrom(
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
  public static Builder newBuilder(outland.feature.proto.FeatureCollection prototype) {
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
   * Protobuf type {@code outland.FeatureCollection}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:outland.FeatureCollection)
      outland.feature.proto.FeatureCollectionOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return outland.feature.proto.FeatureMessage.internal_static_outland_FeatureCollection_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return outland.feature.proto.FeatureMessage.internal_static_outland_FeatureCollection_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              outland.feature.proto.FeatureCollection.class, outland.feature.proto.FeatureCollection.Builder.class);
    }

    // Construct using outland.feature.proto.FeatureCollection.newBuilder()
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
        getItemsFieldBuilder();
      }
    }
    public Builder clear() {
      super.clear();
      type_ = "";

      group_ = "";

      if (itemsBuilder_ == null) {
        items_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
      } else {
        itemsBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return outland.feature.proto.FeatureMessage.internal_static_outland_FeatureCollection_descriptor;
    }

    public outland.feature.proto.FeatureCollection getDefaultInstanceForType() {
      return outland.feature.proto.FeatureCollection.getDefaultInstance();
    }

    public outland.feature.proto.FeatureCollection build() {
      outland.feature.proto.FeatureCollection result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public outland.feature.proto.FeatureCollection buildPartial() {
      outland.feature.proto.FeatureCollection result = new outland.feature.proto.FeatureCollection(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.type_ = type_;
      result.group_ = group_;
      if (itemsBuilder_ == null) {
        if (((bitField0_ & 0x00000004) == 0x00000004)) {
          items_ = java.util.Collections.unmodifiableList(items_);
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.items_ = items_;
      } else {
        result.items_ = itemsBuilder_.build();
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
      if (other instanceof outland.feature.proto.FeatureCollection) {
        return mergeFrom((outland.feature.proto.FeatureCollection)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(outland.feature.proto.FeatureCollection other) {
      if (other == outland.feature.proto.FeatureCollection.getDefaultInstance()) return this;
      if (!other.getType().isEmpty()) {
        type_ = other.type_;
        onChanged();
      }
      if (!other.getGroup().isEmpty()) {
        group_ = other.group_;
        onChanged();
      }
      if (itemsBuilder_ == null) {
        if (!other.items_.isEmpty()) {
          if (items_.isEmpty()) {
            items_ = other.items_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensureItemsIsMutable();
            items_.addAll(other.items_);
          }
          onChanged();
        }
      } else {
        if (!other.items_.isEmpty()) {
          if (itemsBuilder_.isEmpty()) {
            itemsBuilder_.dispose();
            itemsBuilder_ = null;
            items_ = other.items_;
            bitField0_ = (bitField0_ & ~0x00000004);
            itemsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getItemsFieldBuilder() : null;
          } else {
            itemsBuilder_.addAllMessages(other.items_);
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
      outland.feature.proto.FeatureCollection parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (outland.feature.proto.FeatureCollection) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.lang.Object type_ = "";
    /**
     * <code>optional string type = 1;</code>
     */
    public java.lang.String getType() {
      java.lang.Object ref = type_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        type_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>optional string type = 1;</code>
     */
    public com.google.protobuf.ByteString
        getTypeBytes() {
      java.lang.Object ref = type_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        type_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string type = 1;</code>
     */
    public Builder setType(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      type_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional string type = 1;</code>
     */
    public Builder clearType() {
      
      type_ = getDefaultInstance().getType();
      onChanged();
      return this;
    }
    /**
     * <code>optional string type = 1;</code>
     */
    public Builder setTypeBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      type_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object group_ = "";
    /**
     * <code>optional string group = 2;</code>
     */
    public java.lang.String getGroup() {
      java.lang.Object ref = group_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        group_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>optional string group = 2;</code>
     */
    public com.google.protobuf.ByteString
        getGroupBytes() {
      java.lang.Object ref = group_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        group_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string group = 2;</code>
     */
    public Builder setGroup(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      group_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional string group = 2;</code>
     */
    public Builder clearGroup() {
      
      group_ = getDefaultInstance().getGroup();
      onChanged();
      return this;
    }
    /**
     * <code>optional string group = 2;</code>
     */
    public Builder setGroupBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      group_ = value;
      onChanged();
      return this;
    }

    private java.util.List<outland.feature.proto.Feature> items_ =
      java.util.Collections.emptyList();
    private void ensureItemsIsMutable() {
      if (!((bitField0_ & 0x00000004) == 0x00000004)) {
        items_ = new java.util.ArrayList<outland.feature.proto.Feature>(items_);
        bitField0_ |= 0x00000004;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        outland.feature.proto.Feature, outland.feature.proto.Feature.Builder, outland.feature.proto.FeatureOrBuilder> itemsBuilder_;

    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public java.util.List<outland.feature.proto.Feature> getItemsList() {
      if (itemsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(items_);
      } else {
        return itemsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public int getItemsCount() {
      if (itemsBuilder_ == null) {
        return items_.size();
      } else {
        return itemsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public outland.feature.proto.Feature getItems(int index) {
      if (itemsBuilder_ == null) {
        return items_.get(index);
      } else {
        return itemsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public Builder setItems(
        int index, outland.feature.proto.Feature value) {
      if (itemsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureItemsIsMutable();
        items_.set(index, value);
        onChanged();
      } else {
        itemsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public Builder setItems(
        int index, outland.feature.proto.Feature.Builder builderForValue) {
      if (itemsBuilder_ == null) {
        ensureItemsIsMutable();
        items_.set(index, builderForValue.build());
        onChanged();
      } else {
        itemsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public Builder addItems(outland.feature.proto.Feature value) {
      if (itemsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureItemsIsMutable();
        items_.add(value);
        onChanged();
      } else {
        itemsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public Builder addItems(
        int index, outland.feature.proto.Feature value) {
      if (itemsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureItemsIsMutable();
        items_.add(index, value);
        onChanged();
      } else {
        itemsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public Builder addItems(
        outland.feature.proto.Feature.Builder builderForValue) {
      if (itemsBuilder_ == null) {
        ensureItemsIsMutable();
        items_.add(builderForValue.build());
        onChanged();
      } else {
        itemsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public Builder addItems(
        int index, outland.feature.proto.Feature.Builder builderForValue) {
      if (itemsBuilder_ == null) {
        ensureItemsIsMutable();
        items_.add(index, builderForValue.build());
        onChanged();
      } else {
        itemsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public Builder addAllItems(
        java.lang.Iterable<? extends outland.feature.proto.Feature> values) {
      if (itemsBuilder_ == null) {
        ensureItemsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, items_);
        onChanged();
      } else {
        itemsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public Builder clearItems() {
      if (itemsBuilder_ == null) {
        items_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
      } else {
        itemsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public Builder removeItems(int index) {
      if (itemsBuilder_ == null) {
        ensureItemsIsMutable();
        items_.remove(index);
        onChanged();
      } else {
        itemsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public outland.feature.proto.Feature.Builder getItemsBuilder(
        int index) {
      return getItemsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public outland.feature.proto.FeatureOrBuilder getItemsOrBuilder(
        int index) {
      if (itemsBuilder_ == null) {
        return items_.get(index);  } else {
        return itemsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public java.util.List<? extends outland.feature.proto.FeatureOrBuilder> 
         getItemsOrBuilderList() {
      if (itemsBuilder_ != null) {
        return itemsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(items_);
      }
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public outland.feature.proto.Feature.Builder addItemsBuilder() {
      return getItemsFieldBuilder().addBuilder(
          outland.feature.proto.Feature.getDefaultInstance());
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public outland.feature.proto.Feature.Builder addItemsBuilder(
        int index) {
      return getItemsFieldBuilder().addBuilder(
          index, outland.feature.proto.Feature.getDefaultInstance());
    }
    /**
     * <code>repeated .outland.Feature items = 3;</code>
     */
    public java.util.List<outland.feature.proto.Feature.Builder> 
         getItemsBuilderList() {
      return getItemsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        outland.feature.proto.Feature, outland.feature.proto.Feature.Builder, outland.feature.proto.FeatureOrBuilder> 
        getItemsFieldBuilder() {
      if (itemsBuilder_ == null) {
        itemsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            outland.feature.proto.Feature, outland.feature.proto.Feature.Builder, outland.feature.proto.FeatureOrBuilder>(
                items_,
                ((bitField0_ & 0x00000004) == 0x00000004),
                getParentForChildren(),
                isClean());
        items_ = null;
      }
      return itemsBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:outland.FeatureCollection)
  }

  // @@protoc_insertion_point(class_scope:outland.FeatureCollection)
  private static final outland.feature.proto.FeatureCollection DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new outland.feature.proto.FeatureCollection();
  }

  public static outland.feature.proto.FeatureCollection getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<FeatureCollection>
      PARSER = new com.google.protobuf.AbstractParser<FeatureCollection>() {
    public FeatureCollection parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new FeatureCollection(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<FeatureCollection> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<FeatureCollection> getParserForType() {
    return PARSER;
  }

  public outland.feature.proto.FeatureCollection getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

