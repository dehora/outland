package outland.feature;

import java.net.URI;

/**
 * Allows access to the API via resource classes.
 *
 */
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

  /**
   * The resource for features.
   *
   * @return a resource for working with features.
   */
  public FeatureResource features() {
    return new FeatureResourceReal(
        resourceProvider,
        authorizationProvider,
        namespace,
        baseURI
    );
  }
}
