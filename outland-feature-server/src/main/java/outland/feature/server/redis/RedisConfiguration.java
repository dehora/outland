package outland.feature.server.redis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.hibernate.validator.constraints.NotEmpty;

public class RedisConfiguration {

  private static final int DEFAULT_PORT = 6379;
  private static final int DEFAULT_CONNECTIONS = 8;
  private static final String DEFAULT_HOST = "localhost";

  @NotEmpty
  @JsonProperty
  public String name;

  @NotEmpty
  @JsonProperty
  public String host = DEFAULT_HOST;

  @JsonProperty
  @Min(1)
  @Max(65535)
  public Integer port = DEFAULT_PORT;

  @JsonProperty
  @Min(8)
  @Max(128)
  public Integer maxConnections = DEFAULT_CONNECTIONS;

  @Override public int hashCode() {
    return java.util.Objects.hash(name, host, port, maxConnections);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RedisConfiguration that = (RedisConfiguration) o;
    return java.util.Objects.equals(name, that.name) &&
        java.util.Objects.equals(host, that.host) &&
        java.util.Objects.equals(port, that.port) &&
        java.util.Objects.equals(maxConnections, that.maxConnections);
  }

  @Override public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("host", host)
        .add("port", port)
        .add("maxConnections", maxConnections)
        .toString();
  }
}
