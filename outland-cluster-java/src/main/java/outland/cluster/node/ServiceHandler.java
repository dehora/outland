package outland.cluster.node;

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

interface ServiceHandler {

  Response handleJoinRequest(JoinMessage request);

  Response handleForwardJoinRequest(ForwardJoinMessage request);

  Response handleDisconnectRequest(DisconnectMessage request);

  NeighbourReplyMessage handleNeighborRequest(NeighborMessage request);

  Response handleShuffleRequest(ShuffleMessage request);

  Response handleShuffleReply(ShuffleReplyMessage request);

  Imok handleRuok(Rouk request);

  void logViews();
}
