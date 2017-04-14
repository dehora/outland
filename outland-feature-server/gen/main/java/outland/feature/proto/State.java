// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

/**
 * Protobuf enum {@code outland.State}
 */
public enum State
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>none = 0;</code>
   */
  none(0),
  /**
   * <code>off = 1;</code>
   */
  off(1),
  /**
   * <code>on = 2;</code>
   */
  on(2),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>none = 0;</code>
   */
  public static final int none_VALUE = 0;
  /**
   * <code>off = 1;</code>
   */
  public static final int off_VALUE = 1;
  /**
   * <code>on = 2;</code>
   */
  public static final int on_VALUE = 2;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static State valueOf(int value) {
    return forNumber(value);
  }

  public static State forNumber(int value) {
    switch (value) {
      case 0: return none;
      case 1: return off;
      case 2: return on;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<State>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      State> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<State>() {
          public State findValueByNumber(int number) {
            return State.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return outland.feature.proto.FeatureMessage.getDescriptor().getEnumTypes().get(1);
  }

  private static final State[] VALUES = values();

  public static State valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private State(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:outland.State)
}

