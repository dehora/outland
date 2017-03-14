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

public class DynamoCreateFeatureTableTask extends Task {

  private static final Logger logger = LoggerFactory.getLogger(DynamoCreateFeatureTableTask.class);

  private final AmazonDynamoDB amazonDynamoDB;
  private final String tableName;

  @Inject
  public DynamoCreateFeatureTableTask(
      AmazonDynamoDB amazonDynamoDB,
      TableConfiguration tableConfiguration
  ) {
    super("DynamoCreateFeatureTableTask");
    this.amazonDynamoDB = amazonDynamoDB;
    this.tableName = tableConfiguration.outlandFeaturesTable;
  }

  @Override public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output)
      throws Exception {
    createTable();
  }

  public void createTable() {

    final AttributeDefinition appId =
        new AttributeDefinition().withAttributeName("app_id").withAttributeType(
            ScalarAttributeType.S);

    final AttributeDefinition featureKey =
        new AttributeDefinition().withAttributeName("feature_key")
            .withAttributeType(ScalarAttributeType.S);

    final AttributeDefinition id =
        new AttributeDefinition().withAttributeName("id")
            .withAttributeType(ScalarAttributeType.S);

    final ArrayList<AttributeDefinition>
        tableAttributeDefinitions = Lists.newArrayList(appId, featureKey, id);
    final ArrayList<KeySchemaElement> tableKeySchema = Lists.newArrayList();

    tableKeySchema.add(
        new KeySchemaElement().withAttributeName("app_id").withKeyType(KeyType.HASH));
    tableKeySchema.add(
        new KeySchemaElement().withAttributeName("feature_key").withKeyType(KeyType.RANGE));

    final ProvisionedThroughput tableProvisionedThroughput =
        new ProvisionedThroughput()
            .withReadCapacityUnits(10L)
            .withWriteCapacityUnits(10L);

    final ArrayList<KeySchemaElement> indexKeySchema = new ArrayList<>();
    indexKeySchema.add(
        new KeySchemaElement().withAttributeName("app_id").withKeyType(KeyType.HASH));
    indexKeySchema.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.RANGE));

    final Projection projection = new Projection().withProjectionType(ProjectionType.INCLUDE);
    final ArrayList<String> indexColumns = new ArrayList<>();
    indexColumns.add("json");
    indexColumns.add("v");
    projection.setNonKeyAttributes(indexColumns);

    final LocalSecondaryIndex localSecondaryIndex = new LocalSecondaryIndex()
        .withIndexName("feature_by_app_id_and_id_lsi_idx")
        .withKeySchema(indexKeySchema)
        .withProjection(projection);

    final ArrayList<LocalSecondaryIndex> secondaryIndices = new ArrayList<>();
    secondaryIndices.add(localSecondaryIndex);

    final CreateTableRequest createTableRequest =
        new CreateTableRequest()
            .withTableName(tableName)
            .withKeySchema(tableKeySchema)
            .withAttributeDefinitions(tableAttributeDefinitions)
            .withProvisionedThroughput(tableProvisionedThroughput)
            .withLocalSecondaryIndexes(secondaryIndices);

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
