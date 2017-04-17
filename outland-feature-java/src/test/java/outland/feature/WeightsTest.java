package outland.feature;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeightsTest {

  @Test
  public void testNormalize() {
    assertEquals(0.0d, Weights.normalize(0), 0.0d);
    assertEquals(0.0001d, Weights.normalize(1), 0.0d);
    assertEquals(0.001d, Weights.normalize(10), 0.0d);
    assertEquals(0.01d, Weights.normalize(100), 0.0d);
    assertEquals(0.1d, Weights.normalize(1000), 0.0d);
    assertEquals(0.25d, Weights.normalize(2500), 0.0d);
    assertEquals(0.3d, Weights.normalize(3000), 0.0d);
    assertEquals(0.3333d, Weights.normalize(3333), 0.0d);
    assertEquals(0.7001d, Weights.normalize(7001), 0.0d);
    assertEquals(0.9999d, Weights.normalize(9_999), 0.0d);
    assertEquals(1.0d, Weights.normalize(10_000), 0.0d);

    // overflown values are an entry bug, but are capped to 0.0..1.0
    assertEquals(1.0d, Weights.normalize(11_000), 0.0d);
    assertEquals(0.0d, Weights.normalize(-1), 0.0d);
  }
}