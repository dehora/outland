package outland.feature;

import java.util.stream.IntStream;
import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OptionEvaluatorTest {

  @Test
  public void testNormalize() {
    OptionEvaluator oe = new OptionEvaluator();

    assertEquals(0.0d, oe.normalize(0), 0.0d);
    assertEquals(0.0001d, oe.normalize(1), 0.0d);
    assertEquals(0.001d, oe.normalize(10), 0.0d);
    assertEquals(0.01d, oe.normalize(100), 0.0d);
    assertEquals(0.1d, oe.normalize(1000), 0.0d);
    assertEquals(0.25d, oe.normalize(2500), 0.0d);
    assertEquals(0.3d, oe.normalize(3000), 0.0d);
    assertEquals(0.3333d, oe.normalize(3333), 0.0d);
    assertEquals(0.7001d, oe.normalize(7001), 0.0d);
    assertEquals(0.9999d, oe.normalize(9_999), 0.0d);
    assertEquals(1.0d, oe.normalize(10_000), 0.0d);

    // overflown values are an entry bug, but are capped to 0.0..1.0
    assertEquals(1.0d, oe.normalize(11_000), 0.0d);
    assertEquals(0.0d, oe.normalize(-1), 0.0d);
  }

  @Test
  public void testEvalFair() {

    FeatureOption f = FeatureOption.newBuilder()
        .setName("false").setValue("false").setWeight(5_000).build();

    FeatureOption t = FeatureOption.newBuilder()
        .setName("true").setValue("true").setWeight(5_000).build();

    OptionCollection collection = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(t)
        .addItems(f)
        .buildPartial();

    final Feature feature1 = Feature.newBuilder()
        .setOptions(collection).setState(Feature.State.on)
        .build();

    OptionEvaluator oe = new OptionEvaluator();
    oe.evaluateBooleanOptions(feature1);

    final int[] fCount = {0};
    final int[] tCount = {0};

    final int runs = 10000;

    IntStream.range(0, runs).forEach(
        i -> {
          @SuppressWarnings("unused")
          int noop = oe.evaluateBooleanOptions(feature1) ? tCount[0]++ : fCount[0]++;
        }
    );

    int tC = tCount[0];
    int fC = fCount[0];

    int nearestNth = runs / 10;
    int nearestRoundUp = nearestNth / 2;

    final int tr = (int) (nearestNth * Math.floor((tC + nearestRoundUp) / nearestNth));
    final int fr = (int) (nearestNth * Math.floor((fC + nearestRoundUp) / nearestNth));

    assertTrue(tr == fr && (tr + fr == runs));
  }

  @Test
  public void testEvalAlwaysTrue() {

    FeatureOption f = FeatureOption.newBuilder()
        .setName("false").setValue("false").setWeight(10_000).build();

    FeatureOption t = FeatureOption.newBuilder()
        .setName("true").setValue("true").setWeight(0).build();

    OptionCollection collection = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(t)
        .addItems(f)
        .buildPartial();

    final Feature feature1 = Feature.newBuilder()
        .setOptions(collection).setState(Feature.State.on)
        .build();

    OptionEvaluator oe = new OptionEvaluator();

    IntStream.range(0, 10000).forEach(i -> assertFalse(oe.evaluateBooleanOptions(feature1)));
  }

  @Test
  public void testEvalAlwaysFalse() {

    FeatureOption f = FeatureOption.newBuilder()
        .setName("false").setValue("false").setWeight(0).build();

    FeatureOption t = FeatureOption.newBuilder()
        .setName("true").setValue("true").setWeight(10_000).build();

    OptionCollection collection = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(t)
        .addItems(f)
        .buildPartial();

    final Feature feature1 = Feature.newBuilder()
        .setOptions(collection).setState(Feature.State.on)
        .build();

    OptionEvaluator oe = new OptionEvaluator();

    IntStream.range(0, 1000).forEach(i -> assertTrue(oe.evaluateBooleanOptions(feature1)));
  }

  @Test
  public void testEval80_20() {
    final int tWeight = 2_000;
    final int fWeight = 8_000;
    final int runs = 10000;
    int nearestNth = runs / 10;
    testEval_Smaller_to_Larger_Bias(tWeight, fWeight, runs, nearestNth);
  }

  @Test
  public void testEval90_10() {
    final int tWeight = 1_000;
    final int fWeight = 9_000;
    final int runs = 10000;
    int nearestNth = runs / 10;
    testEval_Smaller_to_Larger_Bias(tWeight, fWeight, runs, nearestNth);
  }

  @Test
  public void testEval60_40() {
    final int tWeight = 4_000;
    final int fWeight = 6_000;
    final int runs = 10000;
    int nearestNth = runs / 10;

    testEval_Smaller_to_Larger_Bias(tWeight, fWeight, runs, nearestNth);
  }

  private void testEval_Smaller_to_Larger_Bias(int tWeight, int fWeight, int runs, int nearestNth) {

    final int ratio = fWeight/tWeight;

    FeatureOption f = FeatureOption.newBuilder()
        .setName("false")
        .setValue("false")
        .setWeight(fWeight)
        .build();

    FeatureOption t = FeatureOption.newBuilder()
        .setName("true")
        .setValue("true")
        .setWeight(tWeight)
        .build();

    OptionCollection collection = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(t)
        .addItems(f)
        .buildPartial();

    final Feature feature1 = Feature.newBuilder()
        .setOptions(collection)
        .setState(Feature.State.on  )
        .build();

    OptionEvaluator oe = new OptionEvaluator();
    oe.evaluateBooleanOptions(feature1);

    final int[] fCount = {0};
    final int[] tCount = {0};

    IntStream.range(0, runs).forEach(
        i -> {
          @SuppressWarnings("unused")
          int noop = oe.evaluateBooleanOptions(feature1) ? tCount[0]++ : fCount[0]++;
        }
    );

    int tC = tCount[0];
    int fC = fCount[0];

    int nearestRoundUp = nearestNth / 2;

    final int tr = (int) (nearestNth * Math.floor((tC + nearestRoundUp) / nearestNth));
    final int fr = (int) (nearestNth * Math.floor((fC + nearestRoundUp) / nearestNth));

    assertTrue((tr + fr == runs));
    assertTrue(fr/tr == ratio);
  }
}