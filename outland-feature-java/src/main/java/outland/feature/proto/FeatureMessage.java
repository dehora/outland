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
    internal_static_outland_OwnerCollection_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_OwnerCollection_fieldAccessorTable;
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
    internal_static_outland_OptionCollection_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_OptionCollection_fieldAccessorTable;
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
    internal_static_outland_ServiceAccess_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_ServiceAccess_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_MemberAccess_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_MemberAccess_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_AccessCollection_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_AccessCollection_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_Namespace_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_Namespace_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\routland.proto\022\007outland\"r\n\005Owner\022\n\n\002id\030" +
      "\001 \001(\t\022\014\n\004name\030\002 \001(\t\022\020\n\010username\030\003 \001(\t\022\r\n" +
      "\005email\030\004 \001(\t\022\017\n\007created\030\005 \001(\t\022\017\n\007updated" +
      "\030\006 \001(\t\022\014\n\004type\030\007 \001(\t\">\n\017OwnerCollection\022" +
      "\014\n\004type\030\001 \001(\t\022\035\n\005items\030\013 \003(\0132\016.outland.O" +
      "wner\"N\n\016FeatureVersion\022\n\n\002id\030\001 \001(\t\022\021\n\tti" +
      "mestamp\030\002 \001(\003\022\017\n\007counter\030\003 \001(\003\022\014\n\004type\030\004" +
      " \001(\t\"{\n\rFeatureOption\022\n\n\002id\030\001 \001(\t\022#\n\006opt" +
      "ion\030\002 \001(\0162\023.outland.OptionType\022\014\n\004name\030\003" +
      " \001(\t\022\r\n\005value\030\004 \001(\t\022\016\n\006weight\030\005 \001(\005\022\014\n\004t",
      "ype\030\006 \001(\t\"\177\n\020OptionCollection\022\014\n\004type\030\001 " +
      "\001(\t\022#\n\006option\030\002 \001(\0162\023.outland.OptionType" +
      "\022\021\n\tmaxweight\030\003 \001(\005\022%\n\005items\030\004 \003(\0132\026.out" +
      "land.FeatureOption\"\240\003\n\007Feature\022\n\n\002id\030\001 \001" +
      "(\t\022\013\n\003key\030\002 \001(\t\022\016\n\006appkey\030\003 \001(\t\022%\n\005state" +
      "\030\004 \001(\0162\026.outland.Feature.State\022\023\n\013descri" +
      "ption\030\005 \001(\t\022\017\n\007created\030\006 \001(\t\022\017\n\007updated\030" +
      "\007 \001(\t\022\035\n\005owner\030\010 \001(\0132\016.outland.Owner\0224\n\n" +
      "properties\030\t \003(\0132 .outland.Feature.Prope" +
      "rtiesEntry\022*\n\007options\030\n \001(\0132\031.outland.Op",
      "tionCollection\022(\n\007version\030\014 \001(\0132\027.outlan" +
      "d.FeatureVersion\022\014\n\004type\030\r \001(\t\0321\n\017Proper" +
      "tiesEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005value\030\002 \001(\t:\0028" +
      "\001\"\"\n\005State\022\010\n\004none\020\000\022\007\n\003off\020\001\022\006\n\002on\020\002\"R\n" +
      "\021FeatureCollection\022\014\n\004type\030\001 \001(\t\022\016\n\006appk" +
      "ey\030\002 \001(\t\022\037\n\005items\030\003 \003(\0132\020.outland.Featur" +
      "e\"D\n\rServiceAccess\022\014\n\004type\030\001 \001(\t\022\n\n\002id\030\002" +
      " \001(\t\022\014\n\004name\030\n \001(\t\022\013\n\003key\030\013 \001(\t\"W\n\014Membe" +
      "rAccess\022\014\n\004type\030\001 \001(\t\022\n\n\002id\030\002 \001(\t\022\014\n\004nam" +
      "e\030\n \001(\t\022\020\n\010username\030\013 \001(\t\022\r\n\005email\030\014 \001(\t",
      "\"r\n\020AccessCollection\022\014\n\004type\030\001 \001(\t\022(\n\010se" +
      "rvices\030\n \003(\0132\026.outland.ServiceAccess\022&\n\007" +
      "members\030\013 \003(\0132\025.outland.MemberAccess\"\270\001\n" +
      "\tNamespace\022\014\n\004type\030\010 \001(\t\022\n\n\002id\030\001 \001(\t\022\013\n\003" +
      "key\030\002 \001(\t\022\014\n\004name\030\003 \001(\t\022\017\n\007created\030\006 \001(\t" +
      "\022\017\n\007updated\030\007 \001(\t\022(\n\006owners\030\005 \001(\0132\030.outl" +
      "and.OwnerCollection\022*\n\007granted\030\t \001(\0132\031.o" +
      "utland.AccessCollection* \n\nOptionType\022\010\n" +
      "\004flag\020\000\022\010\n\004bool\020\001B+\n\025outland.feature.pro" +
      "toB\016FeatureMessageH\001P\001b\006proto3"
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
        new java.lang.String[] { "Id", "Name", "Username", "Email", "Created", "Updated", "Type", });
    internal_static_outland_OwnerCollection_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_outland_OwnerCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_OwnerCollection_descriptor,
        new java.lang.String[] { "Type", "Items", });
    internal_static_outland_FeatureVersion_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_outland_FeatureVersion_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureVersion_descriptor,
        new java.lang.String[] { "Id", "Timestamp", "Counter", "Type", });
    internal_static_outland_FeatureOption_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_outland_FeatureOption_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureOption_descriptor,
        new java.lang.String[] { "Id", "Option", "Name", "Value", "Weight", "Type", });
    internal_static_outland_OptionCollection_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_outland_OptionCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_OptionCollection_descriptor,
        new java.lang.String[] { "Type", "Option", "Maxweight", "Items", });
    internal_static_outland_Feature_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_outland_Feature_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Feature_descriptor,
        new java.lang.String[] { "Id", "Key", "Appkey", "State", "Description", "Created", "Updated", "Owner", "Properties", "Options", "Version", "Type", });
    internal_static_outland_Feature_PropertiesEntry_descriptor =
      internal_static_outland_Feature_descriptor.getNestedTypes().get(0);
    internal_static_outland_Feature_PropertiesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Feature_PropertiesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_outland_FeatureCollection_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_outland_FeatureCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureCollection_descriptor,
        new java.lang.String[] { "Type", "Appkey", "Items", });
    internal_static_outland_ServiceAccess_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_outland_ServiceAccess_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_ServiceAccess_descriptor,
        new java.lang.String[] { "Type", "Id", "Name", "Key", });
    internal_static_outland_MemberAccess_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_outland_MemberAccess_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_MemberAccess_descriptor,
        new java.lang.String[] { "Type", "Id", "Name", "Username", "Email", });
    internal_static_outland_AccessCollection_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_outland_AccessCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_AccessCollection_descriptor,
        new java.lang.String[] { "Type", "Services", "Members", });
    internal_static_outland_Namespace_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_outland_Namespace_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Namespace_descriptor,
        new java.lang.String[] { "Type", "Id", "Key", "Name", "Created", "Updated", "Owners", "Granted", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
