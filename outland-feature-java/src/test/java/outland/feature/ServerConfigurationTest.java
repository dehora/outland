package outland.feature;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ServerConfigurationTest {

  @Test
  public void testConfigurationNamespace() throws Exception {
    ServerConfiguration configuration = new ServerConfiguration();
    assertEquals(ServerConfiguration.DEFAULT_NAMESPACE, configuration.namespace());

    configuration.namespace("production");
    assertEquals("production", configuration.namespace());
  }

  @Test
  public void testConfiguration() throws Exception {
    ServerConfiguration configuration = new ServerConfiguration()
        .baseURI(new URI("http://localhost"))
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .httpLoggingEnabled(true)
        .certificatePath("foo")
        .initialCacheSize(20)
        .maxCacheSize(12)
        ;
    assertEquals(8000, configuration.connectTimeout());
    assertEquals(7000, configuration.readTimeout());
    assertEquals(true, configuration.httpLoggingEnabled());
    assertEquals("foo", configuration.certificatePath());
    assertEquals(20, configuration.initialCacheSize());
    assertEquals(12, configuration.maxCacheSize());
  }

  @Test
  public void testNamespaceConfiguration() {

    ServerConfiguration configuration = null;

    try {
      // nothing is ok
      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
      ;

      configuration.validate();
    } catch (FeatureException e) {
      fail("non null group should be configurable");
    }

    try {
      // regular string is ok
      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
          .defaultGroup("myapp")
      ;
      configuration.validate();
    } catch (FeatureException e) {
      fail("non null group should be configurable");
    }

    try {
      new ServerConfiguration()
          .baseURI("http://localhost")
          .defaultGroup(null)
      ;
      fail("null group should not be accepted");
    } catch (FeatureException e) {
      assertEquals("empty_group", e.problem().title());
    }

    try {
      new ServerConfiguration()
          .baseURI("http://localhost")
          .defaultGroup("");
      fail("empty group should not be accepted");
    } catch (FeatureException e) {
      assertEquals("empty_group", e.problem().title());
    }
  }
}