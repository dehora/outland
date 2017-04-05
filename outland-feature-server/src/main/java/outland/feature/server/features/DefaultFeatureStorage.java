package outland.feature.server.features;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureVersion;
import outland.feature.server.hystrix.HystrixConfiguration;
import outland.feature.server.protobuf.Protobuf3Support;

import static outland.feature.server.StructLog.kvp;

public class DefaultFeatureStorage implements FeatureStorage {

  private static final Logger logger = LoggerFactory.getLogger(DefaultFeatureStorage.class);

  static Map<String, Feature> features = Maps.newHashMap();

  private final DynamoDB dynamoDB;
  private final String featureTableName;
  private final HystrixConfiguration hystrixWriteConfiguration;
  private final HystrixConfiguration hystrixReadConfiguration;
  private final MetricRegistry metrics;

  @Inject
  public DefaultFeatureStorage(
      AmazonDynamoDB amazonDynamoDB,
      TableConfiguration tableConfiguration,
      @Named("dynamodbFeatureWriteHystrix") HystrixConfiguration hystrixWriteConfiguration,
      @Named("dynamodbFeatureReadHystrix") HystrixConfiguration hystrixReadConfiguration,
      MetricRegistry metrics
  ) {
    this.dynamoDB = new DynamoDB(amazonDynamoDB);
    this.featureTableName = tableConfiguration.outlandFeaturesTable;
    this.hystrixWriteConfiguration = hystrixWriteConfiguration;
    this.hystrixReadConfiguration = hystrixReadConfiguration;
    this.metrics = metrics;
  }

  @Override public Void createFeature(Feature feature) {

    final String key = feature.getKey();
    final String nsKey = feature.getNamespace();
    final Item item = preparePutItem(feature);

    final PutItemSpec putItemSpec = new PutItemSpec()
        .withItem(item)
        .withConditionExpression("attribute_not_exists(#featurekey)")
        .withNameMap(new NameMap().with("#featurekey", "feature_key"));

    final Supplier<PutItemOutcome> putItemOutcomeSupplier = () -> {
      try {
        return dynamoDB.getTable(featureTableName).putItem(putItemSpec);
      } catch (ConditionalCheckFailedException e) {
        logger.error("err=conflict_feature_already_exists nskey={} {}", feature.getKey(), e.getMessage());
        throwConflictAlreadyExists(feature);
        return null;
      }
    };

    final DynamoDbCommand<PutItemOutcome> cmd = new DynamoDbCommand<>("createFeature",
        putItemOutcomeSupplier,
        () -> {
          throw new RuntimeException("createFeature");
        },
        hystrixWriteConfiguration,
        metrics);

    final PutItemOutcome outcome = cmd.execute();

    logger.info("{} /dynamodb_put_item_result=[{}]",
        kvp("op", "createFeature", "nskey", nsKey, "feature_key", key, "result", "ok"),
        outcome.getPutItemResult().toString());

    return null;
  }

  @Override public Void
  updateFeature(Feature feature, FeatureVersion previousVersion) {
    logger.info("{}",
        kvp("op", "updateFeature", "nskey", feature.getNamespace(), "feature_key", feature.getKey()));

    final String key = feature.getKey();
    final String nskey = feature.getNamespace();
    final Item item = preparePutItem(feature);

    final PutItemSpec putItemSpec = new PutItemSpec()
        .withItem(item)
        .withExpected(new Expected("version_id").eq(previousVersion.getId()));

    final Supplier<PutItemOutcome> putItemOutcomeSupplier = () -> {
      try {
        return dynamoDB.getTable(featureTableName).putItem(putItemSpec);
      } catch (ConditionalCheckFailedException e) {
        logger.error("err=conflict_feature_version_mismatch nskey={} {}", feature.getKey(), e.getMessage());
        throwConflictVersionMismatch(feature);
        return null;
      }
    };

    final DynamoDbCommand<PutItemOutcome> cmd = new DynamoDbCommand<>("updateFeature",
        putItemOutcomeSupplier,
        () -> {
          throw new RuntimeException("updateFeature");
        },
        hystrixWriteConfiguration,
        metrics);

    final PutItemOutcome outcome = cmd.execute();

    logger.info("{} /dynamodb_update_item_result=[{}]",
        kvp("op", "updateFeature", "nskey", nskey, "feature_key", key, "result", "ok"),
        outcome.getPutItemResult().toString());

    return null;
  }

