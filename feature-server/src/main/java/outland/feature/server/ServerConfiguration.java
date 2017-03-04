package outland.feature.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class ServerConfiguration extends Configuration {

  @NotEmpty
  @JsonProperty
  public String serviceName;

  @NotEmpty
  @JsonProperty
  public String environment;
}
