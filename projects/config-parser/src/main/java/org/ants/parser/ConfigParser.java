package org.ants.parser;

import javafx.util.Pair;
import org.ants.parser.datamodel.Device;
import org.ants.parser.relation.ConnectedRoute;
import org.ants.parser.relation.Interface;
import org.ants.parser.relation.Node;
import org.ants.parser.relation.Relation;
import org.ants.parser.utils.IpHelper;
import org.batfish.common.topology.IpOwners;
import org.batfish.datamodel.*;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.collections.NodeInterfacePair;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/*
  From Batfish vendor-independent models to Datalog input relations
 */
public class ConfigParser {
    private BatfishAPI batfishAPI;

    private Map<NodeInterfacePair, NodeInterfacePair> l1Topology;
    private Map<String, Map<String, Object>> dpDeviceModels;
    private Map<String, Map<String, Device>> cpDeviceModels; // node name -> vrf -> Device
    private static Map<String, Map<String, Device>> lastSnapShot;

    public ConfigParser(String testcase, String configPath, String workingPath) throws IOException {
        l1Topology = new HashMap<>();
        dpDeviceModels = new HashMap<>();
        cpDeviceModels = new HashMap<>();
        lastSnapShot = new HashMap<>();
        batfishAPI = new BatfishAPI(testcase, workingPath);
        updateConfig(configPath);
    }

    public void updateConfig() throws IOException{
        parse(batfishAPI.parse());
    }

    public void updateConfig(String configPath) throws IOException{
        parse(batfishAPI.parse(configPath));
    }

    public void parse(Map<String, Configuration> configs) {
        generateL1Topology(configs);
        generateDataPlaneModel(configs);
        generateControlPlaneModel(configs);
    }

    private void generateL1Topology(Map<String, Configuration> configs) {
        l1Topology = new HashMap<>();

        Set<ConnectedRoute> allLinks = new HashSet<>();
        for (Map.Entry<String, Configuration> entry : configs.entrySet()) {
            String nodeName = entry.getKey();
            Configuration config = entry.getValue();
            for (org.batfish.datamodel.Interface intf : config.getAllInterfaces().values()) {
                if (intf.getActive()) {
                    for (ConcreteInterfaceAddress cid : intf.getAllConcreteAddresses()) {
                        allLinks.add(new ConnectedRoute(nodeName, cid.getPrefix().toString(), intf.getName()));
                    }
                }
            }
        }

        for (ConnectedRoute c1 : allLinks) {
            for (ConnectedRoute c2 : allLinks) {
                if (!c1.getNode().equals(c2.getNode()) && c1.getPrefix().equals(c2.getPrefix())) {
                    l1Topology.put(
                            NodeInterfacePair.of(c1.getNode(), c1.getIntf()),
                            NodeInterfacePair.of(c2.getNode(), c2.getIntf()));
                }
            }
        }
    }

    public Map<String, Object> computeVlansValue(
            Set<Integer> vlanList,
            Map<Integer, Set<String>> accessPorts,
            Map<Integer, Map<String, List<String>>> acls,
            Map<Integer, Map<String, String>> ip,
            Map<Integer, Set<String>> trunkPorts) {
        Map<String, Object> vlansValue = new HashMap<>(); // key:vlan_ID
        for (Integer vlanID : vlanList) {
            Map<String, Object> vlanValue = new HashMap<>();
            if (accessPorts.containsKey(vlanID)) {
                vlanValue.put("access ports", accessPorts.get(vlanID));
            } else {
                vlanValue.put("access ports", new HashSet<>());
            }

            if (acls.containsKey(vlanID)) {
                vlanValue.put("acls", acls.get(vlanID));
            } else {
                vlanValue.put("acls", null);
            }

            if (ip.containsKey(vlanID)) {
                vlanValue.put("ip", ip.get(vlanID));
            } else {
                vlanValue.put("ip", null);
            }

            if (trunkPorts.containsKey(vlanID)) {
                vlanValue.put("trunk ports", trunkPorts.get(vlanID));
            } else {
                vlanValue.put("trunk ports", new ArrayList<>());
            }

            vlanValue.put("lldp", new ArrayList<>());
            vlanValue.put("policy", new HashSet<>());

            vlansValue.put("Vlan" + vlanID, vlanValue);
        }
        return vlansValue;
    }

