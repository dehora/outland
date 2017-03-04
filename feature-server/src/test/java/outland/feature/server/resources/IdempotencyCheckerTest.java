package outland.feature.server.resources;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import outland.feature.server.features.Ulid;
import outland.feature.server.redis.RedisConfiguration;
import outland.feature.server.redis.RedisModule;
import outland.feature.server.redis.RedisServersConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IdempotencyCheckerTest {

  @Test
  public void testCheck() {

    RedisConfiguration rc = new RedisConfiguration();
    rc.name = "outland_feature_idempotency_check_redis";
    RedisServersConfiguration rsc = new RedisServersConfiguration();
    rsc.servers = Lists.newArrayList(rc);

    final Injector injector = Guice.createInjector(
        new RedisModule(rsc),
        new AbstractModule() {
          @Override protected void configure() {
            bind(IdempotencyChecker.class).to(IdempotencyCheckerRedis.class).asEagerSingleton();
          }
        }
    );

    final IdempotencyChecker checker = injector.getInstance(IdempotencyChecker.class);

    final String key = Ulid.random();
    assertFalse(checker.seen(key));
    assertTrue(checker.seen(key));

    assertFalse(checker.seen(Ulid.random()));
    assertFalse(checker.seen(Ulid.random()));
  }
}