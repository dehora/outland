package outland.feature.server.apps;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
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
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;
import outland.feature.server.auth.AppMember;

public class AppAuthServiceRemoteServer implements AppAuthService {

  private final OkHttpClient client;
  private final URI tokenLookupUri;
  private final Gson gson = new Gson();

  @Inject
  public AppAuthServiceRemoteServer(
      @Named("OAuthServiceClient") OkHttpClient client,
      @Named("OAuthServiceTokenLookupUri") URI tokenLookupUri) {
    this.client = client;
    this.tokenLookupUri = tokenLookupUri;
  }

  @Override public Optional<AppMember> authenticate(String credentials, String tokenType) {

    // plan-b style tokeninfo fetch; send the rcvd bearer token onto the oauth server

    Request request = new Request.Builder()
        .url(tokenLookupUri.toASCIIString())
        .header("Authorization", tokenType + " " + credentials)
        .get()
        .build();

    final Response response = callAuthServer(request);
    if (response.code() != 200) {
      return Optional.empty();
    }

    return Optional.ofNullable(planBToAppMember(toTokenInfo(response, gson)));
  }

  private AppMember planBToAppMember(Map<String, Object> tokenInfo) {

    if(tokenInfo == null) {
      return null;
    }

    String type = "service";
    final Object realm = tokenInfo.get("realm");
    if(realm != null) {
      if(((String) realm).contains("employees"))  {
        type = "owner";
      }
    }

    final List<String> scopes = Lists.newArrayList();
    final Object scope = tokenInfo.get("scope");
    if(scope != null) {
      scopes.addAll((List<String>)tokenInfo.get("scope"));
    }

    final String uid = (String)tokenInfo.get("uid");
    final String accessToken = (String)tokenInfo.get("access_token");
    // gson artefact; converts ints on the wire to double
    final Long expires = Math.round((Double)tokenInfo.get("expires_in"));

    return new AppMember(type,
        uid, scopes,
        accessToken,
        expires
    );
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
}