    public Map<String, Object> computeVlan(Configuration nodeConfig) {
        Map<String, org.batfish.datamodel.Interface> intfs = nodeConfig.getActiveInterfaces();
        Map<String, Object> vlansValue = new HashMap<>(); // key:vlan_ID

        Set<Integer> vlanList = new HashSet<>();
        // vlanID -> List<interfaceName>
        Map<Integer, Set<String>> accessPorts = new HashMap<>();
        // vlanID -> in/out -> List<aclName>
        Map<Integer, Map<String, List<String>>> acls = new HashMap<>();
        // vlanID -> prefix/mask -> ipv4Adress
        Map<Integer, Map<String, String>> ip = new HashMap<>();
        // vlanID -> List<interfaceName>
        Map<Integer, Set<String>> trunkPorts = new HashMap<>();

        for (String intfName : intfs.keySet()) {
            org.batfish.datamodel.Interface intf = intfs.get(intfName);

            if (intf.getInterfaceType() == InterfaceType.VLAN) { // acls,ip
                if (intf.getVlan() != null) {
                    vlanList.add(intf.getVlan());

                    Map<String, List<String>> aclsValue = new HashMap<>(); // key:acls
                    if (intf.getIncomingFilter() != null) {
                        List<String> inACLs = new ArrayList<>();
                        inACLs.add(intf.getIncomingFilterName());
                        aclsValue.put("in", inACLs);
                    }
                    if (intf.getOutgoingFilter() != null) {
                        List<String> outACLs = new ArrayList<>();
                        outACLs.add(intf.getOutgoingFilterName());
                        aclsValue.put("out", outACLs);
                    }
                    acls.put(intf.getVlan(), aclsValue);

                    if (intf.getConcreteAddress() != null) { // key:ip
                        Map<String, String> ipValue = new HashMap<>();
                        ConcreteInterfaceAddress cadress = intf.getConcreteAddress();
                        String prefix = cadress.getIp().toString();
                        String mask = IpHelper.bitsToIP(cadress.getNetworkBits());
                        ipValue.put("prefix", prefix);
                        ipValue.put("mask", mask);
                        ip.put(intf.getVlan(), ipValue);
                    } else {
                        ip.put(intf.getVlan(), new HashMap<>());
                    }
                }

            } else if (intf.getSwitchportMode() == SwitchportMode.ACCESS) { // access_ports
                if (intf.getAccessVlan() != null) {
                    vlanList.add(intf.getAccessVlan());
                    Integer vlanID = intf.getAccessVlan();
                    if (accessPorts.containsKey(vlanID)) {
                        accessPorts.get(vlanID).add(intfName);
                    } else {
                        Set<String> ports = new HashSet<>();
                        ports.add(intfName);
                        accessPorts.put(intf.getAccessVlan(), ports);
                    }
                }
            } else if (intf.getSwitchportMode() == SwitchportMode.TRUNK) {
                Set<Integer> allowVlans = intf.getAllowedVlans().enumerate();
                if (allowVlans.size() != 0) {

                    vlanList.addAll(allowVlans);

                    for (Integer vlanID : allowVlans) {
                        if (trunkPorts.containsKey(vlanID)) {
                            trunkPorts.get(vlanID).add(intfName);
                        } else {
                            Set<String> ports = new HashSet<>();
                            ports.add(intfName);
                            trunkPorts.put(vlanID, ports);
                        }
                    }
                }
            }
        }

        vlansValue = computeVlansValue(vlanList, accessPorts, acls, ip, trunkPorts);

        return vlansValue;
    }

