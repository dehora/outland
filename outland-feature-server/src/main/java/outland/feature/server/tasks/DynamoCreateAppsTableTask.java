package outland.feature.server.tasks;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.server.features.TableConfiguration;

public class DynamoCreateAppsTableTask extends Task {

  private static final Logger logger = LoggerFactory.getLogger(DynamoCreateAppsTableTask.class);

  private final AmazonDynamoDB amazonDynamoDB;
  private final TableConfiguration tableConfiguration;

  @Inject
  public DynamoCreateAppsTableTask(
      AmazonDynamoDB amazonDynamoDB,
      TableConfiguration tableConfiguration
  ) {
    super("DynamoCreateAppsTableTask");
    this.amazonDynamoDB = amazonDynamoDB;
    this.tableConfiguration = tableConfiguration;
  }

  @Override public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output)
      throws Exception {
    createAppsTable(tableConfiguration.outlandAppsTable);
  }

  private void createAppsTable(String tableName) {
    final AttributeDefinition appKey =
        new AttributeDefinition().withAttributeName("app_key").withAttributeType(
            ScalarAttributeType.S);

    final ArrayList<AttributeDefinition>
        tableAttributeDefinitions = Lists.newArrayList(appKey);

    final ArrayList<KeySchemaElement> tableKeySchema = Lists.newArrayList();
    tableKeySchema.add(
        new KeySchemaElement().withAttributeName("app_key").withKeyType(KeyType.HASH));

    final ProvisionedThroughput tableProvisionedThroughput =
        new ProvisionedThroughput()
            .withReadCapacityUnits(10L)
            .withWriteCapacityUnits(10L);

    final CreateTableRequest createTableRequest =
        new CreateTableRequest()
            .withTableName(tableName)
            .withKeySchema(tableKeySchema)
            .withAttributeDefinitions(tableAttributeDefinitions)
            .withProvisionedThroughput(tableProvisionedThroughput)
        ;

    final TableDescription tableDescription =
        amazonDynamoDB.createTable(createTableRequest).getTableDescription();

    logger.info("created_table {}", tableDescription);

    final DescribeTableRequest describeTableRequest =
        new DescribeTableRequest().withTableName(tableName);

    final TableDescription description =
        amazonDynamoDB.describeTable(describeTableRequest).getTable();

    logger.info("table_description: " + description);
  }
}
