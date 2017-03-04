package outland.feature.server.redis;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedisTest {

  @Test
  public void testLoad() {

    RedisConfiguration rc = new RedisConfiguration();
    rc.name = "r1";
    RedisServersConfiguration rsc = new RedisServersConfiguration();
    rsc.servers = Lists.newArrayList(rc);

    final Injector injector = Guice.createInjector(new RedisModule(rsc),
        new AbstractModule() {
          @Override protected void configure() {
            bind(RedisApp.class);
          }
        });

    final RedisManaged managed = injector.getInstance(RedisManaged.class);
    final RedisHealthCheck health = injector.getInstance(RedisHealthCheck.class);
    final RedisProviders providers = injector.getInstance(RedisProviders.class);

    assertTrue(managed != null);
    assertTrue(health != null);
    assertTrue(providers != null);
    assertTrue(providers.getProviders().size() == 1);

    final RedisApp instance = injector.getInstance(RedisApp.class);

    final RedisProvider r1 = instance.providerMap().get("r1");
    assertEquals("r1", r1.getName());
    r1.closePool();

  }

  static class RedisApp {

    private final Map<String, RedisProvider> providerMap;

    @Inject
    public RedisApp(
        Map<String, RedisProvider> providerMap) {
      this.providerMap = providerMap;
    }

    public Map<String, RedisProvider> providerMap() {
      return providerMap;
    }
  }
}
