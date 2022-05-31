package org.ants.parser.relation.routeMap.setAttribute;

public class SetLocalPref extends SetAttribute{
  public long localPref;

  public SetLocalPref(long localPref) {
    this.localPref = localPref;
  }

  @Override public String toString() {
    // SetLocalPref{350}
    return String.format("SetLocalPref{%d}",localPref);
  }
}
