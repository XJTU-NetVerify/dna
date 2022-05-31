package org.ants.parser.relation.neighbor;

public class L3Links {
  public String node1;
  public String intf1;
  public String node2;
  public String intf2;

  public L3Links(String node1, String intf1, String node2, String intf2) {
    this.node1 = node1;
    this.intf1 = intf1;
    this.node2 = node2;
    this.intf2 = intf2;
  }

  @Override
  public String toString() {
    return String.format(
        "L3Link(\"%s\",\"%s\",\"%s\",\"%s\"),\n", node1, intf1, node2, intf2);
  }
}
