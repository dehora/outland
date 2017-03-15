import java.util.logging.Level;
import outland.cluster.node.Outland;

public class OutlandClusterExampleMain {

  public static void main(String[] args) throws Exception {

    // quieten grpc: https://github.com/grpc/grpc-java/issues/1577
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger("io.grpc");
    logger.setLevel(Level.WARNING);

    String hostName = "localhost";

    // spin up a bunch of hyparview nodes and seed them with 3 nodes for discovery
    buildNode(hostName, 5001, "localhost:5003", "localhost:5002").start();
    buildNode(hostName, 5002, "localhost:5001", "localhost:5003").start();
    buildNode(hostName, 5003, "localhost:5001", "localhost:5002").start();
    buildNode(hostName, 5004, "localhost:5001", "localhost:5002").start();
    buildNode(hostName, 5005, "localhost:5001", "localhost:5002").start();
    buildNode(hostName, 5006, "localhost:5001", "localhost:5002").start();
    buildNode(hostName, 5007, "localhost:5001", "localhost:5002").start();
    buildNode(hostName, 5008, "localhost:5001", "localhost:5002").start();

    // let's pretend time passed before new nodes want to join (i have no idea what i'm doing)
    Thread.sleep(3000);

    System.out.println("Adding more nodes");

    // spin up a bunch more nodes that are joining "later on"
    for (int portNo = 5010; portNo < 5020; portNo++) {
      Outland node = buildNode(hostName, portNo, "localhost:5001", "localhost:5002");
      logger.info("starting new node " + hostName + ":" + portNo);
      node.start();
    }

    // run forever, watching the log output for cluster magic
    System.out.println("running nodes");
  }

  private static Outland buildNode(String hostName, int port, String... seeds) {
    return Outland.newBuilder()
        .withHostPort(hostName + ":" + port)
        .withId("outland" + port)
        .withSeeds(seeds)
        .build();
  }
}
