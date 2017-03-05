package outland.feature;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

class ContentSupport {

  private static final String YYYY_MM_DD_T_HH_MM_SS_SSSXXX = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  private static final Type OFFSET_DATE_TIME_TYPE = new TypeToken<OffsetDateTime>() {
  }.getType();

  private static Gson gson() {
    return GsonHolder.INSTANCE;
  }

  static byte[] toJsonBytes(Message message) {
    try {
      String json = JsonFormat.printer().preservingProtoFieldNames().print(message);
      return json.getBytes(StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  static <T> T fromJson(String raw, Class<T> c) {
    if (String.class.isAssignableFrom(c)) {
      //noinspection unchecked
      return (T) raw;
    }
    return gson().fromJson(raw, c);
  }

  private static class GsonHolder {
    private static final Gson INSTANCE = new GsonBuilder()
        .setPrettyPrinting()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(OFFSET_DATE_TIME_TYPE, new OffsetDateTimeSerdes())
        .setDateFormat(YYYY_MM_DD_T_HH_MM_SS_SSSXXX)
        .create();
  }
}
