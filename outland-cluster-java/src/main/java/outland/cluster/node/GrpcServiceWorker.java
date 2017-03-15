package outland.cluster.node;

import com.google.common.base.Preconditions;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
public class GrpcServiceWorker implements Runnable {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger("outland");

  private final String id;
  private final String hostname;
  private final int port;
  private final ClusterCoordinator clusterCoordinator;
  private Server server;

  public GrpcServiceWorker(String id, String hostname, int port,
      ClusterCoordinator clusterCoordinator) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(clusterCoordinator);

    this.id = id;
    this.hostname = hostname;
    this.port = port;
    this.clusterCoordinator = clusterCoordinator;
  }

  public void stop() {
    logger.info("id={} stopping node", id);
    if (server != null) {
      server.shutdown();
      logger.debug("worker {} stopped, was listening on {}", id, port);
    }
  }

  @Override public void run() {
    startBackgroundServer(clusterCoordinator);
  }

  private void startBackgroundServer(ClusterCoordinator coordinator) {
    startGrpcService(coordinator);
    coordinator.initialize();
    blockOnGrpcServiceUntilShutdown();
  }

  private void startGrpcService(ServiceHandler handler) {
    try {
      server = ServerBuilder.forPort(port)
          .addService(new GrpcService(id, handler))
          .build()
          .start();

      logger.info("id={} grpc_server_started {}:{}", id, hostname, port);

      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          logger.debug("id={} grpc_server_stopping, jvm is shutting down", id);
          GrpcServiceWorker.this.stop();
          handler.logViews();
          logger.debug("id={} grpc_server_stopped for jvm shutdown", id);
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void blockOnGrpcServiceUntilShutdown() {
    try {
      if (server != null) {
        server.awaitTermination();
      }
    } catch (InterruptedException e) {
      logger.info("id={} interrupted, re-interrupting", id);
      Thread.currentThread().interrupt();
    }
  }
}
