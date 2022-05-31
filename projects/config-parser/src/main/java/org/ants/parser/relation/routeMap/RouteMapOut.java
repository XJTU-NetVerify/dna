package org.ants.parser.relation.routeMap;

import ddlog.routing.routingUpdateBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.ants.parser.relation.Node;
import org.ants.parser.relation.Relation;
import org.ants.parser.relation.RelationWrapper;

public class RouteMapOut implements Relation {
  public Node node;
  public Node to_node;
  public List<Policy> policy;

  public RouteMapOut(Node node, Node to_node, List<Policy> policy) {
    this.node = node;
    this.to_node = to_node;
    this.policy = policy;
  }

  public List<Policy> getPolicy() {
    return policy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RouteMapOut)) {
      return false;
    }
    RouteMapOut that = (RouteMapOut) o;
    return Objects.equals(node, that.node)
        && Objects.equals(to_node, that.to_node)
        && Objects.equals(policy, that.policy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, to_node, policy);
  }

  @Override
  public String toString() {
    return String.format("RouteMapOut(%s,%s,%s),\n", node, to_node, new ArrayList<>(policy));
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    builder.insert_RouteMapOut(
        node.DType(builder), to_node.DType(builder), RelationWrapper.policyWrapper(policy, builder));
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    builder.delete_RouteMapOut(
        node.DType(builder), to_node.DType(builder), RelationWrapper.policyWrapper(policy, builder));
  }
}
