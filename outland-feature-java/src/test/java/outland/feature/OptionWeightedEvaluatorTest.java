package outland.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.Test;
import outland.feature.proto.FeatureOption;

import static junit.framework.TestCase.assertTrue;

public class OptionWeightedEvaluatorTest {

  @Test
  public void testFair() {

    /*
    maxweight is 10_000; thus in a 10K iteration we'd expect the option with weight "4000" to be
    selected about 40% of the time.
     */
    final ArrayList<FeatureOption> itemList = Lists.newArrayList(
        FeatureOption.newBuilder().setName("10%").setWeight(1_000).build(),
        FeatureOption.newBuilder().setName("20%").setWeight(2_000).build(),
        FeatureOption.newBuilder().setName("30%").setWeight(3_000).build(),
        FeatureOption.newBuilder().setName("40%").setWeight(4_000).build()
    );

    OptionWeightedEvaluator or = new OptionWeightedEvaluator(itemList);

    final int[] resultFrequencies = new int[4];

    int nearestRoundTo = 1_000;

    /*
     iterate as many times as the max weight; this means a 10% weight should be be hit about
     1000 times, which makes some of the arithmetic below easier
      */
    final int runs = 10_000;
    IntStream.range(0, runs).forEach(i -> {
      /*
       maps our 4 possible results (1000 to 4000) into array slots 0 to 3
        */
      final int fitIntoArray = (or.select().getWeight() / nearestRoundTo) - 1;
      resultFrequencies[fitIntoArray]++;
    });

    final Set<Integer> nearestFrequencies = Sets.newHashSet();
    final int nearestRoundUp = nearestRoundTo / 2; // avoid rounding down to 0 for a number < 1000
    for (int resultFrequency : resultFrequencies) {
      /*
      take the actual frequency and map it to the closest thousandth
       */
      final int nearestFrequency =
          (int) (nearestRoundTo * Math.floor((resultFrequency + nearestRoundUp) / nearestRoundTo));

      nearestFrequencies.add(nearestFrequency);
    }

    /*
    check we have our four expected frequencies
     */
    assertTrue(nearestFrequencies.contains(1_000));
    assertTrue(nearestFrequencies.contains(2_000));
    assertTrue(nearestFrequencies.contains(3_000));
    assertTrue(nearestFrequencies.contains(4_000));
  }

  @Test
  public void testBaseCases() {

    final ArrayList<FeatureOption> itemList1 = Lists.newArrayList(
        FeatureOption.newBuilder().setWeight(0).build(),
        FeatureOption.newBuilder().setWeight(10000).build()
    );
    OptionWeightedEvaluator or1 = new OptionWeightedEvaluator(itemList1);
    IntStream.range(0, 10000).forEach(i -> assertTrue(10000 == or1.select().getWeight()));

    final ArrayList<FeatureOption> itemList2 = Lists.newArrayList(
        FeatureOption.newBuilder().setWeight(10000).build()
    );
    OptionWeightedEvaluator or2 = new OptionWeightedEvaluator(itemList2);
    IntStream.range(0, 10000).forEach(i -> assertTrue(10000 == or2.select().getWeight()));
  }
}