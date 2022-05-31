package org.ants.parser.relation.routeMap.matchCondition;

import ddlog.routing.prefix_list_tWriter;
import ddlog.routing.routingUpdateBuilder;
import org.ants.parser.datamodel.Prefix;

public class CommonPrefix extends Prefix {

  public CommonPrefix(String prefixText) {
    super(prefixText);
  }

  @Override public String toString() {
    return String.format("CommonPrefix{%s}", super.toString());
  }

  public prefix_list_tWriter DType(routingUpdateBuilder builder){
    return builder.create_CommonPrefix(super.Dtype(builder));
  }
}
