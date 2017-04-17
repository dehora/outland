package outland.feature;

import java.util.Arrays;
import java.util.List;
import outland.feature.proto.FeatureOption;

class OptionWeightedEvaluator {

  private final double[] wheel;
  private final FeatureOption[] values;

  OptionWeightedEvaluator(List<FeatureOption> options) {
    wheel = buildWheel(options);
    values = buildValues(options);
  }

  FeatureOption select() {
    final double randomValue = Math.random();
    int idx = Arrays.binarySearch(wheel, randomValue);
    if (idx < 0) {
      idx = -idx - 1;
    }
    return values[idx];
  }

  private FeatureOption[] buildValues(List<FeatureOption> options) {
    return options.toArray(new FeatureOption[options.size()]);
  }

  private double[] buildWheel(List<FeatureOption> options) {

    // loosely based on https://en.wikipedia.org/wiki/Fitness_proportionate_selection

    double[] wheel = new double[options.size()];
    int sumWeights = 0;
    int pos = 0;
    for (FeatureOption featureOption : options) {
      sumWeights += featureOption.getWeight();
      wheel[pos] = normalize(sumWeights);
      pos++;
    }
    return wheel;
  }

  private double normalize(int weight) {
    return Weights.normalize(weight);
  }
}
