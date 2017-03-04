package outland.feature.server.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static outland.feature.server.StructLog.kvp;

public class DynamoDbHealthCheck extends HealthCheck {

  private static final Logger logger = LoggerFactory.getLogger(DynamoDbHealthCheck.class);

  private final DynamoDbProvider provider;

  @Inject
  public DynamoDbHealthCheck(DynamoDbProvider provider) {
    this.provider = provider;
  }

  @Override
  protected Result check() throws Exception {
    Result result;
    final AmazonDynamoDB dynamoDB = provider.get();
    try {
      return Result.healthy(dynamoDB.listTables().getTableNames().toString());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return Result.unhealthy(kvp("ddb_provider", dynamoDB.toString(),
          "err", e.getMessage()));
    }
  }
}
