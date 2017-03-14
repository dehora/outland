package outland.feature.server.apps;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import outland.feature.proto.App;
import outland.feature.proto.Owner;
import outland.feature.proto.Service;

public interface AppService {

  String OWNER = "owner";
  String SERVICE = "service";

  DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  static String asString(OffsetDateTime src) {
    return ISO.format(src);
  }

  static OffsetDateTime asOffsetDateTime(String raw) {
    return ISO.parse(raw, OffsetDateTime::from);
  }

  Optional<App> registerApp(App app);

  App updateApp(App app);

  App addToApp(App app, Service service);

  App addToApp(App app, Owner owner);

  boolean appHasOwner(String appKey, String username);

  boolean appHasService(String appKey, String ownerKey);

  Optional<App> loadAppByKey(String appKey);
}
