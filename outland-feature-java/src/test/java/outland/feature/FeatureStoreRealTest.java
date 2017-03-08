package outland.feature;

import com.codahale.metrics.MetricRegistry;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class FeatureStoreRealTest {

  @Test
  public void multiAppConfiguredOnlyCallsLocalStore() throws Exception {
    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .multiAppEnabled(true); //

    final FeatureClient client = FeatureClient.newBuilder()
        // set useless store and backing
        .featureStore(new FeatureStoreFailing())
        .localFeatureStore(new FeatureStoreLocalNull())
        .serverConfiguration(serverConfiguration)
        .build();

    assertTrue(client.appId() == null);
    assertTrue(client.localStoreEnabled());

    final File dbPath = Files.createTempDirectory("unittest_outland_feature_store_").toFile();

    FeatureStoreLocal rocks =
        new FeatureStoreRocksDb(new MetricsContext("foo", new MetricRegistry()), dbPath);

    FeatureStoreReal fs = new FeatureStoreReal(client, rocks);
    try {
      fs.open();
      assertFalse("should not call api server for multi app",
          fs.loadFromApiAttempted.get());
      assertFalse("should not load api server for multi app when api server down",
          fs.loadFromApiSuccessful.get());
      assertTrue("should call rocksdb for multi app and local enabled",
          fs.loadFromLocalAttempted.get());
      assertTrue("should load rocksdb for multi app and local enabled",
          fs.loadFromLocalSuccessful.get());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    } finally {
      fs.close();
      dbPath.deleteOnExit();
    }
  }


  @Test
  public void multiAppConfiguredLocalStoreDisabledCallsNothing() {
    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .localStoreEnabled(false)
            .multiAppEnabled(true); //

    final FeatureClient client = FeatureClient.newBuilder()
        // set useless store and backing
        .featureStore(new FeatureStoreFailing())
        .localFeatureStore(new FeatureStoreLocalNone())
        .serverConfiguration(serverConfiguration)
        .build();

    assertTrue(client.appId() == null);
    assertTrue(client.multiAppEnabled());
    assertFalse(client.localStoreEnabled());

    FeatureStoreLocal rocks =
        new FeatureStoreRocksDb(new MetricsContext("foo", new MetricRegistry()));

    FeatureStoreReal fs = new FeatureStoreReal(client, rocks);
    try {
      fs.open();
      assertFalse("should not call api server for multi app",
          fs.loadFromApiAttempted.get());
      assertFalse("should not load api server for multi app when api server down",
          fs.loadFromApiSuccessful.get());
      assertFalse("should not call rocksdb for multi app and local disabled",
          fs.loadFromLocalAttempted.get());
      assertFalse("should not load rocksdb for multi app and local disabled",
          fs.loadFromLocalSuccessful.get());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    } finally {
      fs.close();
    }
  }

  @Test
  public void localStoreDisabledSingleAppConfiguredOnlyCallsApiServer() {
    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .localStoreEnabled(false) // disable rocks
            .appId("anapp");

    final FeatureClient client = FeatureClient.newBuilder()
        // set useless store and backing
        .featureStore(new FeatureStoreFailing())
        .localFeatureStore(new FeatureStoreLocalNone())
        .serverConfiguration(serverConfiguration)
        .build();

    assertTrue(client.appId() != null);
    assertFalse(client.localStoreEnabled());

    FeatureStoreLocal rocks =
        new FeatureStoreRocksDb(new MetricsContext("foo", new MetricRegistry()));

    FeatureStoreReal fs = new FeatureStoreReal(client, rocks);
    try {
      fs.open();
      assertTrue("should call api server for single app",
          fs.loadFromApiAttempted.get());
      assertFalse("should not load api server for single app when api server down",
          fs.loadFromApiSuccessful.get());
      assertFalse("should not call rocksdb for single app and local disabled",
          fs.loadFromLocalAttempted.get());
      assertFalse("should not load rocksdb for single app and local disabled",
          fs.loadFromLocalSuccessful.get());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    } finally {
      fs.close();
    }
  }

  @Test
  public void noApiServerDoesNotCrashTheFeatureLoader() {

    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .appId("the_app");

    final FeatureClient client = FeatureClient.newBuilder()
        // set useless store and backing
        .featureStore(new FeatureStoreFailing())
        .localFeatureStore(new FeatureStoreLocalNull())
        .serverConfiguration(serverConfiguration)
        .build();

    FeatureStoreLocal none = new FeatureStoreLocalNull();

    FeatureStoreReal fs = new FeatureStoreReal(client, none);
    try {
      fs.open();
      assertTrue("should call api server for single app and local enabled",
          fs.loadFromApiAttempted.get());
      assertFalse("should not load api server for single app when server down",
          fs.loadFromApiSuccessful.get());
      assertTrue("should call rocksdb for single app and local enabled",
          fs.loadFromLocalAttempted.get());
      assertFalse("should call rocksdb for single app and local enabled",
          fs.loadFromLocalSuccessful.get());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    } finally {
      fs.close();
    }
  }

  @Test
  public void noApiServerAndFailingLocalStoreDoesNotCrashTheFeatureLoader() {

    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .appId("the_app");

    final FeatureClient client = FeatureClient.newBuilder()
        // set useless store and backing
        .featureStore(new FeatureStoreFailing())
        .localFeatureStore(new FeatureStoreLocalNone())
        .serverConfiguration(serverConfiguration)
        .build();

    FeatureStoreLocal failing = new FeatureStoreLocalFailing();

    FeatureStoreReal fs = new FeatureStoreReal(client, failing);
    try {
      fs.open();
      assertTrue("should call api server for single app",
          fs.loadFromApiAttempted.get());
      assertFalse("should call api server for single app when api server down",
          fs.loadFromApiSuccessful.get());
      assertTrue(
          "should call local store for single app",
          fs.loadFromLocalAttempted.get());
      assertFalse("should not load local store for single app when local store crashes",
          fs.loadFromLocalSuccessful.get());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void noApiServerWithRocksOk() {

    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .appId("the_app");

    final FeatureClient client = FeatureClient.newBuilder()
        // storage requests are circumvented with exceptions
        .featureStore(new FeatureStoreFailing())
        .localFeatureStore(new FeatureStoreLocalFailing())
        .serverConfiguration(serverConfiguration)
        .build();

    FeatureStoreLocal rocks = new FeatureStoreRocksDb(new MetricsContext("foo", new MetricRegistry()));

    FeatureStoreReal fs = new FeatureStoreReal(client, rocks);
    try {
      fs.open();
      assertTrue("should call api server for single app",
          fs.loadFromApiAttempted.get());
      assertFalse("should call api server for single app when api server down",
          fs.loadFromApiSuccessful.get());
      assertTrue("should call local store for single app",
          fs.loadFromLocalAttempted.get());
      assertTrue("should local local store for single app",
          fs.loadFromLocalSuccessful.get());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    } finally {
      fs.close();
    }
  }

  static class FeatureStoreFailing implements FeatureStore {
    @Override public Void put(Feature feature) throws FeatureException {
      return null;
    }

    @Override public Optional<Feature> find(String appId, String key) throws FeatureException {
      throw new FeatureException(Problem.localProblem("failstore", ""));
    }

    @Override public FeatureCollection findAll(String appId) throws FeatureException {
      throw new FeatureException(Problem.localProblem("failstore", ""));
    }

    @Override public Void remove(String appId, String featureKey) throws FeatureException {
      throw new FeatureException(Problem.localProblem("failstore", ""));
    }

    @Override public Void removeAll() throws FeatureException {
      throw new FeatureException(Problem.localProblem("failstore", ""));
    }

    @Override public void close() throws FeatureException {
      throw new FeatureException(Problem.localProblem("failstore", ""));
    }
  }
}