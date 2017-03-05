package outland.feature.server;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import java.util.Map;

public class StructLog {

  public static String kvp(Object... objects) {
    return asString(asMap(objects));
  }

  private static String asString(Map<String, Object> map) {
    return Joiner.on(",").withKeyValueSeparator("=").join(map.entrySet());
  }

  private static Map<String, Object> asMap(Object... objects) {
    if (objects.length % 2 == 1) {
      throw new IllegalArgumentException("Please supply an even number of objects");
    }

    final Map<String, Object> result = Maps.newLinkedHashMap(); // maintain insertion order
    for (int i = 0; i < objects.length; i += 2) {
      result.put(objects[i].toString(), objects[i + 1]);
    }
    return result;
  }
}
