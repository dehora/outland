package outland.feature.server.features;

import org.junit.Test;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VersionsTest {
  
  @Test
  public void sendPhysicalTimeInThePast() {
    final long inThePast = 1489018784131L;

    final Versions.Clock clock = () -> inThePast;

    Versions v = new Versions(clock);
    final Versions.HybridLogicalTimestamp send = v.nextVersion();
    final Versions.HybridLogicalTimestamp send1 = v.nextVersion();

    // when our physical clock is stuck, only the counter moves forward
    assertEquals(send.logicalTime(), send1.logicalTime());
    assertTrue(send.counter() < send1.counter());
    assertSame(send.counter(), send1.counter() - 1);
  }

  @Test
  public void sendPhysicalTimeInTheFuture() {

    final int[] calls = {0};

    final Versions.Clock clock = () -> {

      if (calls[0]++ == 0) {
        return (System.currentTimeMillis() * 1000L);
      }

      return (System.currentTimeMillis() * 1000L) + Versions.MAX_SYSTEM_TIME_FORWARD_DRIFT + 1;
    };

    Versions v = new Versions(clock);
    final Versions.HybridLogicalTimestamp send = v.nextVersion();
    final Versions.HybridLogicalTimestamp send1 = v.nextVersion();

    // when our physical clock is too far ahead, only the counter moves forward
    assertEquals(send.logicalTime(), send1.logicalTime());
    assertTrue(send.counter() < send1.counter());
    assertSame(send.counter(), send1.counter() - 1);
  }

  @Test
  public void sendPhysicalTime() {

    // a clock that always moves forward
    final long[] epoch = {System.currentTimeMillis() * 1000L};
    final Versions.Clock clock = () -> epoch[0]++;

    Versions v = new Versions(clock);

    final Versions.HybridLogicalTimestamp send = v.nextVersion();
    final Versions.HybridLogicalTimestamp send1 = v.nextVersion();

    // when the physical clock makes progress, the logical time increases and the counters don't
    assertTrue(send.logicalTime() < send1.logicalTime());
    assertEquals(send.counter(), send1.counter());
    assertEquals(send.counter(), send1.counter());
  }

  @Test
  public void updateOlder() {

    final long[] epoch = {System.currentTimeMillis() * 1000L};
    final Versions.Clock clock = () -> ++epoch[0];

    Versions v = new Versions(clock);

    Versions.HybridLogicalTimestamp older =
        new Versions.HybridLogicalTimestamp(0L, 0L);

    final Versions.HybridLogicalTimestamp send = v.nextVersion();

    // precondition check we're actually ahead
    assertTrue(send.logicalTime() > older.logicalTime());

    // update
    final Versions.HybridLogicalTimestamp receive = v.nextVersionUpdate(older);

    // stayed ahead
    assertTrue(receive.logicalTime() > older.logicalTime());
    // moved head
    assertTrue(receive.logicalTime() > send.logicalTime());
  }

  @Test
  public void updateNewer() {

    final long[] epoch = {System.currentTimeMillis() * 1000L};
    final Versions.Clock clock = () -> ++epoch[0];

    Versions v = new Versions(clock);

    final Versions.HybridLogicalTimestamp nextVersion = v.nextVersion();

    final long recent = clock.timestampMicros() + 10; // push the incoming time fwd

    Versions.HybridLogicalTimestamp newerIncoming =
        new Versions.HybridLogicalTimestamp(recent, 0L);

    // precondition check we're actually getting more recent
    assertTrue(nextVersion.logicalTime() < newerIncoming.logicalTime());

    final Versions.HybridLogicalTimestamp versionUpdate = v.nextVersionUpdate(newerIncoming);

    // took the newer time and bumped its counter
    assertEquals(newerIncoming.logicalTime(), versionUpdate.logicalTime());
    assertTrue(newerIncoming.counter() == versionUpdate.counter() - 1);

    // moved ahead from our last nextVersion
    assertTrue(versionUpdate.logicalTime() > nextVersion.logicalTime());
  }

  @Test
  public void updateSame() {
    final long[] epoch = {System.currentTimeMillis() * 1000L};
    final Versions.Clock clock = () -> epoch[0];

    Versions v = new Versions(clock);

    final Versions.HybridLogicalTimestamp nextVersion = v.nextVersion();

    Versions.HybridLogicalTimestamp incoming =
        new Versions.HybridLogicalTimestamp(clock.timestampMicros(), 1L);

    // precondition check we're actually getting the same time/counter
    assertEquals(nextVersion.logicalTime(), incoming.logicalTime());
    assertEquals(nextVersion.counter(), incoming.counter());

    final Versions.HybridLogicalTimestamp versionUpdate = v.nextVersionUpdate(incoming);

    // the time will be the same, just bumped the counter
    assertEquals(incoming.logicalTime(), versionUpdate.logicalTime());
    assertTrue(incoming.counter() == versionUpdate.counter() - 1);
    assertTrue(nextVersion.counter() == versionUpdate.counter() - 1);

    // now try with a bigger incoming counter

    Versions.HybridLogicalTimestamp incomingAgain =
        new Versions.HybridLogicalTimestamp(clock.timestampMicros(), 5L);

    final Versions.HybridLogicalTimestamp versionUpdateAgain = v.nextVersionUpdate(incomingAgain);

    // the time will be the same, just bumped the counter to use the incoming
    assertEquals(incomingAgain.logicalTime(), versionUpdateAgain.logicalTime());
    assertTrue(incomingAgain.counter() == versionUpdateAgain.counter() - 1);

    // and again with an already seen incoming and its lower counter
    final Versions.HybridLogicalTimestamp versionUpdateAgainAgain =
        v.nextVersionUpdate(incomingAgain);

    assertEquals(incomingAgain.logicalTime(), versionUpdateAgain.logicalTime());
    assertTrue(versionUpdateAgain.counter() > incomingAgain.counter());
    assertTrue(versionUpdateAgain.counter() == versionUpdateAgainAgain.counter() - 1);
  }
}