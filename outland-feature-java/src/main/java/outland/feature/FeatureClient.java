package outland.feature;

import com.codahale.metrics.MetricRegistry;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Feature;
import outland.feature.proto.OptionType;

/**
 * A client which can be used to check feature state and access the feature management APIs.
 * Clients must be created via a {@link FeatureClient.Builder}.
 */
public class FeatureClient {

  private static final Logger logger = LoggerFactory.getLogger(FeatureClient.class.getSimpleName());

  private final ServerConfiguration serverConfiguration;
  private final FeatureStore featureStore;
  private final AuthorizationProvider authorizationProvider;
  private final ResourceProvider resourceProvider;
  private final Resources resources;
  private final ContentSupport contentSupport;
  private final URI baseURI;
  private final String appKey;
  private final boolean multiAppEnabled;
  private final boolean localStoreEnabled;
  private final MetricRegistry metricRegistry;

  public FeatureClient(Builder builder) {
    this.serverConfiguration = builder.serverConfiguration;
    this.baseURI = serverConfiguration.baseURI();
    this.appKey = serverConfiguration.appKey();
    this.multiAppEnabled = serverConfiguration.multiAppEnabled();
    this.localStoreEnabled = serverConfiguration.localStoreEnabled();
    this.authorizationProvider = builder.authorizationProvider;
    this.resourceProvider = builder.resourceProvider;
    this.contentSupport = builder.contentSupport;
    this.metricRegistry = builder.metricRegistry;
    this.resources = new Resources(
        this.resourceProvider,
        this.authorizationProvider,
        this.appKey,
        this.baseURI,
        this.multiAppEnabled
    );
    FeatureStoreLocal localFeatureStore = builder.localFeatureStore;

    /*
     create the cache via the client as the cache needs a client reference to pull from the server.
      If the featureStore is present on the builder it means we've set a cache for testing
      */
    if (builder.featureStore == null) {
      final FeatureStoreReal featureStore = new FeatureStoreReal(this, localFeatureStore);
      builder.featureStore = new FeatureStoreMetered(featureStore, builder.metricsContext);

      featureStore.open();
    }

    this.featureStore = builder.featureStore;
  }

  /**
   * Get a builder that can construct a new client. Clients can't be created directly.
   * <p>
   * If the feature is not found, the result is always false.
   * </p>
   *
   * @return a new builder
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Check and see if a feature is enabled.
   * <p>
   *   The client stores feature state locally in a cache, and will periodically refresh
   *   the cache record for a key by checking the remote API it's configured to use.
   * </p>
   * <p>
   *   The feature is considered enabled if the underlying {@link Feature} has a state equal
   *   to  {@link Feature.State#on}. The call will return false for the following scenarios:
   *   <ol>
   *     <li>If the underlying {@link Feature} has a state equal to {@link Feature.State#off}.</li>
   *     <li>If the feature does not exist.</li>
   *     <li>If there was an internal error. </li>
   *   </ol>
   * </p>
   * <p>
   *   <b>Note</b> the last point, the client is designed generally to not throw exceptions
   *   at the call site, preferring instead to return false and act as if the feature was
   *   disabled or non-existent. Generally this means you should strongly prefer to write code
   *   that fires when the feature is on, rather than on the basis the feature is off. If you
   *   want to throw an exception for a missing feature, use {@link #enabledThrowing}.
   * </p>
   *
   * @param featureKey the feature key defined for the feature
   * @throws FeatureException if the supplied featureKey is null.
   * @return true if the feature is enabled. Returns false if the feature is not enabled, not
   * known, or there was an internal error.
   */
  @SuppressWarnings("WeakerAccess") public boolean enabled(String featureKey) {
    throwIfMultiApp();
    FeatureException.throwIfNull(featureKey, "Please supply a feature key");

    return enabledInner(appKey(), featureKey);
  }

  /**
   * Check and see if a feature is enabled.
   * <p>
   * This is same as {@link #enabled(String)} except it will throw a {@link FeatureException}
   * if the feature does not exist or there is an internal exception.
   * </p>
   *
   * @param featureKey the feature key defined for the feature
   * @return true if the feature is enabled ({@link Feature.State#on}). Returns false if the feature
   * is not enabled ({@link Feature.State#off}).
   * @throws FeatureException if the supplied featureKey is null, the feature does not exist or
   * there was an internal error.
   */
  @SuppressWarnings("WeakerAccess") public boolean enabledThrowing(String featureKey)
      throws FeatureException {
    throwIfMultiApp();
    FeatureException.throwIfNull(featureKey, "Please supply a feature key");

    return enabledThrowingInner(appKey(), featureKey);
  }

  @SuppressWarnings("WeakerAccess") public boolean enabledFor(String appKey, String featureKey) {
    FeatureException.throwIfNull(appKey, "Please supply an appKey");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return enabledInner(appKey, featureKey);
  }

  public boolean enabledForThrowing(String appKey, String featureKey) {
    FeatureException.throwIfNull(appKey, "Please supply an appKey");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return enabledThrowingInner(appKey, featureKey);
  }

  /**
   * Access the feature management APIs.
   *
   * @return a way to access feature management APIs.
   */
  @SuppressWarnings("WeakerAccess") public Resources resources() {
    return resources;
  }

  /**
   * Stop the feature client.
   * <p>
   *   This allows the client to clean up and release any cache related resources such as local
   *   storage. For example you can set this up in a shutdown hook as follows:
   * </p>
   * <p>
   *   <code>
   *    &nbsp;&nbsp;Runtime.getRuntime().addShutdownHook(new Thread(client::close));
   *   </code>
   * </p>
   * @throws FeatureException or a subclass.
   */
  @SuppressWarnings("WeakerAccess") public void close() throws FeatureException {
    logger.info("op=close, action=releasing_storage_resources");
    featureStore.close();
  }

