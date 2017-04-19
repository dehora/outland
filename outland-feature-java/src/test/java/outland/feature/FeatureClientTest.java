package outland.feature;

import java.util.function.Supplier;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class FeatureClientTest {

  @Test
  public void testGuards() {

    ServerConfiguration serverConfiguration =
        new ServerConfiguration()
            .baseURI("http://localhost")
            .defaultGroup("the_app");

    final FeatureClient client = FeatureClient.newBuilder()
        .serverConfiguration(serverConfiguration)
        .localFeatureStore(new FeatureStoreLocalNone())
        .featureStore(new FeatureStoreNone())
        .build();

    expectIllegalArgument(() -> client.enabled(null));
    expectIllegalArgument(() -> client.enabled(""));

    expectIllegalArgument(() -> client.enabled("grp", null));
    expectIllegalArgument(() -> client.enabled("", "f"));
    expectIllegalArgument(() -> client.enabled(null, "f"));
    expectIllegalArgument(() -> client.enabled("g", ""));

    expectIllegalArgument(() -> client.enabledThrowing("grp", null));
    expectIllegalArgument(() -> client.enabledThrowing("", "f"));
    expectIllegalArgument(() -> client.enabledThrowing(null, "f"));
    expectIllegalArgument(() -> client.enabledThrowing("g", ""));

    expectIllegalArgument(() -> client.select(""));
    expectIllegalArgument(() -> client.select(null));

    expectIllegalArgument(() -> client.select("", "f"));
    expectIllegalArgument(() -> client.select(null, "f"));
    expectIllegalArgument(() -> client.select("g", ""));
    expectIllegalArgument(() -> client.select("g", null));

    expectIllegalArgument(() -> client.selectThrowing("" ));
    expectIllegalArgument(() -> client.selectThrowing(null ));

    expectIllegalArgument(() -> client.selectThrowing("", "f"));
    expectIllegalArgument(() -> client.selectThrowing(null, "f"));
    expectIllegalArgument(() -> client.selectThrowing("g", ""));
    expectIllegalArgument(() -> client.selectThrowing("g", null));

    expectIllegalArgument(() -> client.selectBoolean(""));
    expectIllegalArgument(() -> client.selectBoolean(null));

    expectIllegalArgument(() -> client.selectBooleanThrowing(""));
    expectIllegalArgument(() -> client.selectBooleanThrowing(null));

    expectIllegalArgument(() -> client.selectBooleanThrowing("", "f"));
    expectIllegalArgument(() -> client.selectBooleanThrowing(null, "f"));
    expectIllegalArgument(() -> client.selectBooleanThrowing("g", ""));
    expectIllegalArgument(() -> client.selectBooleanThrowing("g", null));

    expectIllegalArgument(() -> client.selectString(""));
    expectIllegalArgument(() -> client.selectString(null));

    expectIllegalArgument(() -> client.selectString("", "f"));
    expectIllegalArgument(() -> client.selectString(null, "f"));
    expectIllegalArgument(() -> client.selectString("g", ""));
    expectIllegalArgument(() -> client.selectString("g", null));

    expectIllegalArgument(() -> client.selectStringThrowing(""));
    expectIllegalArgument(() -> client.selectStringThrowing(null));

    expectIllegalArgument(() -> client.selectStringThrowing("", "f"));
    expectIllegalArgument(() -> client.selectStringThrowing(null, "f"));
    expectIllegalArgument(() -> client.selectStringThrowing("g", ""));
    expectIllegalArgument(() -> client.selectStringThrowing("g", null));
  }

  private void expectIllegalArgument(Supplier supplier) {
    try {
      supplier.get();
      fail();
    } catch (FeatureException e) {
      assertTrue(e.getCause() instanceof IllegalArgumentException);
    }
  }

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