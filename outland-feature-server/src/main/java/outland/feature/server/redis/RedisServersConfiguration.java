package outland.feature.server.redis;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotNull;

public class RedisServersConfiguration {

  @NotNull
  @JsonProperty("servers")
  public List<RedisConfiguration> servers;
}
