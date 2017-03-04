package outland.feature.server.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.google.inject.AbstractModule;

public class DynamoDbModule extends AbstractModule {

  private final AwsConfiguration configuration;

  public DynamoDbModule(AwsConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    bind(AwsConfiguration.class).toInstance(configuration);
    bind(AmazonDynamoDB.class).toProvider(DynamoDbProvider.class).asEagerSingleton();
    bind(DynamoDbHealthCheck.class);
  }
}
