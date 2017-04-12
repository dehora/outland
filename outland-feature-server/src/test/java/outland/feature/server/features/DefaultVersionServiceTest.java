package outland.feature.server.features;

import org.junit.Test;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultVersionServiceTest {
  
  @Test
  public void sendPhysicalTimeInThePast() {
    final long inThePast = 1489018784131L;

    final DefaultVersionService.Clock clock = () -> inThePast;

    DefaultVersionService v = new DefaultVersionService(clock);
    final DefaultVersionService.HybridLogicalTimestamp send = v.nextVersion();
    final DefaultVersionService.HybridLogicalTimestamp send1 = v.nextVersion();

    // when our physical clock is stuck, only the counter moves forward
    assertEquals(send.logicalTime(), send1.logicalTime());
    assertTrue(send.counter() < send1.counter());
    assertSame(send.counter(), send1.counter() - 1);
  }

  @Test
  public void sendPhysicalTimeInTheFuture() {

    final int[] calls = {0};

    final DefaultVersionService.Clock clock = () -> {

      if (calls[0]++ == 0) {
        return (System.currentTimeMillis() * 1000L);
      }

      return (System.currentTimeMillis() * 1000L) + DefaultVersionService.MAX_SYSTEM_TIME_FORWARD_DRIFT + 1;
    };

    DefaultVersionService v = new DefaultVersionService(clock);
    final DefaultVersionService.HybridLogicalTimestamp send = v.nextVersion();
    final DefaultVersionService.HybridLogicalTimestamp send1 = v.nextVersion();

    // when our physical clock is too far ahead, only the counter moves forward
    assertEquals(send.logicalTime(), send1.logicalTime());
    assertTrue(send.counter() < send1.counter());
    assertSame(send.counter(), send1.counter() - 1);
  }

  @Test
  public void sendPhysicalTime() {

    // a clock that always moves forward
    final long[] epoch = {System.currentTimeMillis() * 1000L};
    final DefaultVersionService.Clock clock = () -> epoch[0]++;

    DefaultVersionService v = new DefaultVersionService(clock);

    final DefaultVersionService.HybridLogicalTimestamp send = v.nextVersion();
    final DefaultVersionService.HybridLogicalTimestamp send1 = v.nextVersion();

    // when the physical clock makes progress, the logical time increases and the counters don't
    assertTrue(send.logicalTime() < send1.logicalTime());
    assertEquals(send.counter(), send1.counter());
    assertEquals(send.counter(), send1.counter());
  }

  @Test
  public void updateOlder() {

    final long[] epoch = {System.currentTimeMillis() * 1000L};
    final DefaultVersionService.Clock clock = () -> ++epoch[0];

    DefaultVersionService v = new DefaultVersionService(clock);

    DefaultVersionService.HybridLogicalTimestamp older =
        new DefaultVersionService.HybridLogicalTimestamp(0L, 0L);

    final DefaultVersionService.HybridLogicalTimestamp send = v.nextVersion();

    // precondition check we're actually ahead
    assertTrue(send.logicalTime() > older.logicalTime());

    // update
    final DefaultVersionService.HybridLogicalTimestamp receive = v.nextVersionUpdate(older);

    // stayed ahead
    assertTrue(receive.logicalTime() > older.logicalTime());
    // moved head
    assertTrue(receive.logicalTime() > send.logicalTime());
  }

  @Test
  public void updateNewer() {

    final long[] epoch = {System.currentTimeMillis() * 1000L};
    final DefaultVersionService.Clock clock = () -> ++epoch[0];

    DefaultVersionService v = new DefaultVersionService(clock);

    final DefaultVersionService.HybridLogicalTimestamp nextVersion = v.nextVersion();

    final long recent = clock.timestampMicros() + 10; // push the incoming time fwd

    DefaultVersionService.HybridLogicalTimestamp newerIncoming =
        new DefaultVersionService.HybridLogicalTimestamp(recent, 0L);

    // precondition check we're actually getting more recent
    assertTrue(nextVersion.logicalTime() < newerIncoming.logicalTime());

    final DefaultVersionService.HybridLogicalTimestamp versionUpdate = v.nextVersionUpdate(newerIncoming);

    // took the newer time and bumped its counter
    assertEquals(newerIncoming.logicalTime(), versionUpdate.logicalTime());
    assertTrue(newerIncoming.counter() == versionUpdate.counter() - 1);

    // moved ahead from our last nextVersion
    assertTrue(versionUpdate.logicalTime() > nextVersion.logicalTime());
  }

  @Test
  public void updateSame() {
    final long[] epoch = {System.currentTimeMillis() * 1000L};
    final DefaultVersionService.Clock clock = () -> epoch[0];

    DefaultVersionService v = new DefaultVersionService(clock);

    final DefaultVersionService.HybridLogicalTimestamp nextVersion = v.nextVersion();

    DefaultVersionService.HybridLogicalTimestamp incoming =
        new DefaultVersionService.HybridLogicalTimestamp(clock.timestampMicros(), 1L);

    // precondition check we're actually getting the same time/counter
    assertEquals(nextVersion.logicalTime(), incoming.logicalTime());
    assertEquals(nextVersion.counter(), incoming.counter());

    final DefaultVersionService.HybridLogicalTimestamp versionUpdate = v.nextVersionUpdate(incoming);

    // the time will be the same, just bumped the counter
    assertEquals(incoming.logicalTime(), versionUpdate.logicalTime());
    assertTrue(incoming.counter() == versionUpdate.counter() - 1);
    assertTrue(nextVersion.counter() == versionUpdate.counter() - 1);

    // now try with a bigger incoming counter

    DefaultVersionService.HybridLogicalTimestamp incomingAgain =
        new DefaultVersionService.HybridLogicalTimestamp(clock.timestampMicros(), 5L);

    final DefaultVersionService.HybridLogicalTimestamp versionUpdateAgain = v.nextVersionUpdate(incomingAgain);

    // the time will be the same, just bumped the counter to use the incoming
    assertEquals(incomingAgain.logicalTime(), versionUpdateAgain.logicalTime());
    assertTrue(incomingAgain.counter() == versionUpdateAgain.counter() - 1);

    // and again with an already seen incoming and its lower counter
    final DefaultVersionService.HybridLogicalTimestamp versionUpdateAgainAgain =
        v.nextVersionUpdate(incomingAgain);

    assertEquals(incomingAgain.logicalTime(), versionUpdateAgain.logicalTime());
    assertTrue(versionUpdateAgain.counter() > incomingAgain.counter());
    assertTrue(versionUpdateAgain.counter() == versionUpdateAgainAgain.counter() - 1);
  }
}