    public String processACL(IpAccessListLine ipAccessListLine, int priority) throws Exception {
        List<String> rule = new ArrayList<>();
        // action:permit/deny
        boolean permit = true;
        if (ipAccessListLine.getAction() == LineAction.PERMIT) {
            rule.add("permit");
        } else if (ipAccessListLine.getAction() == LineAction.DENY) {
            // permit = false;
            rule.add("deny");
        }

        AclLineMatchExpr aclLineMatchExpr = ipAccessListLine.getMatchCondition();
        if (aclLineMatchExpr instanceof MatchHeaderSpace) {
            MatchHeaderSpace matchHeaderSpace = (MatchHeaderSpace) aclLineMatchExpr;
            HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
            // protocol number range: permit-> ipProtocol, deny -> notIpProtocol
            if (headerSpace.getNotIpProtocols().size() != 0) {
                throw new Exception("notIpProtocol expression is not supported!");
            }
            if (headerSpace.getIpProtocols().size() != 0) {
                Set<Integer> ipProtocols =
                        headerSpace.getIpProtocols().stream().map(p -> p.number()).collect(Collectors.toSet());
                rule.add(Collections.min(ipProtocols).toString());
                rule.add(Collections.max(ipProtocols).toString());
            } else {
                rule.add("null");
                rule.add("null");
            }
            if (headerSpace.getSrcIps() != null) {
                IpSpace srcIps = headerSpace.getSrcIps();
                if (srcIps instanceof IpWildcardIpSpace) {
                    IpWildcardIpSpace _srcIps = (IpWildcardIpSpace) srcIps;
                    rule.add(_srcIps.getIpWildcard().getIp().toString());
                    String s = Long.toBinaryString(_srcIps.getIpWildcard().getMask());
                    rule.add(IpHelper.bitsToIP(s.lastIndexOf('1') + 1));
                }
            } else {
                rule.add("null");
                rule.add("null");
            }

            if (headerSpace.getSrcPorts().size() != 0) {
                SortedSet<SubRange> srcPorts = headerSpace.getSrcPorts();
                rule.add(String.valueOf(srcPorts.first().getStart()));
                rule.add(String.valueOf(srcPorts.last().getEnd()));
            } else {
                rule.add("null");
                rule.add("null");
            }

            // dst Ip,mask,port
            if (headerSpace.getDstIps() != null) {
                IpSpace dstIps = headerSpace.getDstIps();
                if (dstIps instanceof IpWildcardIpSpace) {
                    IpWildcardIpSpace _dstIps = (IpWildcardIpSpace) dstIps;
                    rule.add(_dstIps.getIpWildcard().getIp().toString());
                    String s = Long.toBinaryString(_dstIps.getIpWildcard().getMask());
                    // rule.add(IpHelper.numberToIp(_dstIps.getIpWildcard().getMask()));
                    rule.add(IpHelper.bitsToIP(s.lastIndexOf('1') + 1));
                }
            } else {
                rule.add("null");
                rule.add("null");
            }

            if (headerSpace.getDstPorts().size() != 0) {
                SortedSet<SubRange> dstPorts = headerSpace.getDstPorts();
                rule.add(String.valueOf(dstPorts.first().getStart()));
                rule.add(String.valueOf(dstPorts.last().getEnd()));
            } else {
                rule.add("null");
                rule.add("null");
            }

            rule.add(String.valueOf(priority));

            StringBuilder sb = new StringBuilder();
            for (String ruleElem : rule) {
                sb.append(ruleElem).append(" ");
            }
            return sb.toString().trim();
        }

        return ipAccessListLine.getName() + " " + priority;
    }

    public List<String> computeACLRules(String aclName, IpAccessList ipAccessList)
            throws Exception {
        List<IpAccessListLine> ipAccessListLines = ipAccessList.getLines();
        List<String> rulesLines = new ArrayList<>();
        int priority = 65535;
        for (IpAccessListLine ipAccessListLine : ipAccessListLines) {
            rulesLines.add("access-list " + aclName + " " + processACL(ipAccessListLine, priority));
            priority--;
        }
        return rulesLines;
    }
    
