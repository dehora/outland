package outland.feature.server;

import java.time.OffsetDateTime;
import outland.feature.server.features.Ulid;

public class Names {

  public static String owner() {
    return "own_" + Ulid.random();
  }

  public static String service() {
    return "svc_" + Ulid.random();
  }

  public static String namespaceFeature() {
    return "nsfeature_" + Ulid.random();
  }

  public static String option() {
    return "opt_" + Ulid.random();
  }

  public static String member() {
    return "mbr_" + Ulid.random();
  }

  public static String feature(OffsetDateTime time) {
    return "feat_" + Ulid.random(time.toInstant().toEpochMilli());
  }

  public static String group(OffsetDateTime time) {
    return "group_" + Ulid.random(time.toInstant().toEpochMilli());
  }
}
