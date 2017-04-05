package outland.feature.server.namespaces;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import outland.feature.proto.Namespace;
import outland.feature.proto.MemberAccess;
import outland.feature.proto.Owner;
import outland.feature.proto.ServiceAccess;

public interface NamespaceService {

  String OWNER = "owner";
  String SERVICE = "service";
  String MEMBER = "member";
  String ACCESS_RELATION = "has_access";
  String OWNER_RELATION = "has_owner";

  DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  static String asString(OffsetDateTime src) {
    return ISO.format(src);
  }

  static OffsetDateTime asOffsetDateTime(String raw) {
    return ISO.parse(raw, OffsetDateTime::from);
  }

  Optional<Namespace> registerNamespace(Namespace namespace);

  Namespace updateNamespace(Namespace namespace);

  Namespace addToNamespace(Namespace namespace, ServiceAccess service);

  Namespace addToNamespace(Namespace namespace, MemberAccess member);

  Namespace addToNamespace(Namespace namespace, Owner owner);

  Namespace removeServiceAccess(Namespace namespace, String serviceKey);

  Namespace removeMemberAccess(Namespace namespace, String memberKey);

  Namespace removeOwner(Namespace namespace, String ownerKey);

  boolean hasOwner(String namespace, String usernameOrEmail);

  boolean hasServiceAccess(String namespace, String serviceKey);

  boolean hasMemberAccess(String namespace, String usernameOrEmail);

  Optional<Namespace> loadNamespaceByKey(String namespace);
}
