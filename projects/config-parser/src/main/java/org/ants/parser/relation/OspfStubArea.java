package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;
import java.util.Objects;

public class OspfStubArea implements Relation {
  public Node node;
  public long process;
  public long area;
  public boolean no_summary;
  public boolean default_originate;
  public boolean no_redis;
  public long metric;

  public static OspfStubArea stubArea(Node node, long process, long area, boolean no_summary) {
    OspfStubArea stubArea = new OspfStubArea();
    stubArea.node = node;
    stubArea.process = process;
    stubArea.area = area;
    stubArea.no_summary = no_summary;
    stubArea.default_originate = true;
    stubArea.no_redis = true;
    stubArea.metric = 0;

    return stubArea;
  }

  public static OspfStubArea nssaArea(
      Node node,
      long process,
      long area,
      boolean no_summary,
      boolean default_originate,
      boolean no_redis) {
    OspfStubArea nssaArea = new OspfStubArea();
    nssaArea.node = node;
    nssaArea.process = process;
    nssaArea.area = area;
    nssaArea.no_summary = no_summary;
    nssaArea.default_originate = default_originate;
    nssaArea.no_redis = no_redis;
    nssaArea.metric = 0;

    return nssaArea;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfStubArea)) {
      return false;
    }
    OspfStubArea that = (OspfStubArea) o;
    return process == that.process
        && area == that.area
        && no_summary == that.no_summary
        && default_originate == that.default_originate
        && no_redis == that.no_redis
        && metric == that.metric
        && Objects.equals(node, that.node);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, process, area, no_summary, default_originate, no_redis, metric);
  }

  @Override
  public String toString() {
    return String.format(
        "OspfStubArea(%s,%d,%d,%b,%b,%b,%d),\n",
        node, process, area, no_summary, default_originate, no_redis, metric);
  }

  @Override
  public void insertRecord(routingUpdateBuilder builder) {
    builder.insert_OspfStubArea(
        node.DType(builder), process, area, no_summary, default_originate, no_redis, metric);
  }

  @Override
  public void deleteRecord(routingUpdateBuilder builder) {
    builder.delete_OspfStubArea(
        node.DType(builder), process, area, no_summary, default_originate, no_redis, metric);
  }
}
