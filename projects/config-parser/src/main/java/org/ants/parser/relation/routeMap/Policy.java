package org.ants.parser.relation.routeMap;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import org.ants.parser.relation.routeMap.matchCondition.MatchCondition;
import org.ants.parser.relation.routeMap.setAttribute.SetAttribute;

public class Policy {
  public boolean permit;
  public Map<String, MatchCondition> matchConditions;
  public Map<String, SetAttribute> setAttributes;

  public Policy(
      boolean permit,
      Map<String, MatchCondition> matchConditions,
      Map<String, SetAttribute> setAttributes) {
    this.permit = permit;
    this.matchConditions = matchConditions;
    this.setAttributes = setAttributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Policy)) {
      return false;
    }
    Policy routeMap = (Policy) o;
    return permit == routeMap.permit
        && Objects.equals(matchConditions, routeMap.matchConditions)
        && Objects.equals(setAttributes, routeMap.setAttributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permit, matchConditions, setAttributes);
  }

  @Override
  public String toString() {
    // format e.g.: RouteMap{true, [MatchPrefix{"2.1.1.1/32"}], [SetCommunity{true, 1}]}
    String matchs =
        matchConditions == null ? "[]" : new ArrayList<>(matchConditions.values()).toString();
    String sets = setAttributes == null ? "[]" : new ArrayList<>(setAttributes.values()).toString();
    return String.format("RouteMap{%b, %s, %s}",permit, matchs, sets);
  }


}
