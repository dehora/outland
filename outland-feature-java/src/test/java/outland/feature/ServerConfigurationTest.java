package outland.feature;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ServerConfigurationTest {

  @Test
  public void testMultiAndSingleAppCombos() {

    ServerConfiguration configuration = null;

    try {
      // neither enabled
      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
          .namespace(null)
          .multiAppEnabled(false);
      configuration.validate();
      fail();
    } catch (FeatureException e) {
      assertEquals("neither_multi_app_or_single_app_enabled", e.problem().title());
    }

    try {
      // both enabled
      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
          .namespace("myapp")
          .multiAppEnabled(true);

      configuration.validate();
      fail();
    } catch (FeatureException e) {
      assertEquals("multi_app_and_single_app_enabled", e.problem().title());
    }

    try {
      // app id enabled and false
      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
          .namespace("myapp")
          .multiAppEnabled(false);

      configuration.validate();

      // app id enabled and default multi (disabled)
      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
          .namespace("myapp");

      configuration.validate();
    } catch (FeatureException e) {
      e.printStackTrace();
      fail();
    }

    try {

      // app id enabled but empty
      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
          .namespace("");

      configuration.validate();
      fail();
    } catch (FeatureException e) {
      e.printStackTrace();
      assertEquals("neither_multi_app_or_single_app_enabled", e.problem().title());
    }

    try {
      // multi enabled and null app id: ok

      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
          .namespace(null)
          .multiAppEnabled(true);
      configuration.validate();

      // multi enabled and no app id: ok

      configuration = new ServerConfiguration()
          .baseURI("http://localhost")
          .multiAppEnabled(true);

      configuration.validate();

    } catch (FeatureException e) {
      e.printStackTrace();
      fail();
    }
  }
}