package outland.feature.server.groups;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import outland.feature.proto.Group;
import outland.feature.proto.MemberAccess;
import outland.feature.proto.Owner;
import outland.feature.proto.ServiceAccess;

public interface GroupService {

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

  Optional<Group> register(Group group);

  Group update(Group group);

  Group add(Group group, ServiceAccess service);

  Group add(Group group, MemberAccess member);

  Group add(Group group, Owner owner);

  Group removeServiceAccess(Group group, String serviceKey);

  Group removeMemberAccess(Group group, String memberKey);

  Group removeOwner(Group group, String ownerKey);

  boolean hasOwner(String group, String usernameOrEmail);

  boolean hasServiceAccess(String group, String serviceKey);

  boolean hasMemberAccess(String group, String usernameOrEmail);

  Optional<Group> loadByKey(String group);
}
