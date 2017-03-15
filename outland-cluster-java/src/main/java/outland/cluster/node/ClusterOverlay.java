package outland.cluster.node;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.cluster.proto.Node;

class ClusterOverlay {

  private static final Logger logger = LoggerFactory.getLogger("outland-overlay");
  private static final Logger viewLogger = LoggerFactory.getLogger("outland-views");
  private static final int ACTIVE_VIEW_MAX = 5; // paper: 5
  private static final int PASSIVE_VIEW_MAX = ACTIVE_VIEW_MAX * 6; // paper: 30
  private static final List<Node> SENTINEL_NODES = Lists.newArrayList();

  private final Map<String, Node> activeMap = Maps.newHashMap();
  private final Map<String, Node> passiveMap = Maps.newHashMap();
  private final String id;
  private final Node localNode;

  ClusterOverlay(String id, Node localNode) {
    this.id = id;
    this.localNode = localNode;
  }

  void moveFromActiveToPassiveView(Node node) {
    Preconditions.checkNotNull(node);
    synchronized (this) {
      removeFromActiveView(node);
      addToPassiveView(node);
    }
    logger.debug("id={} moveFromActiveToPassiveView view {}", id, toString());
  }

  void addToActiveView(Node node) {
    Preconditions.checkNotNull(node);
    if (localNode.equals(node)) {
      return;
    }

    synchronized (this) {
      if (activeViewContains(node)) {
        return;
      }
      removeFromPassiveView(node); // not in paper, symmetry is only implied
      activeMap.put(node.getId(), node);
    }
  }

  void addToPassiveView(Node node) {
    Preconditions.checkNotNull(node);
    if (localNode.equals(node)) {
      return;
    }

    synchronized (this) {
      removeFromActiveView(node); // not in paper, symmetry is only implied
      if (passiveViewIsFull()) {
        // todo: A node will first attempt to remove identifiers sent to the peer
        dropRandomNodeFromPassiveView();
      }
      passiveMap.put(node.getId(), node);
    }
  }

  boolean passiveViewIsFull() {
    synchronized (this) {
      return passiveMap.size() >= PASSIVE_VIEW_MAX;
    }
  }

  boolean passiveViewContains(Node node) {
    Preconditions.checkNotNull(node);
    synchronized (this) {
      return activeMap.containsKey(node.getId());
    }
  }

  boolean activeViewIsEmpty() {
    synchronized (this) {
      return activeMap.isEmpty();
    }
  }

  boolean activeViewIsFull() {
    synchronized (this) {
      return activeMap.size() >= ACTIVE_VIEW_MAX;
    }
  }

  boolean activeViewContains(Node node) {
    Preconditions.checkNotNull(node);
    synchronized (this) {
      return activeMap.containsKey(node.getId());
    }
  }

  Optional<Node> tryDropRandomNodeFromActiveView() {
    final Optional<Node> node = getRandomNodeFromActiveView();
    synchronized (this) {
      if (node.isPresent()) {
        moveFromActiveToPassiveView(node.get());
        logger.debug("id={} dropRandomNodeFromActiveView {}", id, node.get().getId());
      }
      return node;
    }
  }

  Optional<Node> getNonMatchingRandomNodeFromActiveView(Node node) {

    HashMap<String, Node> copy;
    synchronized (this) {
      if (activeMap.isEmpty()) {
        return Optional.empty();
      }
      copy = Maps.newHashMap(activeMap);
    }

    return getNonMatchingNodeFromView(node, copy);
  }

  Optional<Node> getNonMatchingRandomNodeFromPassiveView(Node node) {

    HashMap<String, Node> copy;
    synchronized (this) {
      if (passiveViewIsEmpty()) {
        return Optional.empty();
      }
      copy = Maps.newHashMap(passiveMap);
    }

    return getNonMatchingNodeFromView(node, copy);
  }

