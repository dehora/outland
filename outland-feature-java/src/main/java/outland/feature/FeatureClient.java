package outland.feature;

import com.codahale.metrics.MetricRegistry;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Feature;
import outland.feature.proto.OptionType;
import outland.feature.proto.State;

/**
 * A client which can be used to check feature state and access the feature management APIs.
 * Clients must be created via a {@link FeatureClient.Builder}.
 *
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
  private final String defaultGroup;
  private final String namespace;
  private final boolean localStoreEnabled;
  private final MetricRegistry metricRegistry;


  public FeatureClient(Builder builder) {
    this.serverConfiguration = builder.serverConfiguration;
    this.baseURI = serverConfiguration.baseURI();
    this.defaultGroup = serverConfiguration.defaultGroup();
    this.namespace = serverConfiguration.namespace();
    this.localStoreEnabled = serverConfiguration.localStoreEnabled();
    this.authorizationProvider = builder.authorizationProvider;
    this.resourceProvider = builder.resourceProvider;
    this.contentSupport = builder.contentSupport;
    this.metricRegistry = builder.metricRegistry;
    this.resources = new Resources(
        this.resourceProvider,
        this.authorizationProvider,
        this.defaultGroup,
        this.baseURI
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
   *
   * @return a new builder
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Check and see if a feature is enabled.
   * <p>
   * This is a convenience call for {@link #enabledFor(String, String)} where the group
   * argument is taken from  {@link #defaultGroup()}
   * </p>
   * <p>
   * The client stores feature state locally in a cache, and will periodically refresh
   * the cache record for a key by checking the remote API it's configured to use.
   * </p>
   * <p>
   * The feature is considered enabled if the underlying {@link Feature} has a state equal
   * to  {@link State#on}. The call will return false for the following scenarios:
   * <ol>
   * <li>If the underlying {@link Feature} has a state equal to {@link State#off}.</li>
   * <li>The {@link Feature} is enabled, but is a boolean option type and the boolean option
   * evaluated to false. </li>
   * <li>If the feature does not exist.</li>
   * <li>If there was an internal error. </li>
   * </ol>
   * </p>
   * <p>
   * <b>Note</b> the last point - the client is designed generally to not throw exceptions
   * at the call site, preferring instead to return false and act as if the feature was
   * disabled or non-existent. Generally this means you should strongly prefer to write code
   * that fires when the feature is on, rather than on the basis the feature is off. If you
   * want to throw an exception for a missing feature, use {@link #enabledThrowing}.
   * </p>
   *
   * @param featureKey the feature key defined for the feature
   * @return true if the feature is enabled. Returns false if the feature is not enabled, not known,
   * or there was an internal error.
   * @throws FeatureException if the supplied featureKey is null, or, the default group has not
   * been configured.
   */
  public boolean enabled(String featureKey) {
    throwIfNoDefaultNamespace();
    FeatureException.throwIfNull(featureKey, "Please supply a feature key");

    //noinspection ConstantConditions
    return enabledInner(defaultGroup, featureKey);
  }

  /**
   * Check and see if a feature is enabled.
   * <p>
   * This is a convenience call for {@link #enabledForThrowing(String, String)}
   * where the group argument is taken from  {@link #defaultGroup()}.
   * </p>
   * <p>
   * This is same as {@link #enabled(String)} except it will throw a {@link FeatureException}
   * if the feature does not exist or there is an internal exception.
   * </p>
   *
   * @param featureKey the feature key defined for the feature
   * @return true if the feature is enabled ({@link State#on}). Returns false if the feature
   * is not enabled ({@link State#off}).
   * @throws FeatureException if the supplied featureKey is null, the default group has not been
   * configured, the feature does not exist or there was an internal error.
   */
  public boolean enabledThrowing(String featureKey)
      throws FeatureException {
    throwIfNoDefaultNamespace();
    FeatureException.throwIfNull(featureKey, "Please supply a feature key");

    //noinspection ConstantConditions
    return enabledThrowingInner(defaultGroup, featureKey);
  }

  /**
   * Check and see if a feature is enabled.
   * <p>
   * The client stores feature state locally in a cache, and will periodically refresh
   * the cache record for a key by checking the remote API it's configured to use.
   * </p>
   * <p>
   * The feature is considered enabled if the underlying {@link Feature} has a state equal
   * to  {@link State#on}. The call will return false for the following scenarios:
   * <ol>
   * <li>If the underlying {@link Feature} has a state equal to {@link State#off}.</li>
   * <li>The {@link Feature} is enabled, but is a boolean option type and the boolean option
   * evaluated to false. </li>
   * <li>If the feature does not exist.</li>
   * <li>If there was an internal error. </li>
   * </ol>
   * </p>
   * <p>
   * <b>Note</b> the last point - the client is designed generally to not throw exceptions
   * at the call site, preferring instead to return false and act as if the feature was
   * disabled or non-existent. Generally this means you should strongly prefer to write code
   * that fires when the feature is on, rather than on the basis the feature is off. If you
   * want to throw an exception for a missing feature, use {@link #enabledThrowing}.
   * </p>
   *
   * @param group the group the feature belongs to.
   * @param featureKey the feature key defined for the feature
   * @return true if the feature is enabled. Returns false if the feature is not enabled, not known,
   * or there was an internal error.
   * @throws FeatureException if the supplied featureKey is null, or, the default group has not
   * been configured.
   */
  public boolean enabledFor(String group, String featureKey) {
    FeatureException.throwIfNull(group, "Please supply a defaultGroup");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return enabledInner(group, featureKey);
  }

  /**
   * Check and see if a feature is enabled.
   * <p>
   * This is same as {@link #enabledForThrowing(String, String)} except it will throw a
   * {@link FeatureException} if the feature does not exist or there is an internal exception.
   * </p>
   *
   * @param group the group the feature belongs to.
   * @param featureKey the feature key defined for the feature
   * @return true if the feature is enabled ({@link State#on}). Returns false if the feature
   * is not enabled ({@link State#off}).
   * @throws FeatureException if the supplied featureKey is null, the default group has not been
   * configured, the feature does not exist or there was an internal error.
   */
  public boolean enabledForThrowing(String group, String featureKey) {
    FeatureException.throwIfNull(group, "Please supply a defaultGroup");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return enabledThrowingInner(group, featureKey);
  }

  /**
   * Evaluate a boolean type option.
   *
   * <p>
   * This is a convenience call for {@link #evaluateBoolean(String, String)}
   * where the group argument is taken from  {@link #defaultGroup()}.
   * </p>
   *
   * @param featureKey  the feature key defined for the feature
   * @return true if the feature evaluates to "true". Returns false if the feature
   * evaluates to "false". Always return false if the feature is set to {@link State#off}.
   * @throws FeatureException if the supplied featureKey is null.
   */
  public boolean evaluateBoolean(String featureKey) {
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateBooleanInner(defaultGroup, featureKey);
  }
  /**
   * Evaluate a boolean type option.
   *
   * <p>
   * This will check the feature's bool options and return true or false, factoring
   * in the weights of the options. This means a feature that is set to {@link State#on} can return
   * false due to the weight sampling step. Features that are set to {@link State#off} always
   * evaluate to false.
   * </p>
   *
   * <p>
   *   <b>Warning:</b> passing a non-bool {@link OptionType} to this method will have the feature's
   *   {@link State} evaluated, that is, the result of a non-bool type for this method
   *   is the same as {@link #enabledFor(String, String)}. Also, evaluating a non-existent feature
   *   results in false. If you want to force an error in these cases, call
   *   {@link #evaluateBooleanThrowing(String, String)}.
   * </p>
   *
   * @param group  the group the feature belongs to.
   * @param featureKey  the feature key defined for the feature
   * @return true if the feature evaluates to "true". Returns false if the feature
   * evaluates to "false". Always return false if the feature is set to {@link State#off}.
   * @throws FeatureException if the supplied group or featureKey is null.
   */
  public boolean evaluateBoolean(String group, String featureKey) {
    FeatureException.throwIfNull(group, "Please supply a group");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateBooleanInner(group, featureKey);
  }

  /**
   * Evaluate a boolean type option.
   * <p>
   * This is a convenience call for {@link #evaluateBooleanThrowing(String, String)}
   * where the group argument is taken from  {@link #defaultGroup()}.
   * </p>
   *
   * @param featureKey  the feature key defined for the feature
   * @return true if the feature evaluates to "true". Returns false if the feature
   * evaluates to "false". Always return false if the feature is set to  {@link State#off}.
   * @throws FeatureException if the supplied featureKey is null, the feature does
   * not exist, or is not a boolean option type.
   */
  public boolean evaluateBooleanThrowing(String featureKey) {
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateBooleanInnerThrowing(defaultGroup, featureKey);
  }

  /**
   * Evaluate a boolean type option.
   *
   * <p>
   * This is same as {@link #evaluateBoolean(String, String)} except it will throw a
   * {@link FeatureException} if the feature does not exist or the discovered option
   * was not a bool type.
   * </p>
   *
   * @param group  the group the feature belongs to.
   * @param featureKey  the feature key defined for the feature
   * @return true if the feature evaluates to "true". Returns false if the feature
   * evaluates to "false". Always return false if the feature is set to  {@link State#off}.
   * @throws FeatureException if the supplied group or featureKey is null, the feature does
   * not exist, or is not a boolean option type.
   */
  public boolean evaluateBooleanThrowing(String group, String featureKey) {
    FeatureException.throwIfNull(group, "Please supply a group");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateBooleanInnerThrowing(group, featureKey);
  }

  /**
   * Evaluate a string type option.
   *
   * <p>
   * This is a convenience call for {@link #evaluateString(String, String)}
   * where the group argument is taken from  {@link #defaultGroup()}.
   * </p>
   *
   * @param featureKey  the feature key defined for the feature
   * @return the evaluated option or the control value if the feature is set to {@link State#off}.
   * @throws FeatureException if the supplied featureKey is null.
   */
  public String evaluateString(String featureKey) {
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateInner(defaultGroup, featureKey);
  }

  /**
   * Evaluate a string type option.
   *
   * <p>
   * This will check the feature's string options and return a value, factoring
   * in the weights of the options. Features that are set to {@link State#off} always
   * evaluate to their control value.
   * </p>
   *
   * <p>
   * For non-string option types (eg a bool) their raw string values are returned. Non-existent
   * features will return the empty string (""). If you want to force an error for a missing
   * feature, call {@link #evaluateStringThrowing(String, String)}.
   * </p>
   *
   * @param group  the group the feature belongs to.
   * @param featureKey  the feature key defined for the feature
   * @return the evaluated option or the control value if the feature is set to {@link State#off}.
   * @throws FeatureException if the supplied group or featureKey is null.
   */
  public String evaluateString(String group, String featureKey) {
    FeatureException.throwIfNull(group, "Please supply a group");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    // all option values are raw strings, we can pass through to the general method
    return evaluateInner(group, featureKey);
  }

  /**
   * Evaluate a string type option.
   *
   * <p>
   * This is a convenience call for {@link #evaluateStringThrowing(String, String)}
   * where the group argument is taken from  {@link #defaultGroup()}.
   * </p>
   *
   * @param featureKey  the feature key defined for the feature
   * @return the evaluated option or the control value if the feature is set to {@link State#off}.
   * @throws FeatureException if the supplied featureKey is null, the feature does
   * not exist, or is not a string option type.
   */
  public String evaluateStringThrowing(String featureKey) {
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateThrowing(defaultGroup, featureKey);
  }

  /**
   * Evaluate a string type option.
   *
   * <p>
   * This is same as {@link #evaluateString(String, String)} except it will throw a
   * {@link FeatureException} if the feature does not exist or the option type is not a string.
   * </p>
   *
   * @param group  the group the feature belongs to.
   * @param featureKey  the feature key defined for the feature
   * @return the evaluated option or the control value if the feature is set to {@link State#off}.
   * @throws FeatureException if the supplied group or featureKey is null, the feature does
   * not exist, or is not a string option type.
   */
  public String evaluateStringThrowing(String group, String featureKey) {
    FeatureException.throwIfNull(group, "Please supply a group");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateThrowing(group, featureKey);
  }

  /**
   * Evaluate an option returning one of its values.
   *
   * <p>
   * This is a convenience call for {@link #evaluate(String, String)}
   * where the group argument is taken from  {@link #defaultGroup()}.
   * </p>
   *
   * @param featureKey  the feature key defined for the feature
   * @return the evaluated option, the control value if the feature is set to {@link State#off},
   * or the empty string if the feature is not found.
   * @throws FeatureException if the supplied featureKey is null.
   */
  public String evaluate(String featureKey) {
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateInner(defaultGroup, featureKey);
  }

  /**
   * Evaluate an option returning one of its values.
   *
   * <p>
   * This will check the feature's options and return a value, factoring
   * in the weights of the options. Features that are set to {@link State#off} always
   * evaluate to their control value.
   * </p>
   *
   * <p>
   * For all option types their raw string values are returned. Non-existent
   * features and feature flags will return the empty string (""). If you want to force an error
   * for a missing feature or a flag call, use {@link #evaluateStringThrowing(String, String)}.
   * </p>
   *
   * @param group  the group the feature belongs to.
   * @param featureKey  the feature key defined for the feature
   * @return the evaluated option, the control value if the feature is set to {@link State#off},
   * or the empty string if the feature is not found.
   * @throws FeatureException if the supplied group or featureKey is null.
   */
  public String evaluate(String group, String featureKey) {
    FeatureException.throwIfNull(group, "Please supply a group");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateInner(group, featureKey);
  }

  /**
   * Evaluate an option returning one of its values.
   *
   * <p>
   * This is a convenience call for {@link #evaluateThrowing(String, String)}
   * where the group argument is taken from  {@link #defaultGroup()}.
   * </p>
   *
   * @param featureKey  the feature key defined for the feature
   * @return the evaluated option or the control value if the feature is set to {@link State#off}.
   * @throws FeatureException if the supplied featureKey is null, the feature does
   * not exist, or is a flag type.
   */
  public String evaluateThrowing(String featureKey) {
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateInnerThrowing(defaultGroup, featureKey);
  }

  /**
   * Evaluate an option returning one of its values.
   *
   * <p>
   * This is same as {@link #evaluate(String, String)} except it will throw a
   * {@link FeatureException} if the feature does not exist or the option type is a flag.
   * </p>
   *
   * @param group  the group the feature belongs to.
   * @param featureKey  the feature key defined for the feature
   * @return the evaluated option or the control value if the feature is set to {@link State#off}.
   * @throws FeatureException if the supplied group or featureKey is null, the feature does
   * not exist, or is a flag type.
   */
  public String evaluateThrowing(String group, String featureKey) {
    FeatureException.throwIfNull(group, "Please supply a group");
    FeatureException.throwIfNull(featureKey, "Please supply a featureKey");

    return evaluateInnerThrowing(group, featureKey);
  }

  /**
   * Entry point for the feature management APIs.
   *
   * @return a way to access feature management APIs.
   */
  public Resources resources() {
    return resources;
  }

  /**
   * Stop the feature client.
   * <p>
   * This allows the client to clean up and release any cache related resources such as local
   * storage.
   * </p>
   * <p>
   * Note. The {@link Builder#build()} method places this method in a shutdown hook.
   * </p>
   * <p>
   * <code>
   * &nbsp;&nbsp;Runtime.getRuntime().addShutdownHook(new Thread(client::close));
   * </code>
   * </p>
   *
   * @throws FeatureException or a subclass.
   */
  @SuppressWarnings("WeakerAccess") public void close() throws FeatureException {
    logger.info("op=close, action=releasing_storage_resources");
    featureStore.close();
  }

  @VisibleForTesting
  ServerConfiguration serverConfiguration() {
    return serverConfiguration;
  }

  @VisibleForTesting
  AuthorizationProvider authorizationProvider() {
    return authorizationProvider;
  }

  @VisibleForTesting
  ResourceProvider resourceProvider() {
    return resourceProvider;
  }

  @VisibleForTesting
  ContentSupport contentSupport() {
    return contentSupport;
  }

  @VisibleForTesting
  MetricRegistry metricRegistry() {
    return metricRegistry;
  }

  @VisibleForTesting
  URI baseURI() {
    return baseURI;
  }

  String defaultGroup() {
    return defaultGroup;
  }

  @VisibleForTesting
  boolean localStoreEnabled() {
    return localStoreEnabled;
  }

  private void throwIfNoDefaultNamespace() {
    if (defaultGroup == null) {
      throw new FeatureException(Problem.configProblem("enabled_check_with_no_group",
          "A feature flag check with no group cannot be called without configuring "
              + "a default group first. "
              + "Please set the default group or use the group plus feature key variant."));
    }
  }

  private boolean enabledInner(String group, String featureKey) {
    final FeatureRecord record = featureStore.find(group, featureKey);

    if (record == null) {
      return false;
    }

    return record.enabled(this.namespace);
  }

  private boolean enabledThrowingInner(String group, String featureKey) {
    final FeatureRecord record = featureStore.find(group, featureKey);

    if (record == null) {
      throwNotFound(group, featureKey);
    }

    return record.enabled(this.namespace);
  }

  private boolean evaluateBooleanInner(String group, String featureKey) {
    final FeatureRecord record = featureStore.find(group, featureKey);

    if (record == null) {
      return false;
    }

    return record.evaluateBoolean(this.namespace);
  }

  private boolean evaluateBooleanInnerThrowing(String group, String featureKey) {
    final FeatureRecord record = featureStore.find(group, featureKey);

    if (record == null) {
      throwNotFound(group, featureKey);
    }

    if (!record.feature().getOptions().getOption().equals(OptionType.bool)) {
      throwMismatchedOption(
          group, featureKey, OptionType.bool, record.feature().getOptions().getOption());
    }

    return record.evaluateBoolean(this.namespace);
  }

  private String evaluateInner(String group, String featureKey) {
    final FeatureRecord record = featureStore.find(group, featureKey);

    if (record == null) {
      return "";
    }

    return record.evaluate(this.namespace);
  }

  private String evaluateInnerThrowing(String group, String featureKey) {
    final FeatureRecord record = featureStore.find(group, featureKey);

    if (record == null) {
      throwNotFound(group, featureKey);
    }

    if (record.feature().getOptions().getOption().equals(OptionType.flag)) {
      throwMismatchedOption(
          group, featureKey, OptionType.string, record.feature().getOptions().getOption());
    }

    return record.evaluate(this.namespace);
  }

  private void throwNotFound(String group, String featureKey) {
    throw new FeatureException(
        Problem.noSuchFeature("feature_not_found",
            String.format(
                "feature %s for defaultGroup %s was not found and raising an error was requested",
                featureKey, group)));
  }

  private void throwMismatchedOption(String group, String featureKey,
      OptionType expected, OptionType actual) {
    throw new FeatureException(
        Problem.noSuchFeature("mismatched_option_type", String.format(
            "feature %s in group %s expected option type %s but received %s and raising an error "
                + "was requested", featureKey, group, expected.name(), actual.name())));
  }

  /**
   * Prepares and creates a {@link FeatureClient}.
   */
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

    /**
     * Build a new {@link FeatureClient}.
     * <p>
     * This is the only way to create a client. There's no restriction on the number of clients
     * you can create per JVM process.
     * </p>
     * <p>
     * This method adds the returned {@link FeatureClient#close()} method
     * to a shutdown hook, ie calling this method effectively does the following:
     * </p>
     * <p>
     * <code>
     * &nbsp;&nbsp;Runtime.getRuntime().addShutdownHook(new Thread(client::close));
     * </code>
     * </p>
     *
     * @return a new FeatureClient.
     * @throws FeatureException if the client is considered misconfigured or missing configuration.
     */
    public FeatureClient build() {

      if (serverConfiguration == null) {
        throw new FeatureException(Problem.localProblem(
            "Please supply a configuration", ""));
      }

      if (metricsContextName == null) {
        metricsContextName = "outland.feature";
      }

      if (metricRegistry == null) {
        metricRegistry = new MetricRegistry();
      }

      metricsContext = new MetricsContext(metricsContextName, metricRegistry);

      if (authorizationProvider == null) {
        authorizationProvider = new EmptyAuthorizationProvider();
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

      if (serverConfiguration.localStoreEnabled() && localFeatureStore == null) {
        localFeatureStore = new FeatureStoreRocksDb(metricsContext);
      } else {
        localFeatureStore = new FeatureStoreLocalNone();
      }

      final FeatureClient featureClient = new FeatureClient(this);
      Runtime.getRuntime().addShutdownHook(new Thread(featureClient::close));
      return featureClient;
    }

    /**
     * Supply the {@link ServerConfiguration}.
     *
     * @param serverConfiguration the server configuration
     * @return this
     */
    public Builder serverConfiguration(ServerConfiguration serverConfiguration) {
      this.serverConfiguration = serverConfiguration;
      return this;
    }

    /**
     * Supply the {@link AuthorizationProvider}.
     *
     * @param authorizationProvider the provider
     * @return this
     */
    public Builder authorizationProvider(AuthorizationProvider authorizationProvider) {
      this.authorizationProvider = authorizationProvider;
      return this;
    }

    /**
     * Supply the {@link MetricRegistry}.
     *
     * @param metricRegistry the registry
     * @return this
     */
    public Builder metricRegistry(MetricRegistry metricRegistry) {
      this.metricRegistry = metricRegistry;
      return this;
    }

    /**
     * Supply the prefix used for metrics.
     *
     * <p>
     * You can set this to make the metrics for your client's cluster or service distinct.
     * </p>
     *
     * @param metricsContextName the metrics prefix
     * @return this
     */
    public Builder metricsContextName(String metricsContextName) {
      this.metricsContextName = metricsContextName;
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