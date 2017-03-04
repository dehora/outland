package outland.feature.server.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.google.inject.Provider;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static outland.feature.server.StructLog.kvp;

@Singleton
public class DynamoDbProvider implements Provider<AmazonDynamoDB> {

  private static final Logger logger = LoggerFactory.getLogger(DynamoDbProvider.class);
  private static final int MAX_CONNECTIONS = 64;
  private static final int MAX_ERROR_RETRY = 2;
  private static AmazonDynamoDB amazonDynamoDB;

  @Inject
  public DynamoDbProvider(AwsConfiguration configuration) {
    initialize(configuration);
  }

  private void initialize(AwsConfiguration configuration) {
    if (amazonDynamoDB == null) {
      final ClientConfiguration clientConfiguration = new ClientConfiguration();
      clientConfiguration.withMaxConnections(MAX_CONNECTIONS);
      clientConfiguration.withMaxErrorRetry(MAX_ERROR_RETRY);
      clientConfiguration.withRetryPolicy(
          PredefinedRetryPolicies.getDynamoDBDefaultRetryPolicyWithCustomMaxRetries(
              MAX_ERROR_RETRY));

      logger.info(kvp("op", "configure_ddb",
          "endpoint", configuration.dynamoDbUrl,
          "region", configuration.signingRegion
      ));

      if ("production".equals(configuration.environment)) {
        logger.info(kvp("op", "configure_ddb",
            "environment", configuration.environment,
            "auth", "iam"
        ));

        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
            new AwsClientBuilder.EndpointConfiguration(configuration.dynamoDbUrl,
                configuration.signingRegion);
        amazonDynamoDB = AmazonDynamoDBClientBuilder
            .standard()
            .withClientConfiguration(clientConfiguration)
            .withEndpointConfiguration(endpointConfiguration)
            .withCredentials(InstanceProfileCredentialsProvider.getInstance())
            .build();
      } else {
        logger.info(kvp("op", "configure_ddb",
            "environment", configuration.environment,
            "auth", "key_and_secret"
        ));

        // check the env or fallback to conf
        final Map<String, String> envMap = System.getenv();
        final String accessKeyEnv = configuration.accessKey;
        final String secretKeyEnv = configuration.secretKey;
        final String key = Optional.ofNullable(envMap.get(accessKeyEnv)).orElse(accessKeyEnv);
        final String secret = Optional.ofNullable(envMap.get(secretKeyEnv)).orElse(secretKeyEnv);
        amazonDynamoDB =
            new AmazonDynamoDBClient(new BasicAWSCredentials(key, secret), clientConfiguration);
        amazonDynamoDB.setEndpoint(configuration.dynamoDbUrl);
      }
    }
  }

  @Override
  public AmazonDynamoDB get() {
    return amazonDynamoDB;
  }
}
