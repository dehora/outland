package outland.cluster.node;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Outland {

  private final String id;
  private final String hostPort;
  private final List<String> seeds;
  private OutlandBoot boot;

  public Outland(String id, String hostPort, List<String> seeds) {
    this.id = id;
    this.hostPort = hostPort;
    this.seeds = seeds;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public void start() {
    boot = new OutlandBoot(id, hostPort, seeds);
    boot.start();
  }

  @SuppressWarnings("unused")
  public void stop() {
    boot.stop();
  }

  public static class Builder {

    private String id;
    private String hostPort;
    private List<String> contactSeeds;

    public Builder withId(String id) {
      this.id = id;
      return this;
    }

    public Builder withHostPort(String hostPort) {
      this.hostPort = hostPort;
      return this;
    }

    public Builder withSeeds(String... seeds) {
      contactSeeds = Lists.newArrayList();
      Collections.addAll(contactSeeds, seeds);
      return this;
    }

    public Outland build() {
      return new Outland(id, hostPort, contactSeeds);
    }
  }
}
