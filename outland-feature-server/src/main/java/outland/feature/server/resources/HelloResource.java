package outland.feature.server.resources;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import outland.feature.server.ServerConfiguration;

@Resource
@Path("/hello")
@Consumes(MediaType.WILDCARD)
public class HelloResource {

  private final ServerConfiguration serverConfiguration;

  @Inject
  public HelloResource(ServerConfiguration serverConfiguration) {
    this.serverConfiguration = serverConfiguration;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response ok() {
    return Response.ok("Hello! Welcome to " + serverConfiguration.serviceName).build();
  }
}
