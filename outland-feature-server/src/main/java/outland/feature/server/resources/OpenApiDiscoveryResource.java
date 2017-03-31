package outland.feature.server.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import outland.feature.server.ServerConfiguration;

@Resource
@Path(".well-known")
public class OpenApiDiscoveryResource {

  public static final int MAX_AGE = 60;
  private final Gson gson = new Gson();
  private final URI baseURI;
  private final Date lastModified = new Date();
  private String discoveryJson;
  private EntityTag discoveryEtag;
  private String schemaYaml;
  private EntityTag schemaEtag;

  @Inject
  public OpenApiDiscoveryResource(ServerConfiguration serverConfiguration) throws Exception {
    baseURI = serverConfiguration.baseURI;
    prepareSchemaEntry(Resources.getResource("outland-oai.yaml"));
    prepareDiscoveryEntity();
  }

  @GET
  @Path("outland.yaml")
  @Produces("text/yaml;charset=utf-8")
  @Timed(name = "schema")
  public Response schema(@Context Request request) throws Exception {
    return cacheAwareResponse(request, schemaYaml, schemaEtag, lastModified, MAX_AGE);
  }

  @GET
  @Path("schema-discovery")
  @Produces(MediaType.APPLICATION_JSON)
  @Timed(name = "discovery")
  public Response discovery(@Context Request request) throws Exception {
    return cacheAwareResponse(request, discoveryJson, discoveryEtag, this.lastModified, MAX_AGE);
  }

  private Response cacheAwareResponse(
      Request request, String entity, EntityTag etag, Date lastModified, int maxAge
  ) {

    final Response.ResponseBuilder builderLastMod = request.evaluatePreconditions(lastModified);
    if (builderLastMod != null) {
      return builderLastMod.build();
    }

    final Response.ResponseBuilder builderEtag = request.evaluatePreconditions(etag);
    if (builderEtag != null) {
      return builderEtag.build();
    }

    final CacheControl cc = new CacheControl();
    cc.setMaxAge(maxAge);

    return Response.ok(entity)
        .tag(etag)
        .lastModified(lastModified)
        .cacheControl(cc)
        .build();
  }

  private void prepareSchemaEntry(URL resource) throws IOException, NoSuchAlgorithmException {
    this.schemaYaml = Resources.toString(resource, Charsets.UTF_8);
    this.schemaEtag = new EntityTag(generateEtag(schemaYaml));
  }

  private void prepareDiscoveryEntity() throws Exception {
    if (discoveryJson == null) {
      final String schemaUrl =
          UriBuilder.fromUri(baseURI)
              .path(".well-known/outland.yaml")
              .build()
              .toASCIIString();
      Map<String, String> discovery = Maps.newHashMap();
      discovery.put("schema_url", schemaUrl);
      discovery.put("schema_type", "swagger-2.0");
      discoveryJson = gson.toJson(discovery);
      discoveryEtag = new EntityTag(generateEtag(discoveryJson));
    }
  }

  private String generateEtag(String thing) throws NoSuchAlgorithmException {
    final MessageDigest digest = MessageDigest.getInstance("MD5");
    final byte[] hash = digest.digest(thing.getBytes(Charsets.UTF_8));
    return BaseEncoding.base32Hex().encode(hash);
  }
}
