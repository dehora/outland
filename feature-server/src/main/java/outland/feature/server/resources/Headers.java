package outland.feature.server.resources;

import javax.ws.rs.core.Response;

public class Headers {

  public Headers() {
  }

  Response.ResponseBuilder enrich(Response.ResponseBuilder response, long start) {
    return response.header("Trace-Response-Time",
        "time=" + (System.currentTimeMillis() - start) + ", unit=millis");
  }
}
