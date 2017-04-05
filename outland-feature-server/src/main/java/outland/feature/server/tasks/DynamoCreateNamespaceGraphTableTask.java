package outland.feature.server.tasks;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.server.apps.NamespaceStorage;
import outland.feature.server.features.TableConfiguration;

public class DynamoCreateNamespaceGraphTableTask extends Task {

  private static final Logger logger =
      LoggerFactory.getLogger(DynamoCreateNamespaceGraphTableTask.class);

  private final AmazonDynamoDB dynamoDB;
  private final TableConfiguration tableConfiguration;

  @Inject
  public DynamoCreateNamespaceGraphTableTask(
      AmazonDynamoDB dynamoDB,
      TableConfiguration tableConfiguration
  ) {
    super("DynamoCreateNamespaceGraphTableTask");
    this.dynamoDB = dynamoDB;
    this.tableConfiguration = tableConfiguration;
  }

  @Override public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output)
      throws Exception {
    createAppGraphTable(tableConfiguration.outlandAppGraphTable);
  }

  private void createAppGraphTable(String tableName) {

    final String subjectKey = NamespaceStorage.SUBJECT_KEY;
    final String objectRelationKey = NamespaceStorage.OBJECT_RELATION_KEY;

    final AttributeDefinition subjectDefn =
        new AttributeDefinition().withAttributeName(subjectKey)
            .withAttributeType(ScalarAttributeType.S);

    final AttributeDefinition objectRelationDefn =
        new AttributeDefinition().withAttributeName(objectRelationKey)
            .withAttributeType(ScalarAttributeType.S);

    final List<AttributeDefinition> attributeDefinitions = Lists.newArrayList(
        subjectDefn,
        objectRelationDefn
    );

    final KeySchemaElement hashKey =
        new KeySchemaElement().withAttributeName(subjectKey).withKeyType(KeyType.HASH);
    final KeySchemaElement rangeKey =
        new KeySchemaElement().withAttributeName(objectRelationKey).withKeyType(KeyType.RANGE);

    final List<KeySchemaElement> keySchema = Lists.newArrayList(
        hashKey,
        rangeKey
    );

    final ProvisionedThroughput provisionedThroughput =
        new ProvisionedThroughput().withReadCapacityUnits(10L).withWriteCapacityUnits(10L);

    final CreateTableRequest createTableRequest =
        new CreateTableRequest()
            .withTableName(tableName)
            .withKeySchema(keySchema)
            .withAttributeDefinitions(attributeDefinitions)
            .withProvisionedThroughput(provisionedThroughput);

    final TableDescription tableDescription =
        dynamoDB.createTable(createTableRequest).getTableDescription();

    logger.info("created table: {}", tableDescription);

    final TableDescription description =
        dynamoDB.describeTable(new DescribeTableRequest().withTableName(tableName)).getTable();

    logger.info("reread table description: " + description);
  }
}
