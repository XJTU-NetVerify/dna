package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;
import java.util.Objects;

public class BgpMultipath implements Relation {
  public Node node;
  public int num;
  public boolean relax;

  public BgpMultipath(Node node, int num, boolean relax) {
    this.node = node;
    this.num = num;
    this.relax = relax;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BgpMultipath)) return false;
    BgpMultipath that = (BgpMultipath) o;
    return num == that.num && relax == that.relax && Objects.equals(node, that.node);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, num, relax);
  }

  @Override
  public String toString() {
    return String.format("BgpMultipath(%s, %d, %b),\n", node, num, relax);
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    if (num != -1) {
      builder.insert_BgpMultipath(node.DType(builder), num, relax);
    }
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    if (num != -1) {
      builder.delete_BgpMultipath(node.DType(builder), num, relax);
    }
  }
}
