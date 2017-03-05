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
  private final String appId;
  private final URI baseUri;
  private final boolean multiAppEnabled;

  FeatureResourceReal(
      ResourceProvider resourceProvider,
      AuthorizationProvider authorizationProvider,
      String appId,
      URI baseUri,
      boolean multiAppEnabled
  ) {
    this.resourceProvider = resourceProvider;
    this.authorizationProvider = authorizationProvider;
    this.appId = appId;
    this.baseUri = baseUri;
    this.multiAppEnabled = multiAppEnabled;
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
    FeatureException.throwIfNull(feature.getAppId(), "Please add an app id to the feature");
    FeatureException.throwIfNull(feature.getKey(), "Please add a feature key to the feature");

    try {
      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(feature.getAppId())
          .path(feature.getKey())
          .buildString();

      Response response = postRequest(url, feature);
      return toFeature(response.body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  @Override public Feature findByKey(String featureKey) {
    if(multiAppEnabled) {
      throw new FeatureException(Problem.configProblem("find_by_key_multi_with_no_app_id",
          "Find by key cannot be called with only a feature key when multi app is configured. "
              + "Please supply an app id as well as a feature for multi app configuration."));
    }

    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return findByKey(appId, featureKey);
  }

  @Override public FeatureCollection listFeatures() {
    if(multiAppEnabled) {
      throw new FeatureException(Problem.configProblem("list_features_multi_with_no_app_id",
          "List features cannot be called without an app id key when multi app is configured. "
              + "Please supply an app id for multi app configuration."));
    }

    return listFeatures(appId);
  }

  @Override public FeatureCollection listFeaturesSince(long timestamp, TimeUnit timeUnit) {
    if(multiAppEnabled) {
      throw new FeatureException(Problem.configProblem("list_features_since_multi_with_no_app_id",
          "List features since cannot be called without an app id key when multi app is configured. "
              + "Please supply an app id for multi app configuration."));
    }

    FeatureException.throwIfNull(timeUnit, "Please supply a timeUnit");

    return listFeaturesSince(appId, timestamp, timeUnit);
  }

  @Override
  public Feature findByKey(String appId, String featureKey) {
    FeatureException.throwIfNull(appId, "Please supply an appId");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return findByKeyInner(appId, featureKey);
  }

  @Override
  public FeatureCollection listFeatures(String appId) {
    FeatureException.throwIfNull(appId, "Please supply an appId");

    return listFeaturesInner(appId);
  }

  @Override
  public FeatureCollection listFeaturesSince(String appId, long timestamp, TimeUnit timeUnit) {
    FeatureException.throwIfNull(appId, "Please supply an appId");
    FeatureException.throwIfNull(timeUnit, "Please supply a timeUnit");

    return listFeaturesSinceInner(appId, timestamp, timeUnit);
  }

  private Feature findByKeyInner(String appId, String key) {
    try {

      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(appId)
          .path(key)
          .buildString();

      return toFeature(getRequest(appId, url).body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  private FeatureCollection listFeaturesInner(String appId) {
    try {

      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(appId)
          .buildString();

      return toFeatureCollection(getRequest(appId, url).body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  private FeatureCollection listFeaturesSinceInner(String appId, long timestamp,
      TimeUnit timeUnit) {
    try {
      long ts = timeUnit.toSeconds(timestamp);

      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(appId)
          .query(PARAM_SINCE, ts + "")
          .buildString();

      return toFeatureCollection(getRequest(appId, url).body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  private FeatureCollection toFeatureCollection(String json) throws InvalidProtocolBufferException {
    FeatureCollection.Builder builder = FeatureCollection.newBuilder();
    JsonFormat.parser().merge(json, builder);
    return builder.build();
  }

  private Feature toFeature(String json) throws InvalidProtocolBufferException {
    Feature.Builder builder = Feature.newBuilder();
    JsonFormat.parser().merge(json, builder);
    return builder.build();
  }

  private Response getRequest(String appId, String url) {
    return resourceProvider
        .newResource()
        .requestThrowing(Resource.GET, url, prepareOptions(appId));
  }

  private Response postRequest(String url, Feature feature) {
    return resourceProvider
        .newResource()
        .requestThrowing(Resource.POST, url, prepareOptions(feature.getAppId()), feature);
  }

  private ResourceOptions prepareOptions(String appId) {
    return ResourceSupport.options(APPLICATION_JSON)
        .appId(appId)
        .securityTokenProvider(authorizationProvider);
  }
}
