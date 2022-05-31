package org.ants.parser.relation;

import ddlog.routing.prefix_tWriter;
import ddlog.routing.routingUpdateBuilder;
import org.ants.parser.datamodel.Prefix;

import java.util.Objects;

public class Interface implements Relation{
  public Node node;
  public String intf;
  public String ip;
  public Prefix prefix;

  public Interface(Node node, String intf, String ip, String prefix) {
    this.node = node;
    this.intf = intf;
    this.ip = ip;
    this.prefix = new Prefix(ip, prefix);
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Interface)) {
      return false;
    }
    Interface that = (Interface) o;
    return Objects.equals(node, that.node) && Objects.equals(intf, that.intf) && Objects.equals(
        ip,
        that.ip) && Objects.equals(prefix, that.prefix);
  }

  @Override public int hashCode() {
    return Objects.hash(node, intf, ip, prefix);
  }

  @Override
  public String toString() {
    return String.format("Interface(%s, \"%s\", %s),\n", node, intf, prefix);
  }

  @Override public void insertRecord(routingUpdateBuilder builder) {
    prefix_tWriter prefix_t = RelationWrapper.prefixWrapper(prefix, builder);
    builder.insert_Interface(node.DType(builder), intf, prefix_t);
  }

  @Override public void deleteRecord(routingUpdateBuilder builder) {
    prefix_tWriter prefix_t = RelationWrapper.prefixWrapper(prefix, builder);
    builder.delete_Interface(node.DType(builder), intf, prefix_t);
  }
}
