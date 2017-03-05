package outland.feature;

import java.net.URI;

public class Resources {

  private final AuthorizationProvider authorizationProvider;
  private final ResourceProvider resourceProvider;
  private final String appId;
  private final URI baseURI;
  private final boolean multiAppEnabled;

  Resources(
      ResourceProvider resourceProvider,
      AuthorizationProvider authorizationProvider,
      String appId,
      URI baseUri,
      boolean multiAppEnabled
  ) {
    this.resourceProvider = resourceProvider;
    this.authorizationProvider = authorizationProvider;
    this.appId = appId;
    this.baseURI = baseUri;
    this.multiAppEnabled = multiAppEnabled;
  }

  public FeatureResource features() {
    return new FeatureResourceReal(
        resourceProvider,
        authorizationProvider,
        appId,
        baseURI,
        multiAppEnabled
    );
  }
}
