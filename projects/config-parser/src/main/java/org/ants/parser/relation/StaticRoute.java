package org.ants.parser.relation;

import ddlog.routing.ip_tWriter;
import ddlog.routing.prefix_tWriter;
import ddlog.routing.routingUpdateBuilder;
import org.ants.parser.datamodel.Prefix;

import java.util.Objects;

public class StaticRoute implements Relation {
  public Node node;
  public Prefix prefix;
  public String nhip;
  public int adminCost;

  public StaticRoute(Node node, String prefix, String nhip, int adminCost) {
    this.node = node;
    this.prefix = new Prefix(prefix);
    this.nhip = nhip.equals("AUTO/NONE(-1l)") ? "0.0.0.0" : nhip;
    this.adminCost = adminCost;
  }

  public static String proc(String ip) {
    return ip.replace('.', ',');
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StaticRoute)) {
      return false;
    }
    StaticRoute that = (StaticRoute) o;
    return adminCost == that.adminCost && Objects.equals(node, that.node) && Objects.equals(
        prefix,
        that.prefix) && Objects.equals(nhip, that.nhip);
  }

  @Override public int hashCode() {
    return Objects.hash(node, prefix, nhip, adminCost);
  }

  @Override
  public String toString() {
    return String.format("StaticRoute(%s, %s, Ip{%s}, %d),\n", node, prefix, proc(nhip), adminCost);
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    prefix_tWriter prefix_t = RelationWrapper.prefixWrapper(prefix, builder);
    ip_tWriter ip_t = RelationWrapper.ipWrapper(nhip, builder);
    builder.insert_StaticRoute(node.DType(builder), prefix_t, ip_t, adminCost);
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    ip_tWriter ip_t = RelationWrapper.ipWrapper(nhip, builder);
    builder.delete_StaticRoute(node.DType(builder), prefix.Dtype(builder), ip_t, adminCost);
  }
}
