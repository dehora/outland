package outland.feature;

import com.google.common.base.Strings;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class ServerConfiguration {

  static final long MAX_CACHE_SIZE = 2048L;
  static final int INITIAL_CAPACITY = 50;
  static final int REFRESH_AFTER_WRITE_S = 8;

  private URI baseURI;
  private String appKey;
  private long connectTimeout = 5_000L;
  private long readTimeout = 3_000L;
  private boolean httpLoggingEnabled;
  private boolean multiAppEnabled;
  private boolean localStoreEnabled = true;
  private String certificatePath;
  private long maxCacheSize = MAX_CACHE_SIZE;
  private int initialCacheSize = INITIAL_CAPACITY;
  private long refreshCacheAfterWrite = REFRESH_AFTER_WRITE_S;

  @SuppressWarnings("WeakerAccess") public URI baseURI() {
    return baseURI;
  }

  @SuppressWarnings("WeakerAccess") public ServerConfiguration baseURI(String baseURI) {
    try {
      baseURI(new URI(baseURI));
      return this;
    } catch (URISyntaxException e) {
      throw new FeatureException(
          Problem.configProblem(String.format("Bad base URI [%s]", baseURI), e.getMessage()), e);
    }
  }

  @SuppressWarnings("WeakerAccess") public ServerConfiguration baseURI(URI baseURI) {
    this.baseURI = baseURI;
    return this;
  }

  @SuppressWarnings("WeakerAccess") public long connectTimeout() {
    return connectTimeout;
  }

  @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
  public ServerConfiguration connectTimeout(long connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  @SuppressWarnings("WeakerAccess") public long readTimeout() {
    return readTimeout;
  }

  @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
  public ServerConfiguration readTimeout(long readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  @SuppressWarnings("WeakerAccess") public String certificatePath() {
    return certificatePath;
  }

  public ServerConfiguration certificatePath(String certificatePath) {
    this.certificatePath = certificatePath;
    return this;
  }

  public String appKey() {
    return appKey;
  }

  public ServerConfiguration appKey(String appKey) {
    this.appKey = appKey;
    return this;
  }

  public ServerConfiguration httpLoggingEnabled(boolean httpLoggingEnabled) {
    this.httpLoggingEnabled = httpLoggingEnabled;
    return this;
  }

  @SuppressWarnings("WeakerAccess") public boolean httpLoggingEnabled() {
    return httpLoggingEnabled;
  }

  @SuppressWarnings("WeakerAccess")
  public ServerConfiguration multiAppEnabled(boolean multiAppEnabled) {
    this.multiAppEnabled = multiAppEnabled;
    return this;
  }

  @SuppressWarnings("WeakerAccess") public boolean multiAppEnabled() {
    return this.multiAppEnabled;
  }

  @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
  public ServerConfiguration localStoreEnabled(boolean localStoreEnabled) {
    this.localStoreEnabled = localStoreEnabled;
    return this;
  }

  @SuppressWarnings("WeakerAccess")
  public long maxCacheSize() {
    return maxCacheSize;
  }

  public ServerConfiguration maxCacheSize(long maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
    return this;
  }

  @SuppressWarnings("WeakerAccess")
  public int initialCacheSize() {
    return initialCacheSize;
  }

  public ServerConfiguration initialCacheSize(int initialCacheSize) {
    this.initialCacheSize = initialCacheSize;
    return this;
  }

  @SuppressWarnings("WeakerAccess")
  public long refreshCacheAfterWriteSeconds() {
    return refreshCacheAfterWrite;
  }

  public ServerConfiguration refreshCacheAfterWrite(long refreshCacheAfterWrite, TimeUnit unit) {
    this.refreshCacheAfterWrite = unit.toSeconds(refreshCacheAfterWrite);
    return this;
  }

  @SuppressWarnings("WeakerAccess") public boolean localStoreEnabled() {
    return localStoreEnabled;
  }

  void validate() {
    FeatureException.throwIfNull(baseURI(),
        "Please provide a base URI for the feature server");


    if(! multiAppEnabled() && appKey() == null) {
      throw new FeatureException(Problem.configProblem("neither_multi_app_or_single_app_enabled",
          "Please configure the client to have an app id, or use multi app support."));
    }

    if(! multiAppEnabled() && Strings.isNullOrEmpty(appKey())) {

      throw new FeatureException(Problem.configProblem("neither_multi_app_or_single_app_enabled",
          "Please configure the client to have an app id, or use multi app support."));
    }

    if (multiAppEnabled() && appKey() != null) {
      throw new FeatureException(Problem.configProblem("multi_app_and_single_app_enabled",
          "Cannot configure multi app support and a single app at the same time."));
    }

  }
}
