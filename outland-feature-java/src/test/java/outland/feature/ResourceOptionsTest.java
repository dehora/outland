package outland.feature;

import com.google.common.collect.Maps;
import java.util.HashMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class ResourceOptionsTest {

  @Test
  public void testGroup() {
    ResourceOptions ro = new ResourceOptions();

    try {
      ro.group(null);
      fail();
    } catch (Exception ignored) {
    }

    try {
      ro.group("");
      fail();
    } catch (Exception ignored) {
    }

    assertEquals("grp", ro.group("grp").group());
  }

  @Test
  public void testScope() {
    ResourceOptions ro = new ResourceOptions();

    try {
      ro.scope(null);
      fail();
    } catch (Exception ignored) {
    }

    assertEquals("foo", ro.scope("foo").scope());
  }

  @Test
  public void testHeaders() {
    ResourceOptions ro = new ResourceOptions();

    try {
      ro.headers(null);
      fail();
    } catch (Exception ignored) {
    }

    final HashMap<String, Object> headers = Maps.newHashMap();
    headers.put("Foo", "Bar");
    ro.headers(headers).headers();
    headers.put("Baz", "Quux");
    ro.headers(headers).headers();
    ro.header("A", "B");

    final HashMap<String, Object> headers1 = Maps.newHashMap();
    headers1.put("Foo", "Bar");
    headers1.put("Baz", "Quux");
    headers1.put("A", "B");

    assertEquals(headers1, ro.headers());
  }

}