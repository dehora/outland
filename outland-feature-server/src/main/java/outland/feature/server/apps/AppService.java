package outland.feature.server.apps;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import outland.feature.proto.App;
import outland.feature.proto.MemberAccess;
import outland.feature.proto.Owner;
import outland.feature.proto.ServiceAccess;

public interface AppService {

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

  Optional<App> registerApp(App app);

  App updateApp(App app);

  App addToApp(App app, ServiceAccess service);

  App addToApp(App app, MemberAccess member);

  App addToApp(App app, Owner owner);

  App removeServiceGrant(App app, String serviceKey);

  App removeMemberGrant(App app, String memberKey);

  App removeOwner(App app, String ownerKey);

  boolean appHasOwner(String appKey, String usernameOrEmail);

  boolean appHasServiceGrant(String appKey, String serviceKey);

  boolean appHasMemberGrant(String appKey, String usernameOrEmail);

  Optional<App> loadAppByKey(String appKey);
}
