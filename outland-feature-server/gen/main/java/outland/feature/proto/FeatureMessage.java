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
    internal_static_outland_FeatureData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_FeatureData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_NamespaceFeature_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_NamespaceFeature_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_NamespaceFeatureCollection_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_NamespaceFeatureCollection_fieldAccessorTable;
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
    internal_static_outland_Group_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_Group_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_outland_Group_PropertiesEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_outland_Group_PropertiesEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\routland.proto\022\007outland\"P\n\005Owner\022\014\n\004typ" +
      "e\030\001 \001(\t\022\n\n\002id\030\002 \001(\t\022\020\n\010username\030\n \001(\t\022\r\n" +
      "\005email\030\013 \001(\t\022\014\n\004name\030\014 \001(\t\">\n\017OwnerColle" +
      "ction\022\014\n\004type\030\001 \001(\t\022\035\n\005items\030\n \003(\0132\016.out" +
      "land.Owner\"B\n\016FeatureVersion\022\014\n\004type\030\001 \001" +
      "(\t\022\021\n\ttimestamp\030\n \001(\003\022\017\n\007counter\030\013 \001(\003\"z" +
      "\n\rFeatureOption\022\014\n\004type\030\001 \001(\t\022\n\n\002id\030\002 \001(" +
      "\t\022#\n\006option\030\n \001(\0162\023.outland.OptionType\022\013" +
      "\n\003key\030\013 \001(\t\022\r\n\005value\030\014 \001(\t\022\016\n\006weight\030\r \001" +
      "(\005\"\220\001\n\020OptionCollection\022\014\n\004type\030\001 \001(\t\022#\n",
      "\006option\030\n \001(\0162\023.outland.OptionType\022\021\n\tma" +
      "xweight\030\013 \001(\005\022%\n\005items\030\014 \003(\0132\026.outland.F" +
      "eatureOption\022\017\n\007control\030\r \001(\t\"\251\001\n\013Featur" +
      "eData\022\014\n\004type\030\001 \001(\t\022\n\n\002id\030\002 \001(\t\022\013\n\003key\030\n" +
      " \001(\t\022\035\n\005state\030\013 \001(\0162\016.outland.State\022*\n\007o" +
      "ptions\030\014 \001(\0132\031.outland.OptionCollection\022" +
      "(\n\007version\030\r \001(\0132\027.outland.FeatureVersio" +
      "n\"Z\n\020NamespaceFeature\022\014\n\004type\030\001 \001(\t\022\021\n\tn" +
      "amespace\030\n \001(\t\022%\n\007feature\030\013 \001(\0132\024.outlan" +
      "d.FeatureData\"T\n\032NamespaceFeatureCollect",
      "ion\022\014\n\004type\030\001 \001(\t\022(\n\005items\030\n \003(\0132\031.outla" +
      "nd.NamespaceFeature\"\254\003\n\007Feature\022\014\n\004type\030" +
      "\001 \001(\t\022\n\n\002id\030\002 \001(\t\022\017\n\007created\030\003 \001(\t\022\017\n\007up" +
      "dated\030\004 \001(\t\0224\n\nproperties\030\005 \003(\0132 .outlan" +
      "d.Feature.PropertiesEntry\022\013\n\003key\030\n \001(\t\022\r" +
      "\n\005group\030\013 \001(\t\022\035\n\005state\030\014 \001(\0162\016.outland.S" +
      "tate\022\023\n\013description\030\r \001(\t\022\035\n\005owner\030\016 \001(\013" +
      "2\016.outland.Owner\022*\n\007options\030\017 \001(\0132\031.outl" +
      "and.OptionCollection\022(\n\007version\030\020 \001(\0132\027." +
      "outland.FeatureVersion\0227\n\nnamespaces\030\021 \001",
      "(\0132#.outland.NamespaceFeatureCollection\032" +
      "1\n\017PropertiesEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005value" +
      "\030\002 \001(\t:\0028\001\"Q\n\021FeatureCollection\022\014\n\004type\030" +
      "\001 \001(\t\022\r\n\005group\030\n \001(\t\022\037\n\005items\030\013 \003(\0132\020.ou" +
      "tland.Feature\"D\n\rServiceAccess\022\014\n\004type\030\001" +
      " \001(\t\022\n\n\002id\030\002 \001(\t\022\014\n\004name\030\n \001(\t\022\013\n\003key\030\013 " +
      "\001(\t\"W\n\014MemberAccess\022\014\n\004type\030\001 \001(\t\022\n\n\002id\030" +
      "\002 \001(\t\022\014\n\004name\030\n \001(\t\022\020\n\010username\030\013 \001(\t\022\r\n" +
      "\005email\030\014 \001(\t\"r\n\020AccessCollection\022\014\n\004type" +
      "\030\001 \001(\t\022(\n\010services\030\n \003(\0132\026.outland.Servi",
      "ceAccess\022&\n\007members\030\013 \003(\0132\025.outland.Memb" +
      "erAccess\"\233\002\n\005Group\022\014\n\004type\030\001 \001(\t\022\n\n\002id\030\002" +
      " \001(\t\022\017\n\007created\030\003 \001(\t\022\017\n\007updated\030\004 \001(\t\0222" +
      "\n\nproperties\030\005 \003(\0132\036.outland.Group.Prope" +
      "rtiesEntry\022\013\n\003key\030\n \001(\t\022\014\n\004name\030\013 \001(\t\022(\n" +
      "\006owners\030\014 \001(\0132\030.outland.OwnerCollection\022" +
      "*\n\007granted\030\r \001(\0132\031.outland.AccessCollect" +
      "ion\0321\n\017PropertiesEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005v" +
      "alue\030\002 \001(\t:\0028\001*,\n\nOptionType\022\010\n\004flag\020\000\022\010" +
      "\n\004bool\020\001\022\n\n\006string\020\002*\"\n\005State\022\010\n\004none\020\000\022",
      "\007\n\003off\020\001\022\006\n\002on\020\002B+\n\025outland.feature.prot" +
      "oB\016FeatureMessageH\001P\001b\006proto3"
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
        new java.lang.String[] { "Type", "Id", "Username", "Email", "Name", });
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
        new java.lang.String[] { "Type", "Timestamp", "Counter", });
    internal_static_outland_FeatureOption_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_outland_FeatureOption_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureOption_descriptor,
        new java.lang.String[] { "Type", "Id", "Option", "Key", "Value", "Weight", });
    internal_static_outland_OptionCollection_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_outland_OptionCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_OptionCollection_descriptor,
        new java.lang.String[] { "Type", "Option", "Maxweight", "Items", "Control", });
    internal_static_outland_FeatureData_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_outland_FeatureData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureData_descriptor,
        new java.lang.String[] { "Type", "Id", "Key", "State", "Options", "Version", });
    internal_static_outland_NamespaceFeature_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_outland_NamespaceFeature_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_NamespaceFeature_descriptor,
        new java.lang.String[] { "Type", "Namespace", "Feature", });
    internal_static_outland_NamespaceFeatureCollection_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_outland_NamespaceFeatureCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_NamespaceFeatureCollection_descriptor,
        new java.lang.String[] { "Type", "Items", });
    internal_static_outland_Feature_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_outland_Feature_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Feature_descriptor,
        new java.lang.String[] { "Type", "Id", "Created", "Updated", "Properties", "Key", "Group", "State", "Description", "Owner", "Options", "Version", "Namespaces", });
    internal_static_outland_Feature_PropertiesEntry_descriptor =
      internal_static_outland_Feature_descriptor.getNestedTypes().get(0);
    internal_static_outland_Feature_PropertiesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Feature_PropertiesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_outland_FeatureCollection_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_outland_FeatureCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_FeatureCollection_descriptor,
        new java.lang.String[] { "Type", "Group", "Items", });
    internal_static_outland_ServiceAccess_descriptor =
      getDescriptor().getMessageTypes().get(10);
    internal_static_outland_ServiceAccess_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_ServiceAccess_descriptor,
        new java.lang.String[] { "Type", "Id", "Name", "Key", });
    internal_static_outland_MemberAccess_descriptor =
      getDescriptor().getMessageTypes().get(11);
    internal_static_outland_MemberAccess_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_MemberAccess_descriptor,
        new java.lang.String[] { "Type", "Id", "Name", "Username", "Email", });
    internal_static_outland_AccessCollection_descriptor =
      getDescriptor().getMessageTypes().get(12);
    internal_static_outland_AccessCollection_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_AccessCollection_descriptor,
        new java.lang.String[] { "Type", "Services", "Members", });
    internal_static_outland_Group_descriptor =
      getDescriptor().getMessageTypes().get(13);
    internal_static_outland_Group_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Group_descriptor,
        new java.lang.String[] { "Type", "Id", "Created", "Updated", "Properties", "Key", "Name", "Owners", "Granted", });
    internal_static_outland_Group_PropertiesEntry_descriptor =
      internal_static_outland_Group_descriptor.getNestedTypes().get(0);
    internal_static_outland_Group_PropertiesEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_outland_Group_PropertiesEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
