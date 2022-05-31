package org.ants.parser.relation.routeMap;

import ddlog.routing.routingUpdateBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.ants.parser.relation.Node;
import org.ants.parser.relation.Relation;
import org.ants.parser.relation.RelationWrapper;

public class RouteMapIn implements Relation {
  public Node node;
  public Node from_node;
  public List<Policy> policy;

  public RouteMapIn(Node node, Node from_node, List<Policy> policy) {
    this.node = node;
    this.from_node = from_node;
    this.policy = policy;
  }

  @Override
  public RouteMapIn clone() throws CloneNotSupportedException {
    return (RouteMapIn) super.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RouteMapIn)) {
      return false;
    }
    RouteMapIn that = (RouteMapIn) o;
    return Objects.equals(node, that.node)
        && Objects.equals(from_node, that.from_node)
        && Objects.equals(policy, that.policy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, from_node, policy);
  }

  @Override
  public String toString() {
    return String.format("RouteMapIn(%s,%s,%s),\n", node, from_node, new ArrayList<>(policy));
  }

  public List<Policy> getPolicy() {
    return policy;
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    builder.insert_RouteMapIn(
        node.DType(builder), from_node.DType(builder), RelationWrapper.policyWrapper(policy, builder));
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    builder.delete_RouteMapIn(
        node.DType(builder), from_node.DType(builder), RelationWrapper.policyWrapper(policy, builder));
  }
}
