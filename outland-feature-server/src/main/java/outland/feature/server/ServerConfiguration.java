package outland.feature.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import java.net.URI;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import outland.feature.server.auth.AuthConfiguration;
import outland.feature.server.aws.AwsConfiguration;
import outland.feature.server.aws.DynamoDbModule;
import outland.feature.server.features.FeatureTableConfiguration;
import outland.feature.server.hystrix.HystrixConfiguration;
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
  public URI baseURI;

  @NotNull
  @JsonProperty
  public RedisServersConfiguration redis = new RedisServersConfiguration();

  @NotNull
  @JsonProperty
  public AwsConfiguration aws = new AwsConfiguration();

  @NotNull
  @JsonProperty
  public AuthConfiguration auth = new AuthConfiguration();

  @NotNull
  @JsonProperty
  public FeatureTableConfiguration featureTable = new FeatureTableConfiguration();

  @NotNull
  @JsonProperty
  public HystrixConfiguration redisFeatureCacheHystrix = new HystrixConfiguration();

  @NotNull
  @JsonProperty
  public HystrixConfiguration dynamodbFeatureWriteHystrix = new HystrixConfiguration();

  @NotNull
  @JsonProperty
  public HystrixConfiguration dynamodbFeatureReadHystrix = new HystrixConfiguration();
}