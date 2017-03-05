package outland.feature.server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StructLogTest {

  @Test
  public void kvp() {
    String kvp = StructLog.kvp("k1", "v1", "k2", "v2");
    assertEquals("k1=v1,k2=v2", kvp);
  }

  @Test
  public void boomIfOdd() {
    try {
      StructLog.kvp("k1", "v1", "k2");
      fail();
    } catch (IllegalArgumentException ignored) {
    }
  }
}