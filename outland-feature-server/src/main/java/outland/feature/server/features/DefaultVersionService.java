package outland.feature.server.features;

import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static outland.feature.server.StructLog.kvp;

public class DefaultVersionService implements VersionService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultVersionService.class);

  private static final long ONE_HOUR_MICROSECONDS = 3600L * 1000L * 1000L;

  // no idea what are good values here, let's do it live
  static final long MAX_SYSTEM_TIME_FORWARD_DRIFT = 12 * ONE_HOUR_MICROSECONDS;
  private static final long MAX_INCOMING_TIME_FORWARD_DRIFT = 12 * ONE_HOUR_MICROSECONDS;
  private static final long INITIAL_COUNTER = 0L;

  private final Clock clock;
  private final AtomicLong localTime;
  private final AtomicLong localCounter;

  DefaultVersionService(Clock clock) {
    this.clock = clock;
    // todo: accept these as values, to allow us to memo logicalTime from previous starts
    localTime = new AtomicLong(clock.timestampMicros());
    localCounter = new AtomicLong(INITIAL_COUNTER);
  }

  DefaultVersionService() {
    this(new SystemClock());
  }

  @Override
  public HybridLogicalTimestamp nextVersion() {

    final long physicalNow = this.clock.timestampMicros();

    if (localTime.get() >= physicalNow) {
      //physical clock hasn't moved forward relative to local, keep the logicalTime, bump counter
      localCounter.incrementAndGet();
      traceBackwardDrift(physicalNow, localTime.get());
    } else {
      // the physical clock is ahead

      /*
       a hack to handle the 2051 problem; if our system clock is "too" far ahead of our local
       logicalTime then all future versions get dragged forward to keep up with it and we have
       to wait for "reality" to catch up with that logicalTime to reset things.

       todo: this misses the case where the clock is borked on construction;
       in the ctor we set the initial local to the physical clock, so this only catches drift
       when updating the local. it also needs some thought such that if multiple nodes are stuck
       on fwd/back physical times and just bumping counters they don't clash.
        */
      if (physicalNowTooFarAhead(physicalNow, localTime)) {
        localCounter.incrementAndGet();
        traceForwardDrift(physicalNow, localTime.get());
      } else {
        // things look sane, take the physical logicalTime and reset the counter
        localTime.set(physicalNow);
        localCounter.set(INITIAL_COUNTER);
      }
    }

    return new HybridLogicalTimestamp(localTime.get(), localCounter.get());
  }

  @Override
  public HybridLogicalTimestamp nextVersionUpdate(HybridLogicalTimestamp incoming) {
    final long physicalNow = this.clock.timestampMicros();

    if (physicalNow > localTime.get() && physicalNow > incoming.logicalTime()) {
      if (!physicalNowTooFarAhead(physicalNow, localTime)) {
        localTime.set(physicalNow);
        localCounter.set(INITIAL_COUNTER);
      } else {
        // don't use our physical logicalTime, it's too far ahead
        traceForwardDrift(physicalNow, localTime.get());
        resolveLocalAndIncomingTimestamps(incoming);
      }
    } else {
      // don't use our physical logicalTime, it's either same as or behind us
      traceBackwardDrift(physicalNow, localTime.get(), incoming.logicalTime());
      resolveLocalAndIncomingTimestamps(incoming);
    }

    return new HybridLogicalTimestamp(localTime.get(), localCounter.get());
  }

  private boolean physicalNowTooFarAhead(long physicalNow, AtomicLong logicalTime) {
    return physicalNow - logicalTime.get() > MAX_SYSTEM_TIME_FORWARD_DRIFT;
  }

  private void resolveLocalAndIncomingTimestamps(HybridLogicalTimestamp incoming) {
    if (localTime.get() > incoming.logicalTime()) {
      localCounter.incrementAndGet();
    } else if (localTime.get() < incoming.logicalTime()) {

      // todo: 2051 problem possible as there's no drift check, but log it
      if ((incoming.logicalTime() - localTime.get()) > MAX_INCOMING_TIME_FORWARD_DRIFT) {
        traceIncomingForwardDrift(localTime.get(), incoming.logicalTime());
      }

      localTime.set(incoming.logicalTime());
      localCounter.set(incoming.counter() + 1);
    } else { // times are equal, pick the highest counter and bump it
      if (incoming.counter() > localCounter.get()) {
        localCounter.set(incoming.counter() + 1);
      } else {
        localCounter.incrementAndGet();
      }
    }
  }

  private void traceForwardDrift(long physicalNow, long localTime) {
    logger.error(kvp("op", "version_timestamp",
        "err", "physical_time_drift_forward",
        "my_logical_time", "" + localTime,
        "my_physical_time", "" + physicalNow,
        "local_drift", "" + (physicalNow - localTime),
        "allowed_drift", "" + MAX_SYSTEM_TIME_FORWARD_DRIFT
    ));
  }

  private void traceIncomingForwardDrift(long localTime, long incomingTime) {
    logger.error(kvp("op", "version_timestamp",
        "err", "incoming_time_drift_forward",
        "my_logical_time", "" + localTime,
        "their_logical_time", "" + incomingTime,
        "relative_drift", "" + (incomingTime - localTime),
        "allowed_drift", "" + MAX_INCOMING_TIME_FORWARD_DRIFT
    ));
  }

  private void traceBackwardDrift(long physicalNow, long localTime, long incoming) {
    logger.error(kvp("op", "version_timestamp",
        "err", "physical_time_drift_backward",
        "my_logical_time", "" + localTime,
        "my_physical_time", "" + physicalNow,
        "their_logical_time", "" + incoming,
        "local_drift", "" + (localTime - physicalNow),
        "remote_drift", "" + (incoming - physicalNow)
    ));
  }

  private void traceBackwardDrift(long physicalNow, long localTime) {
    logger.error(kvp("op", "version_timestamp",
        "err", "physical_time_drift_backward",
        "my_logical_time", "" + localTime,
        "my_physical_time", "" + physicalNow,
        "local_drift", "" + (localTime - physicalNow)
    ));
  }

  static class SystemClock implements Clock {

    @Override
    public long timestampMicros() {
      /*
       todo: we might want to sample ntp servers for a hyparview based system,
        we can probably live with cTM for client/server
        */
      return System.currentTimeMillis() * 1000L;
    }
  }
}
