package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;
import java.util.Objects;
import org.ants.parser.datamodel.Prefix;
import org.ants.parser.utils.IpHelper;

public class BgpNetwork implements Relation {
  public Node node;
  public Prefix prefix;

  public BgpNetwork(Node node, String prefix) {
    this.node = node;
    this.prefix = new Prefix(prefix);
  }

  public Node getNode() {
    return node;
  }

  public String getFormatPrefix() {
    long maskNum = IpHelper.ipToNumber(prefix.mask);

    int length = Long.bitCount(maskNum);

    return prefix.ip + "/" + length;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpNetwork)) {
      return false;
    }
    BgpNetwork that = (BgpNetwork) o;
    return Objects.equals(node, that.node) && Objects.equals(prefix, that.prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, prefix);
  }

  @Override
  public String toString() {
    return String.format("BgpNetwork(%s, %s),\n", node, prefix);
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    builder.insert_BgpNetwork(node.DType(builder), prefix.Dtype(builder));
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    builder.delete_BgpNetwork(node.DType(builder), prefix.Dtype(builder));
  }
}
