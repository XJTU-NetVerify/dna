package org.ants.parser.relation;

import ddlog.routing.routingUpdateBuilder;

public interface Relation {
  void insertRecord(routingUpdateBuilder builder);
  void deleteRecord(routingUpdateBuilder builder);
}
