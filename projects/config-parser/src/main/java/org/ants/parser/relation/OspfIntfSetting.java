package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;
import java.util.Objects;

public class OspfIntfSetting implements Relation {
  public Node node;
  public String intf;
  public int cost;
  public long area;
  public boolean passive;
  public long process;

  public OspfIntfSetting(Node node, String intf) {
    this.node = node;
    this.intf = intf;
  }

  public OspfIntfSetting(Node node, String intf, int cost, long area, boolean passive, long process) {
    this.node = node;
    this.intf = intf;
    this.cost = cost;
    this.area = area;
    this.passive = passive;
    this.process = process;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OspfIntfSetting that = (OspfIntfSetting) o;
    return cost == that.cost
        && area == that.area
        && passive == that.passive
        && process == that.process
        && Objects.equals(node, that.node)
        && Objects.equals(intf, that.intf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, intf, cost, area, passive, process);
  }

  @Override
  public String toString() {
    return String.format(
        "OspfIntfSetting(%s,\"%s\",%d,%d,%b,%d),\n", node, intf, cost, area, passive, process);
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    builder.insert_OspfIntfSetting(node.DType(builder), intf, cost, area, passive, process);
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    builder.delete_OspfIntfSetting(node.DType(builder), intf, cost, area, passive, process);
  }
}
