package org.ants.parser.relation.routeMap.matchCondition;

import java.util.Set;
import org.ants.parser.datamodel.Prefix;

public class MatchPrefixList extends MatchCondition {

  public Set<Prefix> prefixSet;

  public MatchPrefixList(Set<Prefix> prefixes) {
    this.prefixSet = prefixes;
  }

  @Override
  public String toString() {
    return String.format("MatchPrefixList{%s}", prefixSet);
  }
}
