package outland.feature.server.features;

import com.google.common.base.MoreObjects;

public interface VersionService {

  HybridLogicalTimestamp nextVersion();

  HybridLogicalTimestamp nextVersionUpdate(HybridLogicalTimestamp incoming);

  interface Clock {
    long timestampMicros();
  }

  class HybridLogicalTimestamp {
    private final long logicalTime;
    private final long counter;

    HybridLogicalTimestamp(long logicalTime, long counter) {
      this.logicalTime = logicalTime;
      this.counter = counter;
    }

    public long logicalTime() {
      return logicalTime;
    }

    public long counter() {
      return counter;
    }

    String id() {
      return "ver_" + Ulid.generate(
          asTimestampMillis(),
          ("localTime=" + logicalTime + ",counter=" + counter).getBytes());
    }

    long asTimestampMicros() {
      // 16 and 48 are taken from https://www.cse.buffalo.edu/tech-reports/2014-04.pdf
      return (logicalTime >> 16 << 16) | (counter << 48 >> 48);
    }

    long asTimestampMillis() {
      return asTimestampMicros() / 1000L;
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("logicalTime", logicalTime)
          .add("counter", counter)
          .toString();
    }
  }
}
