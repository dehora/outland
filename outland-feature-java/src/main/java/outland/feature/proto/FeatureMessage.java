// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: outland.proto

package outland.feature.proto;

public final class FeatureMessage {
  private FeatureMessage() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_FeatureOwner_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_FeatureOwner_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_FeatureVersion_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_FeatureVersion_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_Feature_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_Feature_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_Feature_PropertiesEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_Feature_PropertiesEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_FeatureCollection_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_FeatureCollection_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\routland.proto\022\007outland\"+\n\014FeatureOwner" +
      "\022\014\n\004name\030\002 \001(\t\022\r\n\005email\030\003 \001(\t\"/\n\016Feature" +
      "Version\022\014\n\004ulid\030\001 \001(\t\022\017\n\007logical\030\002 \001(\003\"\354" +
      "\002\n\007Feature\022\n\n\002id\030\001 \001(\t\022\013\n\003key\030\002 \001(\t\022\r\n\005a" +
      "ppId\030\003 \001(\t\022%\n\005state\030\004 \001(\0162\026.outland.Feat" +
      "ure.State\022\023\n\013description\030\005 \001(\t\022\017\n\007create" +
      "d\030\006 \001(\t\022\017\n\007updated\030\007 \001(\t\022$\n\005owner\030\010 \001(\0132" +
      "\025.outland.FeatureOwner\0224\n\nproperties\030\t \003" +
      "(\0132 .outland.Feature.PropertiesEntry\022(\n\007" +
      "version\030\014 \001(\0132\027.outland.FeatureVersion\0321",
      "\n\017PropertiesEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005value\030" +
      "\002 \001(\t:\0028\001\"\"\n\005State\022\010\n\004none\020\000\022\007\n\003off\020\001\022\006\n" +
      "\002on\020\002\"Q\n\021FeatureCollection\022\014\n\004type\030\001 \001(\t" +
      "\022\r\n\005appId\030\002 \001(\t\022\037\n\005items\030\003 \003(\0132\020.outland" +
      ".FeatureB+\n\025outland.feature.protoB\016Featu" +
      "reMessageH\001P\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_outland_FeatureOwner_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_outland_FeatureOwner_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureOwner_descriptor,
        new java.lang.String[] { "Name", "Email", });
    internal_static_outland_FeatureVersion_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_outland_FeatureVersion_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureVersion_descriptor,
        new java.lang.String[] { "Ulid", "Logical", });
    internal_static_outland_Feature_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_outland_Feature_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Feature_descriptor,
        new java.lang.String[] { "Id", "Key", "AppId", "State", "Description", "Created", "Updated", "Owner", "Properties", "Version", });
    internal_static_outland_Feature_PropertiesEntry_descriptor =
      internal_static_outland_Feature_descriptor.getNestedTypes().get(0);
    internal_static_outland_Feature_PropertiesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Feature_PropertiesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_outland_FeatureCollection_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_outland_FeatureCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureCollection_descriptor,
        new java.lang.String[] { "Type", "AppId", "Items", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}