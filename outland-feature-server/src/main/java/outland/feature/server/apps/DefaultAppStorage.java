package outland.feature.server.apps;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.codahale.metrics.MetricRegistry;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.App;
import outland.feature.server.features.DynamoDbCommand;
import outland.feature.server.features.TableConfiguration;
import outland.feature.server.hystrix.HystrixConfiguration;
import outland.feature.server.protobuf.Protobuf3Support;

import static outland.feature.server.StructLog.kvp;

public class DefaultAppStorage implements AppStorage {

  private static final Logger logger = LoggerFactory.getLogger(DefaultAppStorage.class);

  private final DynamoDB dynamoDB;
  private final String appsTableName;
  private final String appsGraphTableName;
  private final HystrixConfiguration dynamodbAppWriteHystrix;
  private final HystrixConfiguration dynamodbAppGraphWriteHystrix;
  private final HystrixConfiguration dynamodbAppGraphQueryHystrix;
  private final MetricRegistry metrics;
  private final AmazonDynamoDB amazonDynamoDB;

  @Inject
  public DefaultAppStorage(
      AmazonDynamoDB amazonDynamoDB,
      TableConfiguration tableConfiguration,
      @Named("dynamodbAppWriteHystrix") HystrixConfiguration dynamodbAppWriteHystrix,
      @Named("dynamodbAppGraphWriteHystrix") HystrixConfiguration dynamodbAppGraphWriteHystrix,
      @Named("dynamodbAppGraphQueryHystrix") HystrixConfiguration dynamodbAppGraphQueryHystrix,
      MetricRegistry metrics
  ) {
    this.amazonDynamoDB = amazonDynamoDB;
    this.dynamoDB = new DynamoDB(this.amazonDynamoDB);
    this.appsTableName = tableConfiguration.outlandAppsTable;
    this.appsGraphTableName = tableConfiguration.outlandAppGraphTable;
    this.dynamodbAppWriteHystrix = dynamodbAppWriteHystrix;
    this.dynamodbAppGraphWriteHystrix = dynamodbAppGraphWriteHystrix;
    this.dynamodbAppGraphQueryHystrix = dynamodbAppGraphQueryHystrix;
    this.metrics = metrics;
  }

  @Override public Void saveApp(App app) {

    String json = Protobuf3Support.toJsonString(app);

    Item item = new Item()
        .withString("id", app.getId())
        .withString("app_key", app.getKey())
        .withString("name", app.getName())
        .withString("json", json)
        .withString("v", "1")
        .withString("created", app.getCreated())
        .withString("updated", app.getUpdated());

    Table table = dynamoDB.getTable(appsTableName);

    DynamoDbCommand<PutItemOutcome> cmd = new DynamoDbCommand<>("saveApp",
        () -> table.putItem(item),
        () -> {
          throw new RuntimeException("saveApp");
        },
        dynamodbAppWriteHystrix,
        metrics);

    PutItemOutcome outcome = cmd.execute();

    logger.info("{} /dynamodb_put_item_result=[{}]",
        kvp("op", "saveApp",
            "app_id", app.getId(),
            "app_key", app.getKey(),
            "result", "ok"),
        outcome.getPutItemResult().toString());

    return null;
  }

  @Override public Void saveRelation(App app, String relationHashKey, String relationRangeKey) {

    Item item = new Item()
        .withString(AppStorage.SUBJECT_KEY, relationHashKey)
        .withString(AppStorage.OBJECT_RELATION_KEY, relationRangeKey);

    Table table = dynamoDB.getTable(appsGraphTableName);

    DynamoDbCommand<PutItemOutcome> cmd = new DynamoDbCommand<>("saveRelation",
        () -> table.putItem(item),
        () -> {
          throw new RuntimeException("saveRelation");
        },
        dynamodbAppGraphWriteHystrix,
        metrics);

    PutItemOutcome outcome = cmd.execute();

    logger.info("{} /dynamodb_put_item_result=[{}]",
        kvp("op", "saveRelation",
            "app_id", app.getKey(),
            "hash_key", relationHashKey,
            "range_key", relationRangeKey,
            "result", "ok"),

        outcome.getPutItemResult().toString());

    return null;
  }

  @Override public boolean queryRelationExists(String relationHashKey, String relationRangeKey) {

    Table table = dynamoDB.getTable(this.appsGraphTableName);

    QuerySpec querySpec = new QuerySpec()
        .withKeyConditionExpression("subject = :k_subject and object_relation = :k_object_relation")
        .withValueMap(new ValueMap()
            .withString(":k_subject", relationHashKey)
            .withString(":k_object_relation", relationRangeKey)
        )
        .withMaxResultSize(1)
        .withConsistentRead(true);

    DynamoDbCommand<ItemCollection<QueryOutcome>> cmd = new DynamoDbCommand<>("queryRelation",
        () -> queryTable(table, querySpec),
        () -> {
          throw new RuntimeException("queryRelation");
        },
        dynamodbAppGraphQueryHystrix,
        metrics);

    // can't use getLastLowLevelResult directly; it's false unless the outcome is iterated first :|
    return cmd.execute().iterator().hasNext();
  }

  private ItemCollection<QueryOutcome> queryTable(Table table, QuerySpec querySpec) {
    return table.query(querySpec);
  }
}
