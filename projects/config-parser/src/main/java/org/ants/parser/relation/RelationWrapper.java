package org.ants.parser.relation;

import ddlog.routing.*;
import org.ants.parser.datamodel.DCommunity;
import org.ants.parser.datamodel.Prefix;
import org.ants.parser.relation.routeMap.Policy;
import org.ants.parser.relation.routeMap.matchCondition.*;
import org.ants.parser.relation.routeMap.setAttribute.SetAttribute;
import org.ants.parser.relation.routeMap.setAttribute.SetCommunity;
import org.ants.parser.relation.routeMap.setAttribute.SetLocalPref;
import org.ants.parser.relation.routeMap.setAttribute.SetMed;

import java.util.*;

public class RelationWrapper {
  public static ip_tWriter ipWrapper(String rawIp, routingUpdateBuilder builder) {
    Integer[] ip = Arrays.stream(rawIp.split("\\.")).map(Integer::parseInt).toArray(Integer[]::new);
    return builder.create_ip_t(ip[0], ip[1], ip[2], ip[3]);
  }

  public static prefix_tWriter prefixWrapper(Prefix rawPrefix, routingUpdateBuilder builder) {
    ip_tWriter addr = ipWrapper(rawPrefix.ip, builder);
    ip_tWriter mask = ipWrapper(rawPrefix.mask, builder);
    return builder.create_prefix_t(addr, mask);
  }

  public static List<route_map_tWriter> policyWrapper(
      List<Policy> rms, routingUpdateBuilder builder) {
    List<route_map_tWriter> routeMaps = new ArrayList<>();
    for (Policy rm : rms) {
      List<match_attr_tWriter> matchAttrs = new ArrayList<>();
      List<set_attr_tWriter> setAttrs = new ArrayList<>();

      if (rm.matchConditions != null) {
        Map<String, MatchCondition> matchConditionMap = rm.matchConditions;
        for (String clazz : matchConditionMap.keySet()) {
          MatchCondition mc = matchConditionMap.get(clazz);
          switch (clazz) {
            case "MatchPrefixList":
              Set<Prefix> prefixes = ((MatchPrefixList) mc).prefixSet;
              List<prefix_list_tWriter> prefix_list_tWriterList = new ArrayList<>();
              for (Prefix prefix : prefixes) {
                if (prefix instanceof CommonPrefix) {
                  CommonPrefix commonPrefix = (CommonPrefix) prefix;
                  prefix_list_tWriterList.add(commonPrefix.DType(builder));
                } else if (prefix instanceof ExtendPrefix) {
                  ExtendPrefix extendPrefix = (ExtendPrefix) prefix;
                  prefix_list_tWriterList.add(extendPrefix.DType(builder));
                }
              }
              matchAttrs.add(builder.create_MatchPrefixList(prefix_list_tWriterList));
              break;

            case "MatchCommunity":
              DCommunity cmt = ((MatchCommunity) mc).communitySet.get(0);
              matchAttrs.add(
                      builder.create_MatchCommunity(builder.create_community_t(cmt.as, cmt.tag)));
              break;

            default:
              System.out.println("unsupported match condition: " + mc.getClass());
          }
        }
      }

      if (rm.setAttributes != null) {
        Map<String, SetAttribute> setAttributeMap = rm.setAttributes;
        for (String clazz : setAttributeMap.keySet()) {
          SetAttribute sa = setAttributeMap.get(clazz);
          switch (sa.getClass().getSimpleName()) {
            case "SetLocalPref":
              long localPref = ((SetLocalPref) sa).localPref;
              setAttrs.add(builder.create_SetLocalPref(localPref));
              break;

            case "SetMed":
              long metric = ((SetMed) sa).metric;
              setAttrs.add(builder.create_SetMed(metric));
              break;

            case "SetCommunity":
              DCommunity cmt = ((SetCommunity) sa).community;
              setAttrs.add(
                  builder.create_SetCommunity(true, builder.create_community_t(cmt.as, cmt.tag)));
              break;

            default:
              System.out.println("unsupported set attribute: " + sa.getClass());
          }
        }
      }
      routeMaps.add(builder.create_route_map_t(rm.permit, matchAttrs, setAttrs));
    }
    return routeMaps;
  }
}
