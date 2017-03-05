package outland.feature;

import java.util.concurrent.ThreadLocalRandom;

class ResourceSupport {

  private static final String HEADER_ACCEPT = "Accept";
  private static final String HEADER_X_FLOW_ID = "X-Flow-Id";

  static String nextFlowId() {
    return String.format("ojc_%d_%016x", System.currentTimeMillis(),
        ThreadLocalRandom.current().nextLong());
  }

  public static ResourceOptions options(String accept) {
    return new ResourceOptions()
        .header(HEADER_ACCEPT, accept)
        .header(HEADER_X_FLOW_ID, ResourceSupport.nextFlowId());
  }
}
