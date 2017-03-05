package outland.feature.server.aws;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class AwsConfiguration {

  @NotEmpty
  @JsonProperty
  public String environment;

  @JsonProperty
  public String accessKey;

  @JsonProperty
  public String secretKey;

  @NotEmpty
  @JsonProperty
  public String dynamoDbUrl;

  @NotEmpty
  @JsonProperty
  public String signingRegion;
}