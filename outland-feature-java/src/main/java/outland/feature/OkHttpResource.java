package outland.feature;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.Message;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

class OkHttpResource implements Resource {

  private static final Logger logger = LoggerFactory.getLogger("FeatureClient");

  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String APPLICATION_JSON_CHARSET_UTF8 = "application/json; charset=utf8";
  private static final String APPLICATION_PROTOBUF = "application/protobuf";

  private final OkHttpClient okHttpClient;
  private final ContentSupport contentSupport;
  private final MetricRegistry metricRegistry;
  private long connectTimeout = 0;
  private long readTimeout = 0;
  private volatile boolean hasPerRequestConnectTimeout;
  private volatile boolean hasPerRequestReadTimeout;
  private volatile Response response;

  OkHttpResource(OkHttpClient okHttp, ContentSupport contentSupport, MetricRegistry metrics) {
    this.okHttpClient = okHttp;
    this.contentSupport = contentSupport;
    this.metricRegistry = metrics;
  }

  public OkHttpResource connectTimeout(long timeout, TimeUnit unit) {
    this.connectTimeout = unit.toMillis(timeout);
    hasPerRequestConnectTimeout = true;
    return this;
  }

  public OkHttpResource readTimeout(long timeout, TimeUnit unit) {
    this.readTimeout = unit.toMillis(timeout);
    hasPerRequestReadTimeout = true;
    return this;
  }

  @Override public Response requestThrowing(String method, String url, ResourceOptions options) {

    Observable<Response> observable =
        Observable.defer(() -> Observable.just(requestThrowingInner(method, url, options)));

    return observable.toBlocking().first();
  }

  @Override public Response requestThrowing(String method, String url, ResourceOptions options,
      Message message) {

    RequestContentSupplier supplier = () -> contentSupport.toJsonBytes(message);

    Observable<Response> observable =
        Observable.defer(
            () -> Observable.just(requestThrowingInner(method, url, options, supplier)));

    return observable.toBlocking().first();
  }

  private Response requestThrowingInner(String method, String url, ResourceOptions options) {
    return requestThrowingInner(method, url, options, null);
  }

  private Response requestThrowingInner(String method, String url, ResourceOptions options,
      RequestContentSupplier supplier) {
    return throwIfError(requestInner(method, url, options, supplier));
  }

  private Response requestInner(String method, String url, ResourceOptions options,
      RequestContentSupplier supplier) {
    return executeRequest(prepareBuilder(method, url, options, supplier));
  }

  private Request.Builder prepareBuilder(String method, String url, ResourceOptions options,
      RequestContentSupplier supplier) {
    Request.Builder builder;
    if (supplier != null) {
      RequestBody requestBody =
          RequestBody.create(MediaType.parse(APPLICATION_JSON_CHARSET_UTF8), supplier.content());
      builder = new Request.Builder().url(url).method(method, requestBody);
    } else {
      builder = applyMethodForNoBody(method, url, new Request.Builder().url(url));
    }

    options.headers()
        .entrySet()
        .forEach(e -> builder.addHeader(e.getKey(), e.getValue().toString()));

    applyAuthHeaderIfPresent(options, builder);

    return builder;
  }

  private Request.Builder applyMethodForNoBody(String method, String url, Request.Builder builder) {
    // assume we're not dealing with put/post/patch here as there's no body
    if (Resource.DELETE.equals(method)) {
      return builder.delete();
    } else if (Resource.GET.equals(method)) {
      return builder.get();
    } else if (Resource.HEAD.equals(method)) {
      return builder.head();
    } else {
      logger.warn("unexpected_method_request_with_no_body method={} url={}", method, url);
      return builder.method(method, RequestBody.create(MediaType.parse("text/plain"), ""));
    }
  }

  private void applyAuthHeaderIfPresent(ResourceOptions options, Request.Builder builder) {
    options.supplyToken().ifPresent(t -> builder.header(HEADER_AUTHORIZATION, t.realm() +" "+t.credential()));
  }

  Response executeRequest(Request.Builder builder) {
    try {
      return okHttpCall(builder);
    } catch (IOException e) {
      throw new NetworkException(Problem.networkProblem(e.getMessage(), ""), e);
    }
  }

  private Response okHttpCall(Request.Builder builder) throws IOException {

    if (hasPerRequestReadTimeout || hasPerRequestConnectTimeout) {

      final OkHttpClient.Builder clientBuilder = okHttpClient.newBuilder();
      if (hasPerRequestReadTimeout) {
        clientBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
      }

      if (hasPerRequestConnectTimeout) {
        clientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
      }
      return clientBuilder.build().newCall(builder.build()).execute();
    } else {
      return okHttpClient.newCall(builder.build()).execute();
    }
  }

  private Response throwIfError(Response response) {
    int code = response.code();
    if (code >= 200 && code < 300) {
      return response;
    } else {
      this.response = response;
      return handleError(response);
    }
  }

  private <T> T handleError(Response response) {
    try {
      String raw = response.body().string();

      Problem problem = Optional.ofNullable(contentSupport.fromJson(raw, Problem.class))
          .orElse(Problem.noProblemo("no problem sent back from server", "", response.code()));

      return throwProblem(response.code(), problem);
    } catch (IOException e) {
      throw new FeatureException(Problem.localProblem(e.getMessage(), ""), e);
    }
  }

  private <T> T throwProblem(int code, Problem problem) {
    throw new HttpException(problem);
  }
}