    public Map<String, Object> computeACL(Configuration nodeConfig) {
        Map<String, IpAccessList> acls = nodeConfig.getIpAccessLists();
        Map<String, org.batfish.datamodel.Interface> allInterface = nodeConfig.getActiveInterfaces();

        Map<String, Object> aclsValue = new HashMap<>(); // key:aclName

        for (String aclName : acls.keySet()) {
            IpAccessList ipAccessList = acls.get(aclName);
            Map<String, Object> aclValue = new HashMap<>(); // key: rules,applications
            List<String> ruleLines = new ArrayList<>();
            try {
                ruleLines = computeACLRules(aclName, ipAccessList);
            } catch (Exception e) {
                e.printStackTrace();
            }

            aclValue.put("rules", ruleLines);
            List<Map<String, String>> applicationsValue = new ArrayList<>(); // [intfDirect,...]
            for (String intf : allInterface.keySet()) {
                Map<String, String> intfDirect = new HashMap<>(); // key: interface, direction
                IpAccessList inACL = allInterface.get(intf).getIncomingFilter();
                IpAccessList outACL = allInterface.get(intf).getOutgoingFilter();
                if (inACL != null && inACL.getName().equals(aclName)) { // some interface don't have ACL
                    intfDirect.put("interface", intf);
                    intfDirect.put("direction", "in");
                } else if (outACL != null && outACL.getName().equals(aclName)) {
                    intfDirect.put("interface", intf);
                    intfDirect.put("direction", "out");
                }

                if (!intfDirect.keySet().isEmpty()) {
                    applicationsValue.add(intfDirect);
                }
            }
            aclValue.put("applications", applicationsValue);
            aclsValue.put(aclName, aclValue);
        }
        return aclsValue;
    }

    public Map<String, Object> computeInterface(Configuration nodeConfig) {
        Map<String, org.batfish.datamodel.Interface> allInterface = nodeConfig.getActiveInterfaces();
        Map<String, Object> interfaceValue = new HashMap<>(); // key: interfaceName

        for (String intfName : allInterface.keySet()) {
            Map<String, Object> intfValue = new HashMap<>();

            org.batfish.datamodel.Interface intf = allInterface.get(intfName);
            String switchMode = "";
            List<String> vlanList = new ArrayList<>();

            if (intf.getSwitchport()) {
                if (intf.getSwitchportMode() == SwitchportMode.ACCESS) {
                    switchMode = "access";
                    vlanList.add("Vlan" + intf.getAccessVlan());
                } else if (intf.getSwitchportMode() == SwitchportMode.TRUNK) {
                    switchMode = "trunk";
                    Set<Integer> allowVlans = intf.getAllowedVlans().enumerate();
                    for (Integer i : allowVlans) {
                        vlanList.add("Vlan" + i);
                    }
                }
            }
            intfValue.put("mode", switchMode);
            intfValue.put("vlan", vlanList);

            Map<String, Object> aclsValue = new HashMap<>();
            IpAccessList inACL = intf.getIncomingFilter();
            if (inACL != null) {
                List<String> arr = new ArrayList<>();
                arr.add(inACL.getName());
                aclsValue.put("in", arr);
            }
            IpAccessList outACL = intf.getOutgoingFilter();
            if (outACL != null) {
                List<String> arr = new ArrayList<>();
                arr.add(outACL.getName());
                aclsValue.put("out", arr);
            }
            List<Map<String, Object>> aclsValueArr = new ArrayList<>();
            aclsValueArr.add(aclsValue);
            intfValue.put("acls", aclsValueArr);

            ConcreteInterfaceAddress intfAddress = intf.getConcreteAddress();
            Map<String, String> ipValue = new HashMap<>();
            if (intfAddress != null) {
                ipValue.put("prefix", intfAddress.getIp().toString());
                ipValue.put("mask", IpHelper.bitsToIP(intfAddress.getNetworkBits()));
                intfValue.put("ip", ipValue);
            } else {
                intfValue.put("ip", ipValue);
            }

            List<Object> lldpValue = new ArrayList<>();
            lldpValue.add(new Object());
            intfValue.put("lldp", lldpValue);

            List<Object> policyValue = new ArrayList<>();
            policyValue.add(new Object());
            intfValue.put("policy", policyValue);

            interfaceValue.put(intfName, intfValue);
        }

        return interfaceValue;
    }

