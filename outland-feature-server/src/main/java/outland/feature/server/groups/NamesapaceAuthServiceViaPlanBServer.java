package outland.feature.server.groups;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;
import outland.feature.server.auth.AuthPrincipal;

import static outland.feature.server.StructLog.kvp;

public class NamesapaceAuthServiceViaPlanBServer implements NamesapaceAuthService {

  private static final Logger logger =
      LoggerFactory.getLogger(NamesapaceAuthServiceViaPlanBServer.class);
  private static final Map<String, String> PLANB_REALMS = Maps.newHashMap();
  private static final String PLANB_FIELD_SCOPE = "scope";
  private static final String PLAN_FIELD_IDENTITY = "uid";
  private static final String PLANB_FIELD_REALM = "realm";

  static {
    PLANB_REALMS.put("/employees", GroupService.OWNER);
    PLANB_REALMS.put("/services", GroupService.SERVICE);
  }

  private final OkHttpClient client;
  private final URI tokenLookupUri;
  @Inject
  public NamesapaceAuthServiceViaPlanBServer(
      @Named("OAuthServiceClient") OkHttpClient client,
      @Named("OAuthServiceTokenLookupUri") URI tokenLookupUri) {
    this.client = client;
    this.tokenLookupUri = tokenLookupUri;
  }

  private static Gson gson() {
    return GsonHolder.INSTANCE;
  }

  @Override public Optional<AuthPrincipal> authenticate(String credentials, String tokenType) {
    return readFromPlanBResult(credentials, tokenType);
  }

  private Optional<AuthPrincipal> readFromPlanBResult(String credentials, String tokenType) {

    // plan-b style tokeninfo fetch; send the rcvd bearer token onto the oauth server
    Request request = new Request.Builder()
        .url(tokenLookupUri.toASCIIString())
        .header("Authorization", tokenType + " " + credentials)
        .get()
        .build();

    Response response = callAuthServer(request);

    logger.info("op=remote_auth,token_type={},status={}", tokenType, response.code());

    if (response.code() > 299 && response.code() < 309) {
      logger.info(
          kvp("op", "upstream_oauth_redirect",
              "status", response.code(),
              "redirect_uri", response.header("Location")
          ));

      request = new Request.Builder()
          .url(response.header("Location"))
          .header("Authorization", tokenType + " " + credentials)
          .get()
          .build();

      response = callAuthServer(request);
    }

    if (response.code() != 200) {
      return Optional.empty();
    }

    return Optional.ofNullable(planBToAppMember(toTokenInfo(response, gson())));
  }

  private AuthPrincipal planBToAppMember(Map<String, Object> tokenInfo) {

    if (tokenInfo == null) {
      return null;
    }

    String type = GroupService.SERVICE;

    final Object realm = tokenInfo.get(PLANB_FIELD_REALM);
    if (realm != null) {
      final String realmS = (String) realm;
      if (PLANB_REALMS.containsKey(realmS)) {
        type = PLANB_REALMS.get(realmS);
      }
    }

    final List<String> scopes = Lists.newArrayList();
    final Object scope = tokenInfo.get(PLANB_FIELD_SCOPE);
    if (scope != null) {
      try {
        //noinspection unchecked
        scopes.addAll((List<String>) tokenInfo.get(PLANB_FIELD_SCOPE));
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      }
    }

    // this is currently a username or a service name; in outland we also accept emails
    final String identity = (String) tokenInfo.get(PLAN_FIELD_IDENTITY);

    return new AuthPrincipal(type, identity, scopes);
  }

  private Map<String, Object> toTokenInfo(Response response, Gson gson) {
    try {
      return gson.fromJson(response.body().string(), new TypeToken<Map<String, Object>>() {
      }.getType());
    } catch (IOException e) {
      throw new ServiceException(
          Problem.clientProblem("upstream_auth_parse_failed", e.getMessage(), 400));
    }
  }

  private Response callAuthServer(Request request) {
    try {
      return client.newCall(request).execute();
    } catch (IOException e) {
      throw new ServiceException(
          Problem.serverProblem("upstream_auth_failed", e.getMessage(), 503));
    }
  }

  private static class GsonHolder {
    private static final Gson INSTANCE = new GsonBuilder()
        .setPrettyPrinting()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();
  }
}
