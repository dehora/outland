package outland.feature.server.groups;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.codahale.metrics.MetricRegistry;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Group;
import outland.feature.server.app.GroupSupport;
import outland.feature.server.features.DynamoDbCommand;
import outland.feature.server.features.TableConfiguration;
import outland.feature.server.hystrix.HystrixConfiguration;
import outland.feature.server.protobuf.Protobuf3Support;

import static outland.feature.server.StructLog.kvp;

public class DefaultGroupStorage implements GroupStorage {

  private static final Logger logger = LoggerFactory.getLogger(DefaultGroupStorage.class);
  public static final String HASH_KEY = "group_key";

  private final DynamoDB dynamoDB;
  private final String groupTableName;
  private final String groupGraphTableName;
  private final HystrixConfiguration dynamodbNamespaceWriteHystrix;
  private final HystrixConfiguration dynamodbNamespaceGraphWriteHystrix;
  private final HystrixConfiguration dynamodbNamespaceGraphQueryHystrix;
  private final MetricRegistry metrics;
  private final AmazonDynamoDB amazonDynamoDB;

  @Inject
  public DefaultGroupStorage(
      AmazonDynamoDB amazonDynamoDB,
      TableConfiguration tableConfiguration,
      @Named("dynamodbNamespaceWriteHystrix") HystrixConfiguration dynamodbNamespaceWriteHystrix,
      @Named("dynamodbNamespaceGraphWriteHystrix") HystrixConfiguration dynamodbNamespaceGraphWriteHystrix,
      @Named("dynamodbNamespaceGraphQueryHystrix") HystrixConfiguration dynamodbNamespaceGraphQueryHystrix,
      MetricRegistry metrics
  ) {
    this.amazonDynamoDB = amazonDynamoDB;
    this.dynamoDB = new DynamoDB(this.amazonDynamoDB);
    this.groupTableName = tableConfiguration.outlandGroupsTable;
    this.groupGraphTableName = tableConfiguration.outlandAppGraphTable;
    this.dynamodbNamespaceWriteHystrix = dynamodbNamespaceWriteHystrix;
    this.dynamodbNamespaceGraphWriteHystrix = dynamodbNamespaceGraphWriteHystrix;
    this.dynamodbNamespaceGraphQueryHystrix = dynamodbNamespaceGraphQueryHystrix;
    this.metrics = metrics;
  }

  @Override public Void create(Group group) {
    Item item = preparePutItem(group);

    PutItemSpec putItemSpec = new PutItemSpec()
        .withItem(item)
        .withConditionExpression("attribute_not_exists(#ns_key)")
        .withNameMap(new NameMap().with("#ns_key", HASH_KEY));

    Table table = dynamoDB.getTable(groupTableName);
    final Supplier<PutItemOutcome> putItemOutcomeSupplier = () -> {
      try {
        return table.putItem(putItemSpec);
      } catch (ConditionalCheckFailedException e) {
        logger.error("err=conflict_group_already_exists ns_key={} {}", group.getKey(), e.getMessage());
        throwConflictAlreadyExists(group);
        return null;
      }
    };
    return putItem(group, putItemOutcomeSupplier);
  }

  @Override public Void save(Group group) {
    Item item = preparePutItem(group);
    Table table = dynamoDB.getTable(groupTableName);
    final Supplier<PutItemOutcome> putItemOutcomeSupplier = () -> table.putItem(item);
    return putItem(group, putItemOutcomeSupplier);
  }

  private Item preparePutItem(Group app) {
    String json = Protobuf3Support.toJsonString(app);

    return new Item()
        .withString("id", app.getId())
        .withString(HASH_KEY, app.getKey())
        .withString("name", app.getName())
        .withString("json", json)
        .withString("v", "1")
        .withString("created", app.getCreated())
        .withString("updated", app.getUpdated());
  }

