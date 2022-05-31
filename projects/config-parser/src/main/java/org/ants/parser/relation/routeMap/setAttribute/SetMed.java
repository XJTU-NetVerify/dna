package org.ants.parser.relation.routeMap.setAttribute;

public class SetMed extends SetAttribute{
  public long metric;

  public SetMed(long metric) {
    this.metric = metric;
  }

  @Override public String toString(){
    // SetMed{50}
    return String.format("SetMed{%d}",metric);
  }
}
