package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;
import ddlog.routing.vnode_tWriter;
import java.util.Objects;

public class Node implements Relation {
  public String node;
  public String vrf;
  public long as;
  public long id;

  public Node(String node, String vrf) {
    this.node = node;
    this.vrf = vrf;
  }

  public Node(Node vnode, long as, long id) {
    this.node = vnode.node;
    this.vrf = vnode.vrf;
    this.as = as;
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Node)) {
      return false;
    }
    Node node1 = (Node) o;
    return as == node1.as
        && id == node1.id
        && Objects.equals(node, node1.node)
        && Objects.equals(vrf, node1.vrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, vrf, as, id);
  }

  @Override
  public String toString() {
    return String.format("VNode{\"%s\",\"%s\"}", node, vrf);
  }

  public String toDATString() {
    return String.format("Node(%s,%d,%d),\n", this, as, id);
  }

  public vnode_tWriter DType(routingUpdateBuilder builder) {
    return builder.create_vnode_t(node, vrf);
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    if (as != -1) builder.insert_Node(DType(builder), as, id);
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    if (as != -1) builder.delete_Node(DType(builder), as, id);
  }
}