  ServerConfiguration serverConfiguration() {
    return serverConfiguration;
  }

  AuthorizationProvider authorizationProvider() {
    return authorizationProvider;
  }

  ResourceProvider resourceProvider() {
    return resourceProvider;
  }

  ContentSupport contentSupport() {
    return contentSupport;
  }

  MetricRegistry metricRegistry() {
    return metricRegistry;
  }

  URI baseURI() {
    return baseURI;
  }

  String appKey() {
    return appKey;
  }

  boolean multiAppEnabled() {
    return this.multiAppEnabled;
  }

  boolean localStoreEnabled() {
    return localStoreEnabled;
  }

  private void throwIfMultiApp() {
    if (multiAppEnabled()) {
      throw new FeatureException(Problem.configProblem("enabled_check_and_multi_with_no_appkey",
          "A feature flag check cannot be called without an app id when multi app is configured. "
              + "Please use the app id plus feature key variants for multi app configuration."));
    }
  }

  private boolean enabledInner(String appKey, String featureKey) {
    final Optional<Feature> maybe = featureStore.find(appKey, featureKey);

    if(! maybe.isPresent()) {
      return false;
    }

    final Feature feature = maybe.get();

    if(feature.getOptions().getOption().equals(OptionType.flag)) {
      return feature.getState().equals(Feature.State.on);
    }

    if(feature.getOptions().getOption().equals(OptionType.bool)) {
      return new OptionEvaluator().evaluateBooleanOptions(feature);
    }

    return false;
  }

  private boolean enabledThrowingInner(String appKey, String featureKey) {
    final Optional<Feature> maybe = featureStore.find(appKey, featureKey);

    if(! maybe.isPresent()) {
      throw new FeatureException(
          Problem.noSuchFeature("feature_not_found",
              String.format(
                  "feature %s for app %s was not found and raising an error was requested",
                  featureKey, appKey)));
    }

    final Feature feature = maybe.get();

    if(feature.getOptions().getOption().equals(OptionType.flag)) {
      return feature.getState().equals(Feature.State.on);
    }

    if(feature.getOptions().getOption().equals(OptionType.bool)) {
      return new OptionEvaluator().evaluateBooleanOptions(feature);
    }

    return false;
  }

  public static class Builder {

    private ServerConfiguration serverConfiguration;
    private AuthorizationProvider authorizationProvider;
    private ResourceProvider resourceProvider;
    private FeatureStore featureStore;
    private FeatureStoreLocal localFeatureStore;
    private ContentSupport contentSupport;
    private MetricRegistry metricRegistry;
    private MetricsContext metricsContext;
    private String metricsContextName;

    Builder() {
    }

    public FeatureClient build() {

      if (serverConfiguration == null) {
        throw new FeatureException(Problem.localProblem(
            "Please supply a configuration", ""));
      }

      if (metricsContextName == null) {
        metricsContextName = "outland.feature";
      }

      if (authorizationProvider == null) {
        authorizationProvider = new EmptyAuthorizationProvider();
      }

      if (metricRegistry == null) {
        metricRegistry = new MetricRegistry();
      }

      if (contentSupport == null) {
        contentSupport = new ContentSupport();
      }

      if (serverConfiguration != null) {

        serverConfiguration.validate();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(serverConfiguration.connectTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(serverConfiguration.readTimeout(), TimeUnit.MILLISECONDS);

        if (serverConfiguration.certificatePath() != null) {
          new CertificateLoader(
              serverConfiguration.certificatePath()).applySslSocketFactory(builder);
        }


        if (serverConfiguration.httpLoggingEnabled()) {
          builder = builder.addNetworkInterceptor(
              new HttpLoggingInterceptor(new okhttp3.logging.HttpLoggingInterceptor.Logger() {

                final Logger httpLogger = LoggerFactory.getLogger("FeatureClientHttpLog");

                @Override public void log(String message) {
                  httpLogger.info(message);
                }
              }).setLevel(HttpLoggingInterceptor.Level.BODY));
          logger.info("http tracing enabled");
        }

        resourceProvider =
            new OkHttpResourceProvider(builder.build(), contentSupport, metricRegistry);
      }

      metricsContext = new MetricsContext(metricsContextName, metricRegistry);

      if (serverConfiguration.localStoreEnabled() && localFeatureStore == null) {
        localFeatureStore = new FeatureStoreRocksDb(metricsContext);
      } else {
        localFeatureStore = new FeatureStoreLocalNone();
      }

      return new FeatureClient(this);
    }

    @SuppressWarnings("WeakerAccess") public Builder serverConfiguration(
        ServerConfiguration serverConfiguration) {
      this.serverConfiguration = serverConfiguration;
      return this;
    }

    @SuppressWarnings("WeakerAccess")
    public Builder authorizationProvider(AuthorizationProvider authorizationProvider) {
      this.authorizationProvider = authorizationProvider;
      return this;
    }

    @SuppressWarnings("WeakerAccess") public Builder metricRegistry(MetricRegistry metricRegistry) {
      this.metricRegistry = metricRegistry;
      return this;
    }

    @VisibleForTesting
    Builder featureStore(FeatureStore featureStore) {
      this.featureStore = featureStore;
      return this;
    }

    @VisibleForTesting
    Builder localFeatureStore(FeatureStoreLocal localFeatureStore) {
      this.localFeatureStore = localFeatureStore;
      return this;
    }
  }
}
