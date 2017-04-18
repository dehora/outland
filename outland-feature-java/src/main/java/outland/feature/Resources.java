package outland.feature;

import java.net.URI;

/**
 * Allows access to the API via resource classes.
 */
public class Resources {

  private final AuthorizationProvider authorizationProvider;
  private final ResourceProvider resourceProvider;
  private final String group;
  private final URI baseURI;

  Resources(
      ResourceProvider resourceProvider,
      AuthorizationProvider authorizationProvider,
      String group,
      URI baseUri
  ) {
    this.resourceProvider = resourceProvider;
    this.authorizationProvider = authorizationProvider;
    this.group = group;
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
        group,
        baseURI
    );
  }
}
