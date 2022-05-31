package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;
import java.util.Objects;
import org.ants.parser.datamodel.Prefix;

public class BgpAggregation implements Relation {
  public Node node;
  public Prefix prefix;

  public BgpAggregation(Node node, Prefix prefix) {
    this.node = node;
    this.prefix = prefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpAggregation)) {
      return false;
    }
    BgpAggregation that = (BgpAggregation) o;
    return Objects.equals(node, that.node) && Objects.equals(prefix, that.prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, prefix);
  }

  @Override
  public String toString() {
    // insert BgpAggregation("border1", Prefix{Ip{2,128,0,0}, Ip{255,255,0,0}}),
    return String.format("BgpAggregation(%s, %s),\n", node, prefix);
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    builder.insert_BgpAggregation(node.DType(builder), prefix.Dtype(builder));
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    builder.delete_BgpAggregation(node.DType(builder), prefix.Dtype(builder));
  }
}
