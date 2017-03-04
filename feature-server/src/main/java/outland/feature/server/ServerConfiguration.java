package outland.feature.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import outland.feature.server.redis.RedisServersConfiguration;

public class ServerConfiguration extends Configuration {

  @NotEmpty
  @JsonProperty
  public String serviceName;

  @NotEmpty
  @JsonProperty
  public String environment;

  @NotNull
  @JsonProperty
  public RedisServersConfiguration redis = new RedisServersConfiguration();

}
