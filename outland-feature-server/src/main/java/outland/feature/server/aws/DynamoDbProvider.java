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
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

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

      AwsClientBuilder.EndpointConfiguration endpointConfiguration =
          new AwsClientBuilder.EndpointConfiguration(configuration.dynamoDbUrl,
              configuration.signingRegion);

      final AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder
          .standard()
          .withClientConfiguration(clientConfiguration)
          .withEndpointConfiguration(endpointConfiguration);

      if ("iam".equals(configuration.authMode)) {
        // bypass the chain and use InstanceProfileCredentials directly
        builder.withCredentials(InstanceProfileCredentialsProvider.getInstance());
        amazonDynamoDB = builder.build();
        logger.info(kvp("op", "configure_ddb", "msg", "using_instance_profile"));
      } else if ("chain".equals(configuration.authMode)) {
        // delegates withCredentials to DefaultAWSCredentialsProviderChain
        logger.info(kvp("op", "configure_ddb", "msg", "using_credentials_chain"));
        amazonDynamoDB = builder.build();
      } else if ("local".equals(configuration.authMode)) {
        logger.info(kvp("op", "configure_ddb", "msg", "using_local_with_envar_or_fake_creds"));
        /*
         convenience for working with ddb local without having to set the
         envars through docker every time;

         local ddb just needs any pair of values set so echo back the envars as the envar values
         to keep it happy with fake creds, in case they are not set.
          */
        final Map<String, String> envMap = System.getenv();
        final String accessKeyEnv = "AWS_ACCESS_KEY_ID";
        final String secretKeyEnv = "AWS_SECRET_ACCESS_KEY";
        final String key = Optional.ofNullable(envMap.get(accessKeyEnv)).orElse(accessKeyEnv);
        final String secret = Optional.ofNullable(envMap.get(secretKeyEnv)).orElse(secretKeyEnv);
        amazonDynamoDB =
            new AmazonDynamoDBClient(new BasicAWSCredentials(key, secret), clientConfiguration);
        amazonDynamoDB.setEndpoint(configuration.dynamoDbUrl);
      } else {
        throw new ServiceException(Problem.argProblem("unknown_aws_auth_mode", ""));
      }
    }
  }

  @Override
  public AmazonDynamoDB get() {
    return amazonDynamoDB;
  }
}
