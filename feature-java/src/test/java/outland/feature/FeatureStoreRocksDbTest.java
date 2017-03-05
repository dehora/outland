package outland.feature;

import com.codahale.metrics.MetricRegistry;
import java.io.File;
import java.nio.file.Files;
import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeatureStoreRocksDbTest {

  @Test
  public void testDb() throws Exception {

    MetricsContext mc = new MetricsContext("context", new MetricRegistry());

    final File dbPath = Files.createTempDirectory("unittest_outland_feature_store_").toFile();
    FeatureStoreRocksDb db = new FeatureStoreRocksDb(mc, dbPath);

    Feature feature = Feature.newBuilder().setKey("key1").setAppId("app1").build();

    db.put(feature);

    final FeatureCollection featureCollection = db.findAll("app1");
    assertEquals("key1", featureCollection.getItems(0).getKey());
    assertEquals("app1", featureCollection.getItems(0).getAppId());

    db.close();

    dbPath.deleteOnExit();
  }

  @Test
  public void testDbMixedApps() throws Exception {

    MetricsContext mc = new MetricsContext("context", new MetricRegistry());
    final File dbPath = Files.createTempDirectory("unittest_outland_feature_store_").toFile();
    FeatureStoreRocksDb db = new FeatureStoreRocksDb(mc, dbPath);

    Feature feature1 = Feature.newBuilder().setKey("key1").setAppId("app1").build();
    Feature feature1_ = Feature.newBuilder().setKey("key10").setAppId("app1").build();

    // this has an app id that also starts with app1 to check our scans don't match both
    Feature feature10 = Feature.newBuilder().setKey("key1").setAppId("app10").build();
    Feature feature10_ = Feature.newBuilder().setKey("key10").setAppId("app10").build();

    Feature feature2 = Feature.newBuilder().setKey("key2").setAppId("app2").build();
    Feature feature2_ = Feature.newBuilder().setKey("key20").setAppId("app2").build();

    Feature feature3 = Feature.newBuilder().setKey("key3").setAppId("app3").build();
    Feature feature3_ = Feature.newBuilder().setKey("key30").setAppId("app3").build();

    db.put(feature1);
    db.put(feature1_);
    db.put(feature10);
    db.put(feature10_);
    db.put(feature2);
    db.put(feature2_);
    db.put(feature3);
    db.put(feature3_);

    final FeatureCollection collection = db.findAll("app1");

    assertTrue(collection.getItemsCount() == 2);
  }

  @Test
  public void testDbReload() throws Exception {

    MetricsContext mc = new MetricsContext("context", new MetricRegistry());

    final File dbPath = Files.createTempDirectory("unittest_outland_feature_store_").toFile();
    FeatureStoreRocksDb db = new FeatureStoreRocksDb(mc, dbPath);

    Feature feature2 = Feature.newBuilder().setKey("key2").setAppId("app2").build();

    db.put(feature2);
    db.close();

    // load up a new db and fetch app2's stuff

    FeatureStoreRocksDb db1 = new FeatureStoreRocksDb(mc, dbPath);

    final FeatureCollection featureCollection = db1.findAll("app2");
    assertTrue(featureCollection.getItemsCount() == 1);
    assertEquals("key2", featureCollection.getItems(0).getKey());
    assertEquals("app2", featureCollection.getItems(0).getAppId());

    // add another app's feature

    Feature feature1 = Feature.newBuilder().setKey("key1").setAppId("app1").build();
    db1.put(feature1);

    db1.close();

    // load up a new db and check app1 and app2's stuff

    FeatureStoreRocksDb db2 = new FeatureStoreRocksDb(mc, dbPath);
    final FeatureCollection featureCollection1 = db2.findAll("app1");
    assertTrue(featureCollection1.getItemsCount() == 1);
    assertEquals("key1", featureCollection1.getItems(0).getKey());
    assertEquals("app1", featureCollection1.getItems(0).getAppId());

    final FeatureCollection featureCollection2 = db2.findAll("app2");
    assertTrue(featureCollection2.getItemsCount() == 1);
    assertEquals("key2", featureCollection2.getItems(0).getKey());
    assertEquals("app2", featureCollection2.getItems(0).getAppId());

    // check load all reads everything

    final FeatureCollection featureCollection3 = db2.loadAll();
    assertTrue(featureCollection3.getItemsCount() == 2);

    dbPath.deleteOnExit();
  }
}