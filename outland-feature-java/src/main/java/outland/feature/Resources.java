package outland.feature;

import java.net.URI;

public class Resources {

  private final AuthorizationProvider authorizationProvider;
  private final ResourceProvider resourceProvider;
  private final String namespace;
  private final URI baseURI;

  Resources(
      ResourceProvider resourceProvider,
      AuthorizationProvider authorizationProvider,
      String namespace,
      URI baseUri
  ) {
    this.resourceProvider = resourceProvider;
    this.authorizationProvider = authorizationProvider;
    this.namespace = namespace;
    this.baseURI = baseUri;
  }

  public FeatureResource features() {
    return new FeatureResourceReal(
        resourceProvider,
        authorizationProvider,
        namespace,
        baseURI
    );
  }
}
