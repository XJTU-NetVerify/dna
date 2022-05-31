package org.ants.parser.relation.routeMap.setAttribute;

import org.batfish.datamodel.Ip;

public class SetNhop extends SetAttribute{
  Ip nhip;

  public SetNhop(Ip nhip) {
    this.nhip = nhip;
  }

  @Override
  public String toString() {
    // SetMed{50}
    return String.format("SetNextHopIp{%s}", nhip.toString().replace('.',','));
  }
}