  private Optional<Node> getNonMatchingNodeFromView(Node node, HashMap<String, Node> copy) {
    final List<String> collect =
        copy.keySet().stream()
            .filter(n -> !node.getId().equals(n)).collect(Collectors.toList());

    if (collect.isEmpty()) {
      return Optional.empty();
    }

    final Node found =
        copy.get(collect.get(ThreadLocalRandom.current().nextInt(0, collect.size())));
    copy.clear();
    collect.clear();
    return Optional.ofNullable(found);
  }

  Optional<Node> getRandomNodeFromActiveView() {
    Map<String, Node> copy;
    synchronized (this) {
      if (activeViewIsEmpty()) {
        return Optional.empty();
      }
      copy = ImmutableMap.copyOf(activeMap);
    }

    if (copy.isEmpty()) {
      return Optional.empty();
    }

    // channelCount racy, make a copy; todo: fix to ensure invariants between active/passive
    return getRandomValueFromMap(copy);
  }

  Optional<Node> getRandomNodeFromPassiveView() {
    Map<String, Node> copy;
    synchronized (this) {
      if (passiveViewIsEmpty()) {
        return Optional.empty();
      }
      try {
        copy = ImmutableMap.copyOf(passiveMap);
      } catch (ConcurrentModificationException e) {
        copy = ImmutableMap.copyOf(passiveMap);
      }
    }
    return getRandomValueFromMap(copy);
  }

  int getActiveViewSize() {
    return activeMap.size();
  }

  List<Node> getRandomNodesFromActiveView(int num) {
    HashMap<String, Node> copy;
    synchronized (this) {
      if (activeMap.isEmpty()) {
        return SENTINEL_NODES;
      }
      copy = Maps.newHashMap(activeMap);
    }
    return getRandomNodes(num, copy);
  }

  List<Node> getRandomNodesFromPassiveView(int num) {
    HashMap<String, Node> copy;
    synchronized (this) {
      if (passiveViewIsEmpty()) {
        return SENTINEL_NODES;
      }
      copy = Maps.newHashMap(activeMap);
    }
    return getRandomNodes(num, copy);
  }

  void dropRandomNodeFromPassiveView() {
    getRandomNodeFromPassiveView().ifPresent(n -> passiveMap.remove(n.getId()));
  }

  Set<Node> getActiveViewWithout(Node node) {
    final Collection<Node> values;
    synchronized (this) {
      values = ImmutableSet.copyOf(activeMap.values());
    }
    return values.stream()
        .filter(n -> !node.equals(n))
        .collect(Collectors.toSet());
  }

  void logViews() {
    if (viewLogger.isInfoEnabled()) {
      String views = toString();
      viewLogger.info("id={} overlay {}", localNode.getId(), views);
    }
  }

  public String toString() {
    Set<String> active;
    Set<String> passive;
    // todo: sometimes throws ccme
    synchronized (this) {
      active = ImmutableSet.copyOf(activeMap.keySet());
      passive = ImmutableSet.copyOf(passiveMap.keySet());
    }
    return "a=" + active.size()
        + " p=" + passive.size()
        + " active_view=" + active.stream().collect(Collectors.toList())
        + " passive_view=" + passive.stream().collect(Collectors.toList());
  }

  private void removeFromActiveView(Node node) {
    Preconditions.checkNotNull(node);
    activeMap.remove(node.getId());
  }

  private boolean passiveViewIsEmpty() {
    return passiveMap.isEmpty();
  }

  private void removeFromPassiveView(Node node) {
    Preconditions.checkNotNull(node);
    passiveMap.remove(node.getId());
  }

  private List<Node> getRandomNodes(int num, Map<String, Node> copy) {
    final ArrayList<Node> list = Lists.newArrayList(copy.values());
    Collections.shuffle(list);
    final ArrayList<Node> result = Lists.newArrayList();
    for (Node node : list) {
      result.add(node);
      if (result.size() == num) {
        break;
      }
    }
    copy.clear();
    list.clear();
    return result;
  }

  private Optional<Node> getRandomValueFromMap(Map<String, Node> local) {
    final List<String> keys = local.keySet().stream().collect(Collectors.toList());
    final Node value = local.get(keys.get(ThreadLocalRandom.current().nextInt(0, local.size())));
    return Optional.ofNullable(value);
  }
}
