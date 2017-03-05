package outland.feature.server.features;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Feature;
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
      FeatureTableConfiguration featureTableConfiguration,
      @Named("dynamodbFeatureWriteHystrix") HystrixConfiguration hystrixWriteConfiguration,
      @Named("dynamodbFeatureReadHystrix") HystrixConfiguration hystrixReadConfiguration,
      MetricRegistry metrics
  ) {
    this.dynamoDB = new DynamoDB(amazonDynamoDB);
    this.featureTableName = featureTableConfiguration.outlandFeaturesTable;
    this.hystrixWriteConfiguration = hystrixWriteConfiguration;
    this.hystrixReadConfiguration = hystrixReadConfiguration;
    this.metrics = metrics;
  }

  @Override public Void saveFeature(Feature feature) {

    String key = feature.getKey();
    String id = feature.getId();
    String appId = feature.getAppId();

    String json = Protobuf3Support.toJsonString(feature);
    Map<String, String> owner = Maps.newHashMap();

    if (!Strings.isNullOrEmpty(feature.getOwner().getName())) {
      owner.put("name", feature.getOwner().getName());
    }
    owner.put("email", feature.getOwner().getEmail());

    Item item = new Item()
        .withString("app_id", appId)
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

    Table table = dynamoDB.getTable(featureTableName);

    DynamoDbCommand<PutItemOutcome> cmd = new DynamoDbCommand<>("saveFeature",
        () -> table.putItem(item),
        () -> {
          throw new RuntimeException("saveFeature");
        },
        hystrixWriteConfiguration,
        metrics);

    PutItemOutcome outcome = cmd.execute();

    logger.info("{} /dynamodb_put_item_result=[{}]",
        kvp("op", "saveFeature", "app_id", appId, "feature_key", key, "result", "ok"),
        outcome.getPutItemResult().toString());

    return null;
  }

  @Override public Void updateFeature(Feature feature) {
    logger.info("{}",
        kvp("op", "updateFeature", "app_id", feature.getAppId(), "feature_key", feature.getKey()));
    saveFeature(feature);
    return null;
  }

  @Override public Optional<Feature> loadFeatureByKey(String appId, String key) {
    logger.info("{}", kvp("op", "loadFeatureByKey", "app_id", appId, "feature_key", key));

    Table table = dynamoDB.getTable(featureTableName);

    DynamoDbCommand<Item> cmd = new DynamoDbCommand<>("loadFeatureByKey",
        () -> table.getItem(appId, key),
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

  @Override public List<Feature> loadFeatures(String appId) {
    logger.info("{}", kvp("op", "loadFeatures", "app_id", appId));
    List<Feature> features = Lists.newArrayList();

    Table table = dynamoDB.getTable(featureTableName);

    QuerySpec querySpec = new QuerySpec()
        .withKeyConditionExpression("app_id = :k_app_id")
        .withValueMap(new ValueMap().withString(":k_app_id", appId))
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
