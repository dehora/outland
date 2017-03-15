package outland.cluster.proto;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.1.2)",
    comments = "Source: outland-cluster.proto")
public class OutlandServiceGrpc {

  private OutlandServiceGrpc() {}

  public static final String SERVICE_NAME = "outland.OutlandService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<outland.cluster.proto.JoinMessage,
      outland.cluster.proto.Response> METHOD_JOIN_REQUEST =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "outland.OutlandService", "JoinRequest"),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.JoinMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Response.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<outland.cluster.proto.JoinReplyMessage,
      outland.cluster.proto.Response> METHOD_JOIN_REPLY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "outland.OutlandService", "JoinReply"),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.JoinReplyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Response.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<outland.cluster.proto.ForwardJoinMessage,
      outland.cluster.proto.Response> METHOD_FORWARD_JOIN_REQUEST =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "outland.OutlandService", "ForwardJoinRequest"),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.ForwardJoinMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Response.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<outland.cluster.proto.DisconnectMessage,
      outland.cluster.proto.Response> METHOD_DISCONNECT_REQUEST =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "outland.OutlandService", "DisconnectRequest"),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.DisconnectMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Response.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<outland.cluster.proto.NeighborMessage,
      outland.cluster.proto.NeighbourReplyMessage> METHOD_NEIGHBOR_REQUEST =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "outland.OutlandService", "NeighborRequest"),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.NeighborMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.NeighbourReplyMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<outland.cluster.proto.ShuffleMessage,
      outland.cluster.proto.Response> METHOD_SHUFFLE_REQUEST =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "outland.OutlandService", "ShuffleRequest"),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.ShuffleMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Response.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<outland.cluster.proto.ShuffleReplyMessage,
      outland.cluster.proto.Response> METHOD_SHUFFLE_REPLY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "outland.OutlandService", "ShuffleReply"),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.ShuffleReplyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Response.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<outland.cluster.proto.Rouk,
      outland.cluster.proto.Imok> METHOD_RUOK =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "outland.OutlandService", "Ruok"),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Rouk.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Imok.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<outland.cluster.proto.Rouk,
      outland.cluster.proto.Imok> METHOD_RUOKS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING,
          generateFullMethodName(
              "outland.OutlandService", "Ruoks"),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Rouk.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(outland.cluster.proto.Imok.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OutlandServiceStub newStub(io.grpc.Channel channel) {
    return new OutlandServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OutlandServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new OutlandServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static OutlandServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new OutlandServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class OutlandServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void joinRequest(outland.cluster.proto.JoinMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_JOIN_REQUEST, responseObserver);
    }

    /**
     */
    public void joinReply(outland.cluster.proto.JoinReplyMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_JOIN_REPLY, responseObserver);
    }

    /**
     */
    public void forwardJoinRequest(outland.cluster.proto.ForwardJoinMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_FORWARD_JOIN_REQUEST, responseObserver);
    }

    /**
     */
    public void disconnectRequest(outland.cluster.proto.DisconnectMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DISCONNECT_REQUEST, responseObserver);
    }

    /**
     */
    public void neighborRequest(outland.cluster.proto.NeighborMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.NeighbourReplyMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_NEIGHBOR_REQUEST, responseObserver);
    }

    /**
     */
    public void shuffleRequest(outland.cluster.proto.ShuffleMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SHUFFLE_REQUEST, responseObserver);
    }

    /**
     */
    public void shuffleReply(outland.cluster.proto.ShuffleReplyMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SHUFFLE_REPLY, responseObserver);
    }

    /**
     */
    public void ruok(outland.cluster.proto.Rouk request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Imok> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_RUOK, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<outland.cluster.proto.Rouk> ruoks(
        io.grpc.stub.StreamObserver<outland.cluster.proto.Imok> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_RUOKS, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_JOIN_REQUEST,
            asyncUnaryCall(
              new MethodHandlers<
                outland.cluster.proto.JoinMessage,
                outland.cluster.proto.Response>(
                  this, METHODID_JOIN_REQUEST)))
          .addMethod(
            METHOD_JOIN_REPLY,
            asyncUnaryCall(
              new MethodHandlers<
                outland.cluster.proto.JoinReplyMessage,
                outland.cluster.proto.Response>(
                  this, METHODID_JOIN_REPLY)))
          .addMethod(
            METHOD_FORWARD_JOIN_REQUEST,
            asyncUnaryCall(
              new MethodHandlers<
                outland.cluster.proto.ForwardJoinMessage,
                outland.cluster.proto.Response>(
                  this, METHODID_FORWARD_JOIN_REQUEST)))
          .addMethod(
            METHOD_DISCONNECT_REQUEST,
            asyncUnaryCall(
              new MethodHandlers<
                outland.cluster.proto.DisconnectMessage,
                outland.cluster.proto.Response>(
                  this, METHODID_DISCONNECT_REQUEST)))
          .addMethod(
            METHOD_NEIGHBOR_REQUEST,
            asyncUnaryCall(
              new MethodHandlers<
                outland.cluster.proto.NeighborMessage,
                outland.cluster.proto.NeighbourReplyMessage>(
                  this, METHODID_NEIGHBOR_REQUEST)))
          .addMethod(
            METHOD_SHUFFLE_REQUEST,
            asyncUnaryCall(
              new MethodHandlers<
                outland.cluster.proto.ShuffleMessage,
                outland.cluster.proto.Response>(
                  this, METHODID_SHUFFLE_REQUEST)))
          .addMethod(
            METHOD_SHUFFLE_REPLY,
            asyncUnaryCall(
              new MethodHandlers<
                outland.cluster.proto.ShuffleReplyMessage,
                outland.cluster.proto.Response>(
                  this, METHODID_SHUFFLE_REPLY)))
          .addMethod(
            METHOD_RUOK,
            asyncUnaryCall(
              new MethodHandlers<
                outland.cluster.proto.Rouk,
                outland.cluster.proto.Imok>(
                  this, METHODID_RUOK)))
          .addMethod(
            METHOD_RUOKS,
            asyncBidiStreamingCall(
              new MethodHandlers<
                outland.cluster.proto.Rouk,
                outland.cluster.proto.Imok>(
                  this, METHODID_RUOKS)))
          .build();
    }
  }

  /**
   */
  public static final class OutlandServiceStub extends io.grpc.stub.AbstractStub<OutlandServiceStub> {
    private OutlandServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OutlandServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OutlandServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OutlandServiceStub(channel, callOptions);
    }

    /**
     */
    public void joinRequest(outland.cluster.proto.JoinMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_JOIN_REQUEST, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void joinReply(outland.cluster.proto.JoinReplyMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_JOIN_REPLY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void forwardJoinRequest(outland.cluster.proto.ForwardJoinMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_FORWARD_JOIN_REQUEST, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void disconnectRequest(outland.cluster.proto.DisconnectMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DISCONNECT_REQUEST, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void neighborRequest(outland.cluster.proto.NeighborMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.NeighbourReplyMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_NEIGHBOR_REQUEST, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void shuffleRequest(outland.cluster.proto.ShuffleMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SHUFFLE_REQUEST, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void shuffleReply(outland.cluster.proto.ShuffleReplyMessage request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Response> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SHUFFLE_REPLY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void ruok(outland.cluster.proto.Rouk request,
        io.grpc.stub.StreamObserver<outland.cluster.proto.Imok> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_RUOK, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<outland.cluster.proto.Rouk> ruoks(
        io.grpc.stub.StreamObserver<outland.cluster.proto.Imok> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_RUOKS, getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class OutlandServiceBlockingStub extends io.grpc.stub.AbstractStub<OutlandServiceBlockingStub> {
    private OutlandServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OutlandServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OutlandServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OutlandServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public outland.cluster.proto.Response joinRequest(outland.cluster.proto.JoinMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_JOIN_REQUEST, getCallOptions(), request);
    }

    /**
     */
    public outland.cluster.proto.Response joinReply(outland.cluster.proto.JoinReplyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_JOIN_REPLY, getCallOptions(), request);
    }

    /**
     */
    public outland.cluster.proto.Response forwardJoinRequest(outland.cluster.proto.ForwardJoinMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_FORWARD_JOIN_REQUEST, getCallOptions(), request);
    }

    /**
     */
    public outland.cluster.proto.Response disconnectRequest(outland.cluster.proto.DisconnectMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DISCONNECT_REQUEST, getCallOptions(), request);
    }

    /**
     */
    public outland.cluster.proto.NeighbourReplyMessage neighborRequest(outland.cluster.proto.NeighborMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_NEIGHBOR_REQUEST, getCallOptions(), request);
    }

    /**
     */
    public outland.cluster.proto.Response shuffleRequest(outland.cluster.proto.ShuffleMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SHUFFLE_REQUEST, getCallOptions(), request);
    }

    /**
     */
    public outland.cluster.proto.Response shuffleReply(outland.cluster.proto.ShuffleReplyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SHUFFLE_REPLY, getCallOptions(), request);
    }

    /**
     */
    public outland.cluster.proto.Imok ruok(outland.cluster.proto.Rouk request) {
      return blockingUnaryCall(
          getChannel(), METHOD_RUOK, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class OutlandServiceFutureStub extends io.grpc.stub.AbstractStub<OutlandServiceFutureStub> {
    private OutlandServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OutlandServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OutlandServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OutlandServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<outland.cluster.proto.Response> joinRequest(
        outland.cluster.proto.JoinMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_JOIN_REQUEST, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<outland.cluster.proto.Response> joinReply(
        outland.cluster.proto.JoinReplyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_JOIN_REPLY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<outland.cluster.proto.Response> forwardJoinRequest(
        outland.cluster.proto.ForwardJoinMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_FORWARD_JOIN_REQUEST, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<outland.cluster.proto.Response> disconnectRequest(
        outland.cluster.proto.DisconnectMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DISCONNECT_REQUEST, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<outland.cluster.proto.NeighbourReplyMessage> neighborRequest(
        outland.cluster.proto.NeighborMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_NEIGHBOR_REQUEST, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<outland.cluster.proto.Response> shuffleRequest(
        outland.cluster.proto.ShuffleMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SHUFFLE_REQUEST, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<outland.cluster.proto.Response> shuffleReply(
        outland.cluster.proto.ShuffleReplyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SHUFFLE_REPLY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<outland.cluster.proto.Imok> ruok(
        outland.cluster.proto.Rouk request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_RUOK, getCallOptions()), request);
    }
  }

  private static final int METHODID_JOIN_REQUEST = 0;
  private static final int METHODID_JOIN_REPLY = 1;
  private static final int METHODID_FORWARD_JOIN_REQUEST = 2;
  private static final int METHODID_DISCONNECT_REQUEST = 3;
  private static final int METHODID_NEIGHBOR_REQUEST = 4;
  private static final int METHODID_SHUFFLE_REQUEST = 5;
  private static final int METHODID_SHUFFLE_REPLY = 6;
  private static final int METHODID_RUOK = 7;
  private static final int METHODID_RUOKS = 8;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final OutlandServiceImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(OutlandServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_JOIN_REQUEST:
          serviceImpl.joinRequest((outland.cluster.proto.JoinMessage) request,
              (io.grpc.stub.StreamObserver<outland.cluster.proto.Response>) responseObserver);
          break;
        case METHODID_JOIN_REPLY:
          serviceImpl.joinReply((outland.cluster.proto.JoinReplyMessage) request,
              (io.grpc.stub.StreamObserver<outland.cluster.proto.Response>) responseObserver);
          break;
        case METHODID_FORWARD_JOIN_REQUEST:
          serviceImpl.forwardJoinRequest((outland.cluster.proto.ForwardJoinMessage) request,
              (io.grpc.stub.StreamObserver<outland.cluster.proto.Response>) responseObserver);
          break;
        case METHODID_DISCONNECT_REQUEST:
          serviceImpl.disconnectRequest((outland.cluster.proto.DisconnectMessage) request,
              (io.grpc.stub.StreamObserver<outland.cluster.proto.Response>) responseObserver);
          break;
        case METHODID_NEIGHBOR_REQUEST:
          serviceImpl.neighborRequest((outland.cluster.proto.NeighborMessage) request,
              (io.grpc.stub.StreamObserver<outland.cluster.proto.NeighbourReplyMessage>) responseObserver);
          break;
        case METHODID_SHUFFLE_REQUEST:
          serviceImpl.shuffleRequest((outland.cluster.proto.ShuffleMessage) request,
              (io.grpc.stub.StreamObserver<outland.cluster.proto.Response>) responseObserver);
          break;
        case METHODID_SHUFFLE_REPLY:
          serviceImpl.shuffleReply((outland.cluster.proto.ShuffleReplyMessage) request,
              (io.grpc.stub.StreamObserver<outland.cluster.proto.Response>) responseObserver);
          break;
        case METHODID_RUOK:
          serviceImpl.ruok((outland.cluster.proto.Rouk) request,
              (io.grpc.stub.StreamObserver<outland.cluster.proto.Imok>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RUOKS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.ruoks(
              (io.grpc.stub.StreamObserver<outland.cluster.proto.Imok>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class OutlandServiceDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return outland.cluster.proto.ClusterMessage.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (OutlandServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OutlandServiceDescriptorSupplier())
              .addMethod(METHOD_JOIN_REQUEST)
              .addMethod(METHOD_JOIN_REPLY)
              .addMethod(METHOD_FORWARD_JOIN_REQUEST)
              .addMethod(METHOD_DISCONNECT_REQUEST)
              .addMethod(METHOD_NEIGHBOR_REQUEST)
              .addMethod(METHOD_SHUFFLE_REQUEST)
              .addMethod(METHOD_SHUFFLE_REPLY)
              .addMethod(METHOD_RUOK)
              .addMethod(METHOD_RUOKS)
              .build();
        }
      }
    }
    return result;
  }
}