  @Override public Optional<Feature> loadFeatureByKey(String nskey, String key) {
    logger.info("{}", kvp("op", "loadFeatureByKey", "nskey", nskey, "feature_key", key));

    Table table = dynamoDB.getTable(featureTableName);

    DynamoDbCommand<Item> cmd = new DynamoDbCommand<>("loadFeatureByKey",
        () -> getItem(nskey, key, table),
        () -> {
          throw new RuntimeException("loadFeatureById");
        },
        hystrixReadConfiguration,
        metrics);

    Item item = cmd.execute();
    if (item == null) {
      return Optional.empty();
    }
    return Optional.of(FeatureSupport.toFeature(item.getString("json")));
  }

  private Item preparePutItem(Feature feature) {
    final String key = feature.getKey();
    final String id = feature.getId();
    final String nskey = feature.getNamespace();
    final String json = Protobuf3Support.toJsonString(feature);

    final Map<String, String> owner = Maps.newHashMap();

    if (!Strings.isNullOrEmpty(feature.getOwner().getName())) {
      owner.put("name", feature.getOwner().getName());
    }

    if (!Strings.isNullOrEmpty(feature.getOwner().getEmail())) {
      owner.put("email", feature.getOwner().getEmail());
    }

    if (!Strings.isNullOrEmpty(feature.getOwner().getUsername())) {
      owner.put("username", feature.getOwner().getUsername());
    }

    final Item item = new Item()
        .withString("ns_key", nskey)
        .withString("version_id", feature.getVersion().getId())
        .withNumber("version_timestamp", feature.getVersion().getTimestamp())
        .withNumber("version_counter", feature.getVersion().getCounter())
        .withString("feature_key", key)
        .withString("id", id)
        .withString("state", feature.getState().name())
        .withString("json", json)
        .withString("v", "1")
        .withString("created", feature.getCreated())
        .withString("updated", feature.getUpdated())
        .withMap("owner", owner);

    if (!Strings.isNullOrEmpty(feature.getDescription())) {
      item.withString("desc", feature.getDescription());
    }
    return item;
  }

  private Item getItem(String nskey, String key, Table table) {
    return table.getItem("nskey", nskey, "feature_key",key);
  }

  @Override public List<Feature> loadFeatures(String nskey) {
    logger.info("{}", kvp("op", "loadFeatures", "nskey", nskey));
    List<Feature> features = Lists.newArrayList();

    Table table = dynamoDB.getTable(featureTableName);

    QuerySpec querySpec = new QuerySpec()
        .withKeyConditionExpression("ns_key = :k_ns_key")
        .withValueMap(new ValueMap().withString(":k_ns_key", nskey))
        .withConsistentRead(true);

    DynamoDbCommand<ItemCollection<QueryOutcome>> cmd = new DynamoDbCommand<>("loadFeatures",
        () -> queryTable(table, querySpec),
        () -> {
          throw new RuntimeException("loadFeatureById");
        },
        hystrixReadConfiguration,
        metrics);

    ItemCollection<QueryOutcome> items = cmd.execute();

    for (Page<Item, QueryOutcome> page : items.pages()) {
      page.forEach(item -> features.add(FeatureSupport.toFeature(item.getString("json"))));
    }

    return features;
  }

  private ItemCollection<QueryOutcome> queryTable(Table table, QuerySpec querySpec) {
    return table.query(querySpec);
  }
}
