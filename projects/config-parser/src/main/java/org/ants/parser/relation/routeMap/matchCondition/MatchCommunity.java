package org.ants.parser.relation.routeMap.matchCondition;

import java.util.List;
import org.ants.parser.datamodel.DCommunity;

public class MatchCommunity extends MatchCondition {
  public List<DCommunity> communitySet;

  public MatchCommunity(List<DCommunity> communitySet) {
    this.communitySet = communitySet;
  }

  @Override
  public String toString() {
    // MatchCommunity{3}
    return String.format("MatchCommunity{%s}", communitySet.get(0));
  }
}
