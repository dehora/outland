package outland.feature;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FeatureClientTest {

  @Test
  public void throwMultiAndSingleApp() {
    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .appId("the_app")
            .multiAppEnabled(true);

    assertTrue(serverConfiguration.multiAppEnabled());
    assertTrue(serverConfiguration.appId() != null);

    try {
      FeatureClient.newBuilder()
          .serverConfiguration(serverConfiguration)
          .featureStore(new FeatureStoreNone())
          .localFeatureStore(new FeatureStoreLocalNone())
          .build();
      fail();
    } catch (FeatureException e) {
      assertEquals("multi_app_and_single_app_enabled", e.problem().title());
    }
  }

  @Test
  public void throwIfMissing() {

    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .appId("the_app");

    final FeatureClient client = FeatureClient.newBuilder()
        .serverConfiguration(serverConfiguration)
        .localFeatureStore(new FeatureStoreLocalNone())
        .featureStore(new FeatureStoreNone())
        .build();

    assertFalse(client.enabled("throwIfMissing"));

    try {
      client.enabledThrowing("throwIfMissing");
      fail();
    } catch (FeatureException e) {
      assertEquals(Problem.MISSING_FEATURE_TYPE, e.problem().type());
    }
  }

  @Test
  public void testNopeBuilder() {
    
    try {
      FeatureClient.newBuilder()
          .featureStore(new FeatureStoreNone())
          .localFeatureStore(new FeatureStoreLocalNone())
          .build();
      fail();
    } catch (FeatureException e) {
      assertEquals("Please supply at least one feature or outland configuration",
          e.problem().title());
    }

    try {
      FeatureClient.newBuilder()
          .featureStore(new FeatureStoreNone())
          .localFeatureStore(new FeatureStoreLocalNone())
          .serverConfiguration(new ServerConfiguration())
          .build();
    } catch (FeatureException e) {
      assertEquals("Please provide a base URI for the feature server",
          e.problem().title());
    }
  }

  @Test
  public void testMinBuilder() {

    FeatureClient client = null;
    try {
      ServerConfiguration serverConfiguration =
          new ServerConfiguration()
              .baseURI("http://localhost")
              .appId("the_app");

      client = FeatureClient.newBuilder()
          .featureStore(new FeatureStoreNone())
          .localFeatureStore(new FeatureStoreLocalNone())
          .serverConfiguration(serverConfiguration)
          .build();

      //assertNull(client.clusterConfiguration());
      assertNotNull(client.serverConfiguration());
      assertNotNull(client.contentSupport());
      assertNotNull(client.metricRegistry());
      assertNotNull(client.resourceProvider());
      assertNotNull(client.authorizationProvider());
    } finally {
      if (client != null) {
        client.close();
      }
    }
  }
}