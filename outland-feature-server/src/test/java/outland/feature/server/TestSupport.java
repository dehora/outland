package outland.feature.server;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;

public class TestSupport {

  public static String load(String name) {
    try {
      return Resources.toString(Resources.getResource(name), Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
