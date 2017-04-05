package outland.feature;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import okhttp3.Response;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

class FeatureResourceReal implements FeatureResource {

  private static final String APPLICATION_JSON = "application/json";
  private static final String PATH_FEATURES = "features";
  private static final String PARAM_SINCE = "since";

  private final ResourceProvider resourceProvider;
  private final AuthorizationProvider authorizationProvider;
  private final String nsKey;
  private final URI baseUri;
  private final boolean multiNamespaceEnabled;

  FeatureResourceReal(
      ResourceProvider resourceProvider,
      AuthorizationProvider authorizationProvider,
      String nsKey,
      URI baseUri,
      boolean multiNamespaceEnabled
  ) {
    this.resourceProvider = resourceProvider;
    this.authorizationProvider = authorizationProvider;
    this.nsKey = nsKey;
    this.baseUri = baseUri;
    this.multiNamespaceEnabled = multiNamespaceEnabled;
  }

  @Override public Feature register(Feature feature) {
    FeatureException.throwIfNull(feature, "Please supply a feature");

    try {
      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .buildString();

      Response response = postRequest(url, feature);
      return toFeature(response.body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  @Override public Feature update(Feature feature) {
    FeatureException.throwIfNull(feature, "Please supply a feature");
    FeatureException.throwIfNull(feature.getNamespace(), "Please add a namespace key to the feature");
    FeatureException.throwIfNull(feature.getKey(), "Please add a feature key to the feature");

    try {
      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(feature.getNamespace())
          .path(feature.getKey())
          .buildString();

      Response response = postRequest(url, feature);
      return toFeature(response.body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  @Override public Feature findByKey(String featureKey) {
    if(multiNamespaceEnabled) {
      throw new FeatureException(Problem.configProblem("find_by_key_multi_with_no_ns_key",
          "Find by key cannot be called with only a feature key when multi namespace is configured. "
              + "Please supply a namespace key as well as a feature for multi namespace configuration."));
    }

    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return findByKey(nsKey, featureKey);
  }

  @Override public FeatureCollection listFeatures() {
    if(multiNamespaceEnabled) {
      throw new FeatureException(Problem.configProblem("list_features_multi_with_no_ns_key",
          "List features cannot be called without a namespace key when multi namespace is configured. "
              + "Please supply a namespace key for multi namespace configuration."));
    }

    return listFeatures(nsKey);
  }

  @Override public FeatureCollection listFeaturesSince(long timestamp, TimeUnit timeUnit) {
    if(multiNamespaceEnabled) {
      throw new FeatureException(Problem.configProblem("list_features_since_multi_with_no_ns_key",
          "List features since cannot be called without a namespace key when multi namespace is configured. "
              + "Please supply a namespace key for multi namespace configuration."));
    }

    FeatureException.throwIfNull(timeUnit, "Please supply a timeUnit");

    return listFeaturesSince(nsKey, timestamp, timeUnit);
  }

  @Override
  public Feature findByKey(String nsKey, String featureKey) {
    FeatureException.throwIfNull(nsKey, "Please supply a nsKey");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return findByKeyInner(nsKey, featureKey);
  }

  @Override
  public FeatureCollection listFeatures(String nsKey) {
    FeatureException.throwIfNull(nsKey, "Please supply a nsKey");

    return listFeaturesInner(nsKey);
  }

  @Override
  public FeatureCollection listFeaturesSince(String nsKey, long timestamp, TimeUnit timeUnit) {
    FeatureException.throwIfNull(nsKey, "Please supply a nsKey");
    FeatureException.throwIfNull(timeUnit, "Please supply a timeUnit");

    return listFeaturesSinceInner(nsKey, timestamp, timeUnit);
  }

  private Feature findByKeyInner(String nsKey, String key) {
    try {

      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(nsKey)
          .path(key)
          .buildString();

      return toFeature(getRequest(nsKey, url).body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  private FeatureCollection listFeaturesInner(String nsKey) {
    try {

      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(nsKey)
          .buildString();

      return toFeatureCollection(getRequest(nsKey, url).body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  private FeatureCollection listFeaturesSinceInner(String nsKey, long timestamp,
      TimeUnit timeUnit) {
    try {
      long ts = timeUnit.toSeconds(timestamp);

      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(nsKey)
          .query(PARAM_SINCE, ts + "")
          .buildString();

      return toFeatureCollection(getRequest(nsKey, url).body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  private FeatureCollection toFeatureCollection(String json) throws InvalidProtocolBufferException {
    FeatureCollection.Builder builder = FeatureCollection.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
    return builder.build();
  }

  private Feature toFeature(String json) throws InvalidProtocolBufferException {
    Feature.Builder builder = Feature.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
    return builder.build();
  }

  private Response getRequest(String nsKey, String url) {
    return resourceProvider
        .newResource()
        .requestThrowing(Resource.GET, url, prepareOptions(nsKey));
  }

  private Response postRequest(String url, Feature feature) {
    return resourceProvider
        .newResource()
        .requestThrowing(Resource.POST, url, prepareOptions(feature.getNamespace()), feature);
  }

  private ResourceOptions prepareOptions(String nsKey) {
    return ResourceSupport.options(APPLICATION_JSON)
        .appKey(nsKey)
        .securityTokenProvider(authorizationProvider);
  }
}
