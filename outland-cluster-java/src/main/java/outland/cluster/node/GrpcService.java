package outland.cluster.node;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import org.slf4j.LoggerFactory;
import outland.cluster.proto.DisconnectMessage;
import outland.cluster.proto.ForwardJoinMessage;
import outland.cluster.proto.Imok;
import outland.cluster.proto.JoinMessage;
import outland.cluster.proto.NeighborMessage;
import outland.cluster.proto.NeighbourReplyMessage;
import outland.cluster.proto.Response;
import outland.cluster.proto.Rouk;
import outland.cluster.proto.ShuffleMessage;
import outland.cluster.proto.ShuffleReplyMessage;
import outland.cluster.proto.OutlandServiceGrpc;

@SuppressWarnings("WeakerAccess")
public class GrpcService extends OutlandServiceGrpc.OutlandServiceImplBase {

  public static final String EMPTY_STRING = "";
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger("outland-grpc");
  private final String id;
  private final ServiceHandler handler;

  public GrpcService(String id, ServiceHandler handler) {
    this.id = id;
    this.handler = handler;
  }

  @Override
  public void joinRequest(JoinMessage request, StreamObserver<Response> responseObserver) {
    logger.debug("{} gRPC joinRequest {}", id, toJson(request));
    responseObserver.onNext(handler.handleJoinRequest(request));
    responseObserver.onCompleted();
  }

  @Override public void forwardJoinRequest(ForwardJoinMessage request,
      StreamObserver<Response> responseObserver) {
    logger.debug("{} gRPC handleForwardJoinRequest {}", id, toJson(request));
    responseObserver.onNext(handler.handleForwardJoinRequest(request));
    responseObserver.onCompleted();
  }

  @Override public void disconnectRequest(DisconnectMessage request,
      StreamObserver<Response> responseObserver) {
    logger.debug("{} gRPC disconnectRequest {}", id, toJson(request));
    responseObserver.onNext(handler.handleDisconnectRequest(request));
    responseObserver.onCompleted();
  }

  @Override
  public void neighborRequest(NeighborMessage request,
      StreamObserver<NeighbourReplyMessage> responseObserver) {
    logger.debug("{} gRPC neighborRequest {}", id, toJson(request));
    responseObserver.onNext(handler.handleNeighborRequest(request));
    responseObserver.onCompleted();
  }

  @Override
  public void shuffleRequest(ShuffleMessage request, StreamObserver<Response> responseObserver) {
    logger.debug("{} gRPC shuffleRequest {}", id, toJson(request));
    responseObserver.onNext(handler.handleShuffleRequest(request));
    responseObserver.onCompleted();
  }

  @Override
  public void shuffleReply(ShuffleReplyMessage request, StreamObserver<Response> responseObserver) {
    logger.debug("{} gRPC shuffleReply {}", id, toJson(request));
    responseObserver.onNext(handler.handleShuffleReply(request));
    responseObserver.onCompleted();
  }

  @Override public void ruok(Rouk request, StreamObserver<Imok> responseObserver) {
    logger.debug("{} gRPC ruok {}", id, toJson(request));
    responseObserver.onNext(handler.handleRuok(request));
    responseObserver.onCompleted();
  }

  private String toJson(GeneratedMessageV3 message) {
    if (logger.isDebugEnabled()) {
      try {
        return JsonFormat.printer().omittingInsignificantWhitespace().print(message);
      } catch (InvalidProtocolBufferException e) {
        return message.toString();
      }
    } else {
      return EMPTY_STRING;
    }
  }
}
