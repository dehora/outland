package outland.feature.server.features;

import org.junit.Test;

import static org.junit.Assert.*;

public class UlidTest {

  @Test
  public void testLowerCase() {
    for(int i=0; i< 100; ++i) {
      final String ulid = Ulid.random();
      assertEquals(ulid, ulid.toLowerCase());
    }
  }

}