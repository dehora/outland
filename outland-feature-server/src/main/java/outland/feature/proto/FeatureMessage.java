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
    internal_static_outland_Owner_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_Owner_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_FeatureVersion_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_FeatureVersion_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_FeatureOption_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_FeatureOption_fieldAccessorTable;
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
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_ServiceGrant_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_ServiceGrant_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_MemberGrant_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_MemberGrant_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_GrantCollection_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_GrantCollection_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_App_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_App_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\routland.proto\022\007outland\"P\n\005Owner\022\n\n\002id\030" +
      "\001 \001(\t\022\014\n\004name\030\002 \001(\t\022\020\n\010username\030\003 \001(\t\022\r\n" +
      "\005email\030\004 \001(\t\022\014\n\004type\030\007 \001(\t\"N\n\016FeatureVer" +
      "sion\022\n\n\002id\030\001 \001(\t\022\021\n\ttimestamp\030\002 \001(\003\022\017\n\007c" +
      "ounter\030\003 \001(\003\022\014\n\004type\030\004 \001(\t\"{\n\rFeatureOpt" +
      "ion\022\n\n\002id\030\001 \001(\t\022#\n\006option\030\002 \001(\0162\023.outlan" +
      "d.OptionType\022\014\n\004name\030\003 \001(\t\022\r\n\005value\030\004 \001(" +
      "\t\022\016\n\006weight\030\005 \001(\005\022\014\n\004type\030\006 \001(\t\"\302\003\n\007Feat" +
      "ure\022\n\n\002id\030\001 \001(\t\022\013\n\003key\030\002 \001(\t\022\016\n\006appkey\030\003" +
      " \001(\t\022%\n\005state\030\004 \001(\0162\026.outland.Feature.St",
      "ate\022\023\n\013description\030\005 \001(\t\022\017\n\007created\030\006 \001(" +
      "\t\022\017\n\007updated\030\007 \001(\t\022\035\n\005owner\030\010 \001(\0132\016.outl" +
      "and.Owner\0224\n\nproperties\030\t \003(\0132 .outland." +
      "Feature.PropertiesEntry\022#\n\006option\030\n \001(\0162" +
      "\023.outland.OptionType\022\'\n\007options\030\013 \003(\0132\026." +
      "outland.FeatureOption\022(\n\007version\030\014 \001(\0132\027" +
      ".outland.FeatureVersion\022\014\n\004type\030\r \001(\t\0321\n" +
      "\017PropertiesEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005value\030\002" +
      " \001(\t:\0028\001\"\"\n\005State\022\010\n\004none\020\000\022\007\n\003off\020\001\022\006\n\002" +
      "on\020\002\"R\n\021FeatureCollection\022\014\n\004type\030\001 \001(\t\022",
      "\016\n\006appkey\030\002 \001(\t\022\037\n\005items\030\003 \003(\0132\020.outland" +
      ".Feature\"C\n\014ServiceGrant\022\014\n\004type\030\001 \001(\t\022\n" +
      "\n\002id\030\002 \001(\t\022\014\n\004name\030\n \001(\t\022\013\n\003key\030\013 \001(\t\"V\n" +
      "\013MemberGrant\022\014\n\004type\030\001 \001(\t\022\n\n\002id\030\002 \001(\t\022\014" +
      "\n\004name\030\n \001(\t\022\020\n\010username\030\013 \001(\t\022\r\n\005email\030" +
      "\014 \001(\t\"o\n\017GrantCollection\022\014\n\004type\030\001 \001(\t\022\'" +
      "\n\010services\030\n \003(\0132\025.outland.ServiceGrant\022" +
      "%\n\007members\030\013 \003(\0132\024.outland.MemberGrant\"\247" +
      "\001\n\003App\022\014\n\004type\030\010 \001(\t\022\n\n\002id\030\001 \001(\t\022\013\n\003key\030" +
      "\002 \001(\t\022\014\n\004name\030\003 \001(\t\022\017\n\007created\030\006 \001(\t\022\017\n\007",
      "updated\030\007 \001(\t\022\036\n\006owners\030\005 \003(\0132\016.outland." +
      "Owner\022)\n\007granted\030\t \001(\0132\030.outland.GrantCo" +
      "llection* \n\nOptionType\022\010\n\004flag\020\000\022\010\n\004bool" +
      "\020\001B+\n\025outland.feature.protoB\016FeatureMess" +
      "ageH\001P\001b\006proto3"
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
    internal_static_outland_Owner_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_outland_Owner_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Owner_descriptor,
        new java.lang.String[] { "Id", "Name", "Username", "Email", "Type", });
    internal_static_outland_FeatureVersion_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_outland_FeatureVersion_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureVersion_descriptor,
        new java.lang.String[] { "Id", "Timestamp", "Counter", "Type", });
    internal_static_outland_FeatureOption_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_outland_FeatureOption_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureOption_descriptor,
        new java.lang.String[] { "Id", "Option", "Name", "Value", "Weight", "Type", });
    internal_static_outland_Feature_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_outland_Feature_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Feature_descriptor,
        new java.lang.String[] { "Id", "Key", "Appkey", "State", "Description", "Created", "Updated", "Owner", "Properties", "Option", "Options", "Version", "Type", });
    internal_static_outland_Feature_PropertiesEntry_descriptor =
      internal_static_outland_Feature_descriptor.getNestedTypes().get(0);
    internal_static_outland_Feature_PropertiesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Feature_PropertiesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_outland_FeatureCollection_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_outland_FeatureCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureCollection_descriptor,
        new java.lang.String[] { "Type", "Appkey", "Items", });
    internal_static_outland_ServiceGrant_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_outland_ServiceGrant_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_ServiceGrant_descriptor,
        new java.lang.String[] { "Type", "Id", "Name", "Key", });
    internal_static_outland_MemberGrant_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_outland_MemberGrant_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_MemberGrant_descriptor,
        new java.lang.String[] { "Type", "Id", "Name", "Username", "Email", });
    internal_static_outland_GrantCollection_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_outland_GrantCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_GrantCollection_descriptor,
        new java.lang.String[] { "Type", "Services", "Members", });
    internal_static_outland_App_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_outland_App_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_App_descriptor,
        new java.lang.String[] { "Type", "Id", "Key", "Name", "Created", "Updated", "Owners", "Granted", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
