package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;
import java.util.Objects;

public class OspfRedis implements Relation {
  public Node node;
  public boolean enable;
  public String protocol;
  public long processId;

  public OspfRedis(Node node, boolean enable, String protocol, long processId) {
    this.node = node;
    this.enable = enable;
    this.protocol = protocol;
    this.processId = processId;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfRedis)) {
      return false;
    }
    OspfRedis ospfRedis = (OspfRedis) o;
    return enable == ospfRedis.enable && Objects.equals(node, ospfRedis.node) && Objects.equals(
        protocol,
        ospfRedis.protocol);
  }

  @Override public int hashCode() {
    return Objects.hash(node, enable, protocol);
  }

  @Override public String toString() {
    return String.format("OspfRedis(%s,\"%s\", %d),\n", node, protocol,processId);
  }

  @Override public void insertRecord(routingUpdateBuilder builder) {
    if (enable) {
      builder.insert_OspfRedis(node.DType(builder), protocol, processId);
    }
  }

  @Override public void deleteRecord(routingUpdateBuilder builder) {
    if(enable){
      builder.delete_OspfRedis(node.DType(builder), protocol, processId);
    }
  }
}
