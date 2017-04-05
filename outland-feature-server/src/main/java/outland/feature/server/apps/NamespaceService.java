package outland.feature.server.apps;

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
  public static final String ACCESS_RELATION = "has_access";
  public static final String OWNER_RELATION = "has_owner";

  DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  static String asString(OffsetDateTime src) {
    return ISO.format(src);
  }

  static OffsetDateTime asOffsetDateTime(String raw) {
    return ISO.parse(raw, OffsetDateTime::from);
  }

  Optional<Namespace> registerNamespace(Namespace Namespace);

  Namespace updateNamespace(Namespace Namespace);

  Namespace addToNamespace(Namespace Namespace, ServiceAccess service);

  Namespace addToNamespace(Namespace Namespace, MemberAccess member);

  Namespace addToNamespace(Namespace Namespace, Owner owner);

  Namespace removeServiceAccess(Namespace Namespace, String serviceKey);

  Namespace removeMemberAccess(Namespace Namespace, String memberKey);

  Namespace removeOwner(Namespace Namespace, String ownerKey);

  boolean hasOwner(String nsKey, String usernameOrEmail);

  boolean hasServiceAccess(String nsKey, String serviceKey);

  boolean hasMemberAccess(String nsKey, String usernameOrEmail);

  Optional<Namespace> loadNamespaceByKey(String nsKey);
}