    private void generateDataPlaneModel(Map<String, Configuration> configs) {
        dpDeviceModels = new HashMap<>();
        for (Map.Entry<String, Configuration> entry: configs.entrySet()) {
            String nodeName = entry.getKey();
            Configuration config = entry.getValue();

            Map<String, Object> vlansValue = computeVlan(config);
            List<Map<String, Object>> vlansValueArr = new ArrayList<>();
            vlansValueArr.add(vlansValue);
            dpDeviceModels.computeIfAbsent(nodeName, k -> new HashMap<>()).put("vlan", vlansValueArr);

            List<Object> classifierArr = new ArrayList<>();
            classifierArr.add(new Object());
            dpDeviceModels.get(nodeName).put("classifier", classifierArr);

            Map<String, Object> aclsValue = computeACL(config);
            List<Map<String, Object>> aclsValueArr = new ArrayList<>();
            aclsValueArr.add(aclsValue);
            dpDeviceModels.get(nodeName).put("acl", aclsValueArr);

            Map<String, Object> interfaceValue = computeInterface(config);
            List<Map<String, Object>> interfaceValueArr = new ArrayList<>();
            interfaceValueArr.add(interfaceValue);
            dpDeviceModels.get(nodeName).put("interface", interfaceValueArr);

            List<Object> behaviorArr = new ArrayList<>();
            behaviorArr.add(new Object());
            dpDeviceModels.get(nodeName).put("behavior", behaviorArr);

            List<Object> policyArr = new ArrayList<>();
            policyArr.add(new Object());
            dpDeviceModels.get(nodeName).put("policy", policyArr);

            List<Object> portArr = new ArrayList<>();
            portArr.add(new Object());
            dpDeviceModels.get(nodeName).put("port-channel", policyArr);
        }
    }

    private void generateControlPlaneModel(Map<String, Configuration> configs) {
        cpDeviceModels = new HashMap<>();

        Map<Ip, Map<String, Set<String>>> ipVrfOwners = new IpOwners(configs).getIpVrfOwners(); // ip -> hostname -> vrf
        for (Map.Entry<String, Configuration> configEntry: configs.entrySet()) {
            String nodeName = configEntry.getKey();
            Configuration config = configEntry.getValue();
            if (config.getDeviceType() == DeviceType.HOST) continue;

            for (Map.Entry<String, Vrf> vrfEntry : config.getVrfs().entrySet()) {
                String vrfName = vrfEntry.getKey();
                Vrf vrf = vrfEntry.getValue();
                Device device = new Device(new Node(nodeName, vrfName), vrf, config, ipVrfOwners);
                cpDeviceModels.computeIfAbsent(nodeName, k -> new HashMap<>()).put(vrfName, device);
            }
        }
    }

