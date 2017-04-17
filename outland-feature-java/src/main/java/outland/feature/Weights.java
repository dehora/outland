package outland.feature;

class Weights {

  static final int MAX_WEIGHT = 10_000;

  static double normalize(int weight) {
    return Weights.normalize(weight, MAX_WEIGHT);
  }

  static double normalize(int weight, int maxWeight) {
    double n = (weight - 0.0d) / (maxWeight - 0.0d);

    if (n > 1.0d) {
      return 1.0d;
    }

    if (n < 0.0d) {
      return 0.0d;
    }

    return n;
  }
}
