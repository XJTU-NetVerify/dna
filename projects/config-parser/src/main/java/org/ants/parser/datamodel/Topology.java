package org.ants.parser.datamodel;

import org.ants.parser.relation.ConnectedRoute;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.collections.NodeInterfacePair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Topology {
  public static Map<NodeInterfacePair, NodeInterfacePair> l1Topology(
      Map<String, Configuration> configs) {
    Set<ConnectedRoute> allLinks = new HashSet<>();
    Map<NodeInterfacePair, NodeInterfacePair> topology = new HashMap<>();

    for (String node : configs.keySet()) {
      Configuration config = configs.get(node);
      for (Interface intf : config.getAllInterfaces().values()) {
        if (intf.getActive()) {
          for (ConcreteInterfaceAddress cid : intf.getAllConcreteAddresses()) {
            allLinks.add(new ConnectedRoute(node, cid.getPrefix().toString(), intf.getName()));
          }
        }
      }
    }

    for (ConnectedRoute c1 : allLinks) {
      for (ConnectedRoute c2 : allLinks) {
        if (!c1.getNode().equals(c2.getNode()) && c1.getPrefix().equals(c2.getPrefix())) {
          topology.put(
              NodeInterfacePair.of(c1.getNode(), c1.getIntf()),
              NodeInterfacePair.of(c2.getNode(), c2.getIntf()));
        }
      }
    }
    return topology;
  }

  public static Map<NodeInterfacePair, NodeInterfacePair> ospfTopology(
      Map<String, Configuration> configs) {
    Map<NodeInterfacePair, NodeInterfacePair> topology = l1Topology(configs);
    Map<NodeInterfacePair, NodeInterfacePair> ospfTopology = new HashMap<>();

    for (NodeInterfacePair nip1 : topology.keySet()) {
      Interface intf1 = configs.get(nip1.getHostname()).getAllInterfaces().get(nip1.getInterface());
      NodeInterfacePair nip2 = topology.get(nip1);
      Interface intf2 = configs.get(nip2.getHostname()).getAllInterfaces().get(nip2.getInterface());
      if(intf1.getOspfAreaName()!=null && intf2.getOspfAreaName()!=null){
        if (intf1.getOspfAreaName().equals(intf2.getOspfAreaName())) {
          if (!intf1.getOspfPassive() && !intf2.getOspfPassive()) {
            ospfTopology.put(nip1, nip2);
          }
        }
      }
    }
    System.out.println(ospfTopology.size());
    return ospfTopology;
  }
}