    public Map<String, List<Relation>> getControlPlaneDiff(Set<String> changedDevices) {
        // ALL devices may be changed
        if (changedDevices == null) {
            changedDevices = new HashSet<>();
            changedDevices.addAll(lastSnapShot.keySet());
            changedDevices.addAll(cpDeviceModels.keySet());
        }

        List<Relation> inserts = new ArrayList<>();
        List<Relation> deletes = new ArrayList<>();
        Map<String, List<Relation>> controlPlaneDiff = new HashMap<>();

        for(Map.Entry<String, Map<String, Device>> outerEntry : cpDeviceModels.entrySet()) {
            String nodeName = outerEntry.getKey();
            if(changedDevices.contains(nodeName)) {
                for(Map.Entry<String, Device> innerEntry : outerEntry.getValue().entrySet()) {
                    String vrfName = innerEntry.getKey();
                    Device curr = innerEntry.getValue();

                    if(lastSnapShot.containsKey(nodeName) && lastSnapShot.get(nodeName).containsKey(vrfName)) {
                        Device old = lastSnapShot.get(nodeName).get(vrfName);
                        Pair<List<Relation>, List<Relation>> diff = curr.compareTo(old);
                        inserts.addAll(diff.getKey());
                        deletes.addAll(diff.getValue());
                    } else {
                        inserts.addAll(curr.getDeviceInfo());
                    }
                }

                changedDevices.remove(nodeName);
            }
        }

        for(Map.Entry<String, Map<String, Device>> outerEntry : lastSnapShot.entrySet()) {
            String nodeName = outerEntry.getKey();
            if(changedDevices.contains(nodeName)) {
                for(Map.Entry<String, Device> innerEntry : outerEntry.getValue().entrySet()) {
                    Device curr = innerEntry.getValue();
                    deletes.addAll(curr.getDeviceInfo());
                }
            }
        }

        lastSnapShot = cpDeviceModels;
        controlPlaneDiff.put("insert", inserts);
        controlPlaneDiff.put("delete", deletes);

        return controlPlaneDiff;
    }

    public Map<String, List<Relation>> getControlPlane() {
        Map<String, List<Relation>> records = new HashMap<>();

        List<Relation> inserts = cpDeviceModels.values().stream().map(Map::values).flatMap(Collection::stream)
                .map(Device::getDeviceInfo).flatMap(Collection::stream).collect(Collectors.toList());
        records.put("insert", inserts);

        return records;
    }

    public String getDDlogInputText() {
        StringBuilder sb = new StringBuilder();

        sb.append(("start;\n"));
        getControlPlane().get("insert").forEach(relation -> {
            if (relation instanceof Node)
                sb.append(String.format("insert %s", ((Node) relation).toDATString()));
            else
                sb.append(String.format("insert %s", relation.toString()));
        });
        sb.append("commit;\ndump Fib;\n");

        return sb.toString();
    }

    public HashSet<String> getTopology() {
        HashSet<String> topology = new HashSet<>();

        for (Map.Entry<NodeInterfacePair, NodeInterfacePair> edge : l1Topology.entrySet()) {
            topology.add(String.format("%s %s %s %s",
                    edge.getKey().getHostname(), edge.getKey().getInterface(),
                    edge.getValue().getHostname(), edge.getValue().getInterface()));
        }

        return topology;
    }

    public HashSet<String> getEdgePorts() {
        HashSet<String> edgePorts = new HashSet<>();

        cpDeviceModels.values().stream().map(Map::values).flatMap(Collection::stream).forEach(device -> {
            Set<String> internalPorts = new HashSet<>();
            for (NodeInterfacePair pair : l1Topology.keySet()) {
                if (pair.getHostname().equals(device.getNode().node)) {
                    internalPorts.add(pair.getHostname() + " " + pair.getInterface());
                }
            }

            List<Interface> intfs = device.getIntfs();
            for (Interface intf : intfs) {
                String intfPort = intf.node.node + " " + intf.intf;
                // filter "LongReachEthernet0", seem to be the problem of parser
                if (!internalPorts.contains(intfPort) && !intf.intf.toLowerCase().contains("loopback") && !intf.intf.toLowerCase().contains("longreachethernet0")) {
                    edgePorts.add(intfPort);
                }
            }
        });

        return edgePorts;
    }

    public Map<String, Map<String, Object>> getDataPlaneModels() {
        return dpDeviceModels;
    }
}
