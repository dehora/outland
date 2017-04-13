package outland.feature;

import org.junit.Test;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureData;
import outland.feature.proto.FeatureOption;
import outland.feature.proto.NamespaceFeature;
import outland.feature.proto.NamespaceFeatureCollection;
import outland.feature.proto.OptionCollection;
import outland.feature.proto.OptionType;
import outland.feature.proto.State;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class FeatureClientTest {

  @Test
  public void testWithAndWithoutDefaultNamespaceIsOk() {
    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .defaultGroup("the_app");

    assertTrue(serverConfiguration.defaultGroup() != null);

    // we can create a client without suppyling a default ns
    try {
      FeatureClient.newBuilder()
          .serverConfiguration(serverConfiguration)
          .featureStore(new FeatureStoreNone())
          .localFeatureStore(new FeatureStoreLocalNone())
          .build();
    } catch (FeatureException e) {
      fail();
    }
  }

  @Test
  public void throwIfMissing() {

    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .defaultGroup("the_app");

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
      assertEquals("Please supply a configuration",
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
              .defaultGroup("the_app");

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