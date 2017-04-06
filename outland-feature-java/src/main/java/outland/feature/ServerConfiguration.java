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
  private String defaultNamespace;
  private long connectTimeout = 5_000L;
  private long readTimeout = 3_000L;
  private boolean httpLoggingEnabled;
  private boolean localStoreEnabled = true;
  private String certificatePath;
  private long maxCacheSize = MAX_CACHE_SIZE;
  private int initialCacheSize = INITIAL_CAPACITY;
  private long refreshCacheAfterWrite = REFRESH_AFTER_WRITE_S;

  public URI baseURI() {
    return baseURI;
  }

  public ServerConfiguration baseURI(String baseURI) {
    try {
      baseURI(new URI(baseURI));
      return this;
    } catch (URISyntaxException e) {
      throw new FeatureException(
          Problem.configProblem(String.format("Bad base URI [%s]", baseURI), e.getMessage()), e);
    }
  }

  public ServerConfiguration baseURI(URI baseURI) {
    this.baseURI = baseURI;
    return this;
  }

  public long connectTimeout() {
    return connectTimeout;
  }

  public ServerConfiguration connectTimeout(long connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public long readTimeout() {
    return readTimeout;
  }

  public ServerConfiguration readTimeout(long readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  public String certificatePath() {
    return certificatePath;
  }

  public ServerConfiguration certificatePath(String certificatePath) {
    this.certificatePath = certificatePath;
    return this;
  }

  public String defaultNamespace() {
    return defaultNamespace;
  }

  public ServerConfiguration defaultNamespace(String defaultNamespace) {
    /*
     fast fail because a null namespace is logically ok as it means we expect to be called always
      with the namespace/feature variants, but setting a null/empty is a bug.
      */
    if(Strings.isNullOrEmpty(defaultNamespace)) {
      throw new FeatureException(Problem.configProblem("empty_namespace",
          "Please configure a non-null and non-empty namespace."));
    }

    // todo: reject junk once we define the legal namespace regex on the api

    this.defaultNamespace = defaultNamespace;
    return this;
  }

  public ServerConfiguration httpLoggingEnabled(boolean httpLoggingEnabled) {
    this.httpLoggingEnabled = httpLoggingEnabled;
    return this;
  }

  public boolean httpLoggingEnabled() {
    return httpLoggingEnabled;
  }

  public ServerConfiguration localStoreEnabled(boolean localStoreEnabled) {
    this.localStoreEnabled = localStoreEnabled;
    return this;
  }

  public long maxCacheSize() {
    return maxCacheSize;
  }

  public ServerConfiguration maxCacheSize(long maxCacheSize) {
    this.maxCacheSize = maxCacheSize;
    return this;
  }

  public int initialCacheSize() {
    return initialCacheSize;
  }

  public ServerConfiguration initialCacheSize(int initialCacheSize) {
    this.initialCacheSize = initialCacheSize;
    return this;
  }

  public long refreshCacheAfterWriteSeconds() {
    return refreshCacheAfterWrite;
  }

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
