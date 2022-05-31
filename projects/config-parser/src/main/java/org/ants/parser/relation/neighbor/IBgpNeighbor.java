package org.ants.parser.relation.neighbor;

import ddlog.routing.ip_tWriter;
import ddlog.routing.routingUpdateBuilder;
import org.ants.parser.relation.Node;
import org.ants.parser.relation.Relation;
import org.ants.parser.relation.RelationWrapper;

public class IBgpNeighbor extends Neighbor implements Relation {

  public IBgpNeighbor(Node node1, String ip1, Node node2, String ip2) {
    super(node1, ip1, node2, ip2);
  }

  @Override
  public String toString() {
    return "IBgpNeighbor" + super.toString();
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    ip_tWriter ip1_w = RelationWrapper.ipWrapper(ip1, builder);
    ip_tWriter ip2_w = RelationWrapper.ipWrapper(ip2, builder);
    builder.insert_IBgpNeighbor(node1.DType(builder), ip1_w, node2.DType(builder), ip2_w);
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    ip_tWriter ip1_w = RelationWrapper.ipWrapper(ip1, builder);
    ip_tWriter ip2_w = RelationWrapper.ipWrapper(ip2, builder);
    builder.delete_IBgpNeighbor(node1.DType(builder), ip1_w, node2.DType(builder), ip2_w);
  }
}
