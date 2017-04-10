package outland.feature;

import com.google.common.base.Strings;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * Configure the client for an API server.
 */
public class ServerConfiguration {

  static final long MAX_CACHE_SIZE = 2048L;
  static final int INITIAL_CAPACITY = 50;
  static final int REFRESH_AFTER_WRITE_S = 8;

  private URI baseURI;
  private String defaultGroup;
  private long connectTimeout = 5_000L;
  private long readTimeout = 3_000L;
  private boolean httpLoggingEnabled;
  private boolean localStoreEnabled = true;
  private String certificatePath;
  private long maxCacheSize = MAX_CACHE_SIZE;
  private int initialCacheSize = INITIAL_CAPACITY;
  private long refreshCacheAfterWrite = REFRESH_AFTER_WRITE_S;

  /**
   * The base API url the client is using.
   */
  public URI baseURI() {
    return baseURI;
  }

  /**
   * Turn on http request/response logging. The http traffic will be logged at info.
   *
   * @param baseURI the URI as a string
   * @return this.
   */
  public ServerConfiguration baseURI(String baseURI) {
    try {
      baseURI(new URI(baseURI));
      return this;
    } catch (URISyntaxException e) {
      throw new FeatureException(
          Problem.configProblem(String.format("Bad base URI [%s]", baseURI), e.getMessage()), e);
    }
  }

  /**
   * Set the required base URI for the server. All other resource URLs are derived from this.
   *
   * @param baseURI the URI
   * @return this.
   */
  public ServerConfiguration baseURI(URI baseURI) {
    this.baseURI = baseURI;
    return this;
  }

  public long connectTimeout() {
    return connectTimeout;
  }

  /**
   * Optionally set the default connect timeout for new connections. If 0, no timeout, otherwise
   * values must be between 1 and {@link Integer#MAX_VALUE}.
   *
   * @param connectTimeout the timeout value.
   * @param unit the time unit
   * @return this.
   */
  public ServerConfiguration connectTimeout(long connectTimeout, TimeUnit unit) {
    this.connectTimeout = unit.toMillis(connectTimeout);
    return this;
  }

  public long readTimeout() {
    return readTimeout;
  }

  /**
   * Optionally set the default read timeout for connections. If 0, no timeout, otherwise
   * values must be between 1 and {@link Integer#MAX_VALUE}.
   *
   * @param readTimeout the timeout value.
   * @param unit the time unit
   * @return this.
   */
  public ServerConfiguration readTimeout(long readTimeout, TimeUnit unit) {
    this.readTimeout = unit.toMillis(readTimeout);
    return this;
  }

  public String certificatePath() {
    return certificatePath;
  }

  /**
   * Supply a path to a set of certificates.
   * <p>If your target server is using a self-signed or other certificate and for some
   * reason you can't install that cert into the system trust store using something like
   * keytool you can supply the cert here. This will cause the client to install any certificates
   * it finds in the supplied directory; files with `*.crt` and `*.pem` extensions are loaded. The
   * path must begin with <code>"file:///"</code> or <code>"classpath:"</code> to indicate whether
   * the certs are loaded from a file directory or  the classpath. If no
   * <code>certificatePath</code> is supplied, the system defaults are used.
   * </p>
   * @param certificatePath the classpath or file path to the certs
   * @return this
   */
  public ServerConfiguration certificatePath(String certificatePath) {
    this.certificatePath = certificatePath;
    return this;
  }

  public String defaultGroup() {
    return defaultGroup;
  }

  /**
   * Configure a default group. All features are housed within a group.
   * <p>
   * This is used for {@link FeatureClient} and  {@link FeatureResource} calls that don't have
   * the group as an argument. Enabling this does not interfere with calls which supply
   * the group argument.
   * </p>
   *
   * @param defaultGroup the default group.
   * @return this.
   */
  public ServerConfiguration defaultGroup(String defaultGroup) {
    /*
     fast fail because a null group is logically ok as it means we expect to be called always
      with the group/feature variants, but setting a null/empty is a bug.
      */
    if (Strings.isNullOrEmpty(defaultGroup)) {
      throw new FeatureException(Problem.configProblem("empty_group",
          "Please configure a non-null and non-empty group."));
    }

    // todo: reject junk once we define the legal group regex on the api

    this.defaultGroup = defaultGroup;
    return this;
  }

  /**
   * Turn on http request/response logging. The http traffic will be logged at info.
   *
   * @return this.
   */
  public ServerConfiguration httpLoggingEnabled(boolean httpLoggingEnabled) {
    this.httpLoggingEnabled = httpLoggingEnabled;
    return this;
  }

  public boolean httpLoggingEnabled() {
    return httpLoggingEnabled;
  }

  /**
   * Enable/disable local persistent storage of features. The default is enabled (true).
   *
   * @param localStoreEnabled enable/disable storage.
   * @return this.
   */
  public ServerConfiguration localStoreEnabled(boolean localStoreEnabled) {
    this.localStoreEnabled = localStoreEnabled;
    return this;
  }

  public long maxCacheSize() {
    return maxCacheSize;
  }

  /**
   * Configure the maximum number of items to cache.
   *
   * @param maxCacheSize the max size of the cache
   * @return this.
   */
  public ServerConfiguration maxCacheSize(long maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
    return this;
  }

  public int initialCacheSize() {
    return initialCacheSize;
  }

  /**
   * Configure the initial cache size before resizing.
   *
   * @param initialCacheSize the initial cache size.
   * @return this.
   */
  public ServerConfiguration initialCacheSize(int initialCacheSize) {
    this.initialCacheSize = initialCacheSize;
    return this;
  }

  public long refreshCacheAfterWriteSeconds() {
    return refreshCacheAfterWrite;
  }

  /**
   * When to refresh the in-memory cache of a feature.
   *
   * @param refreshCacheAfterWrite when to refresh
   * @param unit the time unit
   * @return this
   */
  public ServerConfiguration refreshCacheAfterWrite(long refreshCacheAfterWrite, TimeUnit unit) {
    this.refreshCacheAfterWrite = unit.toSeconds(refreshCacheAfterWrite);
    return this;
  }

  public boolean localStoreEnabled() {
    return localStoreEnabled;
  }

  void validate() {
    FeatureException.throwIfNull(baseURI(),
        "Please provide a base URI for the feature server");
  }
}
