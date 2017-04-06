package outland.feature;

import java.util.concurrent.TimeUnit;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

/**
 * Supports API operations related to features.
 */
public interface FeatureResource {

  /**
   * Create a new feature.
   * <p>
   *   Other the a null argument check the client does not validate what is sent to server.
   * </p>
   * @param feature the new feature
   * @return the resulting feature returned by the server.
   * @throws FeatureException for a request error or null argument.
   */
  Feature register(Feature feature) throws FeatureException;

  /**
   * Update a new feature.
   * <p>
   *   Other the a null argument check the client does not validate what is sent to server.
   * </p>
   * @param feature  the updated feature.
   * @return the resulting feature update returned by the server.
   * @throws FeatureException for a request error or null argument.
   */
  Feature update(Feature feature) throws FeatureException;

  /**
   * Find a feature in the "default" namespace.
   * <p>
   * This requires the {@link ServerConfiguration#defaultNamespace(String)} has been configured.
   * Calling it without a configuration results in a {@link FeatureException}.
   * </p>
   *
   * @param featureKey the feature key.
   * @return the feature
   * @throws FeatureException for a request error or missing default namespace
   */
  Feature findByKey(String featureKey) throws FeatureException;

  /**
   * Read features for the "default" namespace.
   * <p>
   * This requires the {@link ServerConfiguration#defaultNamespace(String)} has been configured.
   * Calling it without a configuration results in a {@link FeatureException}.
   * </p>
   *
   * @return the results in a collection.
   * @throws FeatureException for a request error or missing default namespace
   * @see ServerConfiguration#defaultNamespace
   * @throws FeatureException for a request error or null/empty arguments
   */
  FeatureCollection listFeatures() throws FeatureException;

  /**
   * Read features that have been changed or created since a given time.
   *
   * @param timestamp the time to read as far back as (always UTC).
   * @param timeUnit the time unit.
   * @return the results in a collection.
   * @throws FeatureException for a request error or null/empty arguments
   */
  FeatureCollection listFeaturesSince(long timestamp, TimeUnit timeUnit) throws FeatureException;

  /**
   * Read features for a given namespace.
   *
   * @param namespace the feature namespace.
   * @return the results in a collection.
   * @throws FeatureException for a request error or null/empty arguments
   */
  FeatureCollection listFeatures(String namespace) throws FeatureException;

  /**
   * Find a feature in a  namespace.
   *
   * @param namespace the feature namespace
   * @param featureKey the feature key.
   * @return the feature
   * @throws FeatureException for a request error or null/empty arguments
   */
  Feature findByKey(String namespace, String featureKey) throws FeatureException;

  /**
   * Read features that have been changed or created since a given time.
   *
   * @param namespace the feature namespace
   * @param timestamp the time to read as far back as (always UTC).
   * @param timeUnit the time unit.
   * @return the results in a collection.
   * @throws FeatureException for a request error or null/empty arguments
   */
  FeatureCollection listFeaturesSince(String namespace, long timestamp, TimeUnit timeUnit)
      throws FeatureException;
}
