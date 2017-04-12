package outland.feature.server.features;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class TimeSupport {

  private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  static String asString(OffsetDateTime src) {
    return ISO.format(src);
  }

  static OffsetDateTime asOffsetDateTime(String raw) {
    return ISO.parse(raw, OffsetDateTime::from);
  }
}
