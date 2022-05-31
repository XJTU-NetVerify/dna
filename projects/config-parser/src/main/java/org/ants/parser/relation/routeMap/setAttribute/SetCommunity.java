package org.ants.parser.relation.routeMap.setAttribute;

import org.ants.parser.datamodel.DCommunity;

public class SetCommunity extends SetAttribute{
  public boolean additive;
  public DCommunity community;

  public SetCommunity(boolean additive, DCommunity community) {
    this.additive = additive;
    this.community = community;
  }

  @Override public String toString(){
    // SetCommunity{true,65538}
    return String.format("SetCommunity{%b,%s}",additive,community);
  }
}
