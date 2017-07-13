package outland.feature;

import java.util.stream.IntStream;
import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OptionSelectorBoolTest {

  @Test
  public void testNotOne() {
    OptionSelectorBool oe = new OptionSelectorBool();

    final Feature feature = Feature.newBuilder()
        .setStatus(Status.off)
        .build();

    final OptionSelectorWeighted selector =
        new OptionSelectorWeighted(feature.getOptions().getItemsList());

    assertFalse(oe.selectBooleanOptions(feature.getOptions(), feature.getStatus(), selector));

    final Feature feature1 = Feature.newBuilder()
        .setStatus(Status.none)
        .build();

    final OptionSelectorWeighted selector1 =
        new OptionSelectorWeighted(feature.getOptions().getItemsList());

    assertFalse(oe.selectBooleanOptions(feature1.getOptions(), feature.getStatus(), selector1));
  }

  @Test
  public void testOptionCollectionFlag() {
    OptionSelectorBool oe = new OptionSelectorBool();

    OptionCollection collection = OptionCollection.newBuilder()
        .setOption(OptionType.flag)
        .build();

    final Feature feature1 = Feature.newBuilder()
        .setOptions(collection)
        .setStatus(Status.on)
        .build();

    assertFalse(oe.selectBooleanOptions(
        feature1.getOptions(),
        feature1.getStatus(),
        new OptionSelectorWeighted(feature1.getOptions().getItemsList())
    ));
  }

  @Test
  public void testZeroWeight() {
    FeatureOption f = FeatureOption.newBuilder()
        .setKey("false").setValue("false").setWeight(0).build();

    FeatureOption t = FeatureOption.newBuilder()
        .setKey("true").setValue("true").setWeight(0).build();

    OptionCollection collection = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(t)
        .addItems(f)
        .buildPartial();

    final Feature feature1 = Feature.newBuilder()
        .setOptions(collection)
        .setStatus(Status.on)
        .build();

    OptionSelectorBool oe = new OptionSelectorBool();
    assertEquals(true, oe.selectBooleanOptions(
        feature1.getOptions(),
        feature1.getStatus(),
        new OptionSelectorWeighted(feature1.getOptions().getItemsList())

    ));
  }

  @Test
  public void testSelectFair() {

    FeatureOption f = FeatureOption.newBuilder()
        .setKey("false").setValue("false").setWeight(5_000).build();

    FeatureOption t = FeatureOption.newBuilder()
        .setKey("true").setValue("true").setWeight(5_000).build();

    OptionCollection collection = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(t)
        .addItems(f)
        .buildPartial();

    final Feature feature1 = Feature.newBuilder()
        .setOptions(collection).setStatus(Status.on)
        .build();

    OptionSelectorBool oe = new OptionSelectorBool();
    oe.selectBooleanOptions(
        feature1.getOptions(),
        feature1.getStatus(),
        new OptionSelectorWeighted(feature1.getOptions().getItemsList())
    );

    final int[] fCount = {0};
    final int[] tCount = {0};

    final int runs = 10000;

    IntStream.range(0, runs).forEach(
        i -> {
          @SuppressWarnings("unused")
          int noop = oe.selectBooleanOptions(
              feature1.getOptions(),
              feature1.getStatus(),
              new OptionSelectorWeighted(feature1.getOptions().getItemsList())
          ) ? tCount[0]++ : fCount[0]++;
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
  public void testSelectAlwaysTrue() {

    FeatureOption f = FeatureOption.newBuilder()
        .setKey("false").setValue("false").setWeight(10_000).build();

    FeatureOption t = FeatureOption.newBuilder()
        .setKey("true").setValue("true").setWeight(0).build();

    OptionCollection collection = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(t)
        .addItems(f)
        .buildPartial();

    final Feature feature1 = Feature.newBuilder()
        .setOptions(collection).setStatus(Status.on)
        .build();

    OptionSelectorBool oe = new OptionSelectorBool();

    IntStream.range(0, 10000).forEach(i -> assertFalse(oe.selectBooleanOptions(
        feature1.getOptions(),
        feature1.getStatus(),
        new OptionSelectorWeighted(feature1.getOptions().getItemsList())
    )));
  }

  @Test
  public void testSelectAlwaysFalse() {

    FeatureOption f = FeatureOption.newBuilder()
        .setKey("false").setValue("false").setWeight(0).build();

    FeatureOption t = FeatureOption.newBuilder()
        .setKey("true").setValue("true").setWeight(10_000).build();

    OptionCollection collection = OptionCollection.newBuilder()
        .setOption(OptionType.bool)
        .addItems(t)
        .addItems(f)
        .buildPartial();

    final Feature feature1 = Feature.newBuilder()
        .setOptions(collection).setStatus(Status.on)
        .build();

    OptionSelectorBool oe = new OptionSelectorBool();

    IntStream.range(0, 1000).forEach(i -> assertTrue(oe.selectBooleanOptions(
        feature1.getOptions(),
        feature1.getStatus(),
        new OptionSelectorWeighted(feature1.getOptions().getItemsList())
    )));
  }

  @Test
  public void testSelect80_20() {
    final int tWeight = 2_000;
    final int fWeight = 8_000;
    final int runs = 10000;
    int nearestNth = runs / 10;
    testSelect_Smaller_to_Larger_Bias(tWeight, fWeight, runs, nearestNth);
  }

  @Test
  public void testSelect90_10() {
    final int tWeight = 1_000;
    final int fWeight = 9_000;
    final int runs = 10000;
    int nearestNth = runs / 10;
    testSelect_Smaller_to_Larger_Bias(tWeight, fWeight, runs, nearestNth);
  }

  @Test
  public void testSelect60_40() {
    final int tWeight = 4_000;
    final int fWeight = 6_000;
    final int runs = 10000;
    int nearestNth = runs / 10;

    testSelect_Smaller_to_Larger_Bias(tWeight, fWeight, runs, nearestNth);
  }

  private void testSelect_Smaller_to_Larger_Bias(int tWeight, int fWeight, int runs, int nearestNth) {

    final int ratio = fWeight/tWeight;

    FeatureOption f = FeatureOption.newBuilder()
        .setKey("false")
        .setValue("false")
        .setWeight(fWeight)
        .build();

    FeatureOption t = FeatureOption.newBuilder()
        .setKey("true")
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
        .setStatus(Status.on  )
        .build();

    OptionSelectorBool oe = new OptionSelectorBool();
    oe.selectBooleanOptions(
        feature1.getOptions(),
        feature1.getStatus(),
        new OptionSelectorWeighted(feature1.getOptions().getItemsList())
    );

    final int[] fCount = {0};
    final int[] tCount = {0};

    IntStream.range(0, runs).forEach(
        i -> {
          @SuppressWarnings("unused")
          int noop = oe.selectBooleanOptions(
              feature1.getOptions(),
              feature1.getStatus(),
              new OptionSelectorWeighted(feature1.getOptions().getItemsList())
          ) ? tCount[0]++ : fCount[0]++;
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