package outland.feature;

import com.google.protobuf.InvalidProtocolBufferException;
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
  private final String group;
  private final URI baseUri;
  private final FeatureSupport featureSupport = new FeatureSupport();

  FeatureResourceReal(
      ResourceProvider resourceProvider,
      AuthorizationProvider authorizationProvider,
      String group,
      URI baseUri
  ) {
    this.resourceProvider = resourceProvider;
    this.authorizationProvider = authorizationProvider;
    this.group = group;
    this.baseUri = baseUri;
  }

  @Override public Feature register(Feature feature) {
    FeatureException.throwIfNull(feature, "Please supply a feature");

    try {
      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(feature.getGroup())
          .buildString();

      Response response = postRequest(url, feature);
      return toFeature(response.body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  @Override public Feature update(Feature feature) {
    FeatureException.throwIfNull(feature, "Please supply a feature");
    FeatureException.throwIfNull(feature.getGroup(), "Please add a group key to the feature");
    FeatureException.throwIfNull(feature.getKey(), "Please add a feature key to the feature");

    try {
      String url = UriBuilder.builder(baseUri)
          .path(PATH_FEATURES)
          .path(feature.getGroup())
          .path(feature.getKey())
          .buildString();

      Response response = postRequest(url, feature);
      return toFeature(response.body().string());
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  @Override public Feature findByKey(String featureKey) {
    if (group == null) {
      throw new FeatureException(Problem.configProblem("find_by_key_with_no_group",
          "Find by key cannot be called with only a feature key and no default group configured. "
              + "Please configure a default group or use the group/feature method."));
    }

    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return findByKey(group, featureKey);
  }

  @Override public FeatureCollection listFeatures() {
    if (group == null) {
      throw new FeatureException(Problem.configProblem("list_features_multi_with_no_group",
          "List features cannot be called with only a feature key and no default group configured."
              + "Please configure a default group or use the group/feature method."));
    }

    return listFeatures(group);
  }

  @Override public FeatureCollection listFeaturesSince(long timestamp, TimeUnit timeUnit) {
    if (group == null) {
      throw new FeatureException(
          Problem.configProblem("list_features_since_multi_with_no_group_key",
              "List features since cannot be called with only a feature key and no default group configured."
                  + "Please configure a default group or use the group/feature method."));
    }

    FeatureException.throwIfNull(timeUnit, "Please supply a timeUnit");

    return listFeaturesSince(group, timestamp, timeUnit);
  }

  @Override
  public FeatureCollection listFeatures(String group) {
    FeatureException.throwIfNull(group, "Please supply a group");

    return listFeaturesInner(group);
  }

  @Override
  public Feature findByKey(String group, String featureKey) {
    FeatureException.throwIfNull(group, "Please supply a group");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return findByKeyInner(group, featureKey);
  }

  @Override
  public FeatureCollection listFeaturesSince(String group, long timestamp, TimeUnit timeUnit) {
    FeatureException.throwIfNull(group, "Please supply a group");
    FeatureException.throwIfNull(timeUnit, "Please supply a timeUnit");

    return listFeaturesSinceInner(group, timestamp, timeUnit);
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
    return featureSupport.toFeatureCollection(json);
  }

  private Feature toFeature(String json) throws InvalidProtocolBufferException {
    return featureSupport.toFeature(json);
  }

  private Response getRequest(String nsKey, String url) {
    return resourceProvider
        .newResource()
        .requestThrowing(Resource.GET, url, prepareOptions(nsKey));
  }

  private Response postRequest(String url, Feature feature) {
    return resourceProvider
        .newResource()
        .requestThrowing(Resource.POST, url, prepareOptions(feature.getGroup()), feature);
  }

  private ResourceOptions prepareOptions(String group) {
    return ResourceSupport.options(APPLICATION_JSON)
        .group(group)
        .securityTokenProvider(authorizationProvider);
  }
}
