package outland.feature;

public class Resources {

  private final FeatureClient client;

  Resources(FeatureClient client) {
    this.client = client;
  }

  public FeatureResource features() {
    return new FeatureResourceReal(client);
  }
}
