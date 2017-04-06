package outland.feature;

import java.net.URI;

public class Resources {

  private final AuthorizationProvider authorizationProvider;
  private final ResourceProvider resourceProvider;
  private final String namespace;
  private final URI baseURI;
  private final boolean multiAppEnabled;

  Resources(
      ResourceProvider resourceProvider,
      AuthorizationProvider authorizationProvider,
      String namespace,
      URI baseUri,
      boolean multiAppEnabled
  ) {
    this.resourceProvider = resourceProvider;
    this.authorizationProvider = authorizationProvider;
    this.namespace = namespace;
    this.baseURI = baseUri;
    this.multiAppEnabled = multiAppEnabled;
  }

  public FeatureResource features() {
    return new FeatureResourceReal(
        resourceProvider,
        authorizationProvider,
        namespace,
        baseURI,
        multiAppEnabled
    );
  }
}
