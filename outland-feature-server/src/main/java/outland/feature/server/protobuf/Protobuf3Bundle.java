package outland.feature.server.protobuf;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Based on DW's protobuf extensions bundle. Removes the "x-" prefix from the media types and
 * also supports JSON.
 */
public class Protobuf3Bundle implements Bundle {

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
  }

  @Override
  public void run(Environment environment) {
    environment.jersey().register(new Protobuf3MessageBodyProvider());
  }
}
