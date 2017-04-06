package outland.feature;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ServerConfigurationTest {

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
      fail("non null namespace should be configurable");
    }

    try {
      // regular string is ok
      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
          .defaultNamespace("myapp")
      ;
      configuration.validate();
    } catch (FeatureException e) {
      fail("non null namespace should be configurable");
    }

    try {
      new ServerConfiguration()
          .baseURI("http://localhost")
          .defaultNamespace(null)
      ;
      fail("null namespace should not be accepted");
    } catch (FeatureException e) {
      assertEquals("empty_namespace", e.problem().title());
    }

    try {
      new ServerConfiguration()
          .baseURI("http://localhost")
          .defaultNamespace("");
      fail("empty namespace should not be accepted");
    } catch (FeatureException e) {
      assertEquals("empty_namespace", e.problem().title());
    }
  }
}