  private Void putItem(Group app, Supplier<PutItemOutcome> putItemOutcomeSupplier) {
    DynamoDbCommand<PutItemOutcome> cmd = new DynamoDbCommand<>("save",
        putItemOutcomeSupplier,
        () -> {
          throw new RuntimeException("save");
        },
        dynamodbNamespaceWriteHystrix,
        metrics);

    PutItemOutcome outcome = cmd.execute();

    logger.info("{} /dynamodb_put_item_result=[{}]",
        kvp("op", "save",
            "appid", app.getId(),
            HASH_KEY, app.getKey(),
            "result", "ok"),
        outcome.getPutItemResult().toString());

    return null;
  }

  @Override public Void saveRelation(Group group, String relationHashKey, String relationRangeKey) {

    Item item = new Item()
        .withString(GroupStorage.SUBJECT_KEY, relationHashKey)
        .withString(GroupStorage.OBJECT_RELATION_KEY, relationRangeKey);

    Table table = dynamoDB.getTable(groupGraphTableName);

    DynamoDbCommand<PutItemOutcome> cmd = new DynamoDbCommand<>("saveRelation",
        () -> table.putItem(item),
        () -> {
          throw new RuntimeException("saveRelation");
        },
        dynamodbNamespaceGraphWriteHystrix,
        metrics);

    PutItemOutcome outcome = cmd.execute();

    logger.info("{} /dynamodb_put_item_result=[{}]",
        kvp("op", "saveRelation",
            "appkey", group.getKey(),
            "hash_key", relationHashKey,
            "range_key", relationRangeKey,
            "result", "ok"),

        outcome.getPutItemResult().toString());

    return null;
  }

  @Override public Void removeRelation(Group group, String relationHashKey, String relationRangeKey) {

    Table table = dynamoDB.getTable(groupGraphTableName);

    final PrimaryKey key = new PrimaryKey(
        GroupStorage.SUBJECT_KEY, relationHashKey,
        GroupStorage.OBJECT_RELATION_KEY, relationRangeKey
    );

    DynamoDbCommand<DeleteItemOutcome> cmd = new DynamoDbCommand<>("removeRelation",
        () -> table.deleteItem(key),
        () -> {
          throw new RuntimeException("removeRelation");
        },
        dynamodbNamespaceGraphWriteHystrix,
        metrics);

    final DeleteItemOutcome deleteItemOutcome = cmd.execute();

    logger.info("{} /dynamodb_remove_item_result=[{}]",
        kvp("op", "removeRelation",
            "appkey", group.getKey(),
            "hash_key", relationHashKey,
            "range_key", relationRangeKey,
            "result", "ok"),
        deleteItemOutcome.getDeleteItemResult().toString());

    return null;
  }

  @Override public boolean queryRelationExists(String relationHashKey, String relationRangeKey) {

    Table table = dynamoDB.getTable(this.groupGraphTableName);

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
        dynamodbNamespaceGraphQueryHystrix,
        metrics);

    // can't use getLastLowLevelResult directly; it's false unless the outcome is iterated first :|
      return cmd.execute().iterator().hasNext();
  }

  @Override public Optional<Group> loadByKey(String key) {
    Table table = dynamoDB.getTable(this.groupTableName);

    QuerySpec querySpec = new QuerySpec()
        .withKeyConditionExpression(HASH_KEY+" = :k_app_key")
        .withValueMap(new ValueMap()
            .withString(":k_app_key", key)
        )
        .withMaxResultSize(1)
        .withConsistentRead(true);

    DynamoDbCommand<ItemCollection<QueryOutcome>> cmd = new DynamoDbCommand<>("loadByKey",
        () -> queryTable(table, querySpec),
        () -> {
          throw new RuntimeException("loadByKey");
        },
        dynamodbNamespaceGraphQueryHystrix,
        metrics);

    final ItemCollection<QueryOutcome> items = cmd.execute();
    final IteratorSupport<Item, QueryOutcome> iterator = items.iterator();
    if (iterator.hasNext()) {
      return Optional.of(GroupSupport.toApp(iterator.next().getString("json")));
    }

    return Optional.empty();

  }

  private ItemCollection<QueryOutcome> queryTable(Table table, QuerySpec querySpec) {
    return table.query(querySpec);
  }
}
