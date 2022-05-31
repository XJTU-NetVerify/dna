package org.ants.parser.relation.routeMap;

import java.util.List;

public class RouteMap {

  public List<Policy> policies;

  public RouteMap(List<Policy> policies) {
    this.policies = policies;
  }

  public List<Policy> getPolicy(){
    return policies;
  }
}
