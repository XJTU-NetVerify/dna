package org.ants.parser.datamodel;

import javafx.util.Pair;
import org.ants.parser.relation.Interface;
import org.ants.parser.relation.StaticRoute;
import org.ants.parser.relation.*;
import org.ants.parser.relation.neighbor.BgpReflectClient;
import org.ants.parser.relation.neighbor.EBgpNeighbor;
import org.ants.parser.relation.neighbor.IBgpNeighbor;
import org.ants.parser.relation.routeMap.Policy;
import org.ants.parser.relation.routeMap.RouteMapIn;
import org.ants.parser.relation.routeMap.RouteMapOut;
import org.ants.parser.relation.routeMap.matchCondition.*;
import org.ants.parser.relation.routeMap.setAttribute.SetCommunity;
import org.ants.parser.relation.routeMap.setAttribute.*;
import org.ants.parser.utils.IpHelper;
import org.batfish.datamodel.*;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.ospf.*;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.*;
import org.batfish.datamodel.routing_policy.statement.*;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Device {

  private Node node;
  private List<Interface> intfs;
  private List<StaticRoute> staticRoutes;
  private List<OspfIntfSetting> ospfIntfSetting;
  private List<OspfRedis> ospfRedises;

  private List<OspfStubArea> ospfStubAreas;
  private List<EBgpNeighbor> eBgpNeighbors;
  private List<IBgpNeighbor> iBgpNeighbors;
  private List<BgpReflectClient> bgpReflectClients;
  private List<RouteMapIn> routeMapIns;
  private List<RouteMapOut> routeMapOuts;
  private List<BgpNetwork> bgpNetworks;
  private List<BgpAggregation> bgpAggregations;
  private List<BgpRedis> bgpRedises;
  private BgpMultipath bgpMultipath;
  private long routerId = 0L;
  private long as = 0L;
  private Configuration config;
  private Vrf vrf;
  private Map<Ip, Map<String, Set<String>>> ipVrfOwners;   // ip -> hostname -> interface

  public Device() {}

  public Device(Node node, Vrf vrf, Configuration config, Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
    this.node = node;
    this.vrf = vrf;
    this.config = config;
    this.ipVrfOwners = ipVrfOwners;

    init();
  }

  public static String genName(String node, String vrf) {
    if (vrf.equals("default")) {
      return node;
    } else {
      return String.format("%s::%s", node, vrf);
    }
  }

  public void init() {
    intfs = new ArrayList<>();
    staticRoutes = new ArrayList<>();

    ospfIntfSetting = new ArrayList<>();
    ospfRedises = new ArrayList<>();
    ospfStubAreas = new ArrayList<>();

    eBgpNeighbors = new ArrayList<>();
    iBgpNeighbors = new ArrayList<>();
    bgpReflectClients = new ArrayList<>();
    bgpNetworks = new ArrayList<>();
    bgpAggregations = new ArrayList<>();
    bgpMultipath = new BgpMultipath(node, -1, false);
    bgpRedises = new ArrayList<>();

    routeMapIns = new ArrayList<>();
    routeMapOuts = new ArrayList<>();

    generateInterface();
    generateStaticRoute();
    if (!vrf.getOspfProcesses().keySet().isEmpty()) {
      generateOspf();
    }
    if (vrf.getBgpProcess() != null) {
      Optional<BgpActivePeerConfig> peer = vrf.getBgpProcess().getActiveNeighbors().values().stream().findAny();
      if(peer.isPresent())
        as = peer.get().getLocalAs();
      generateBgp();
    }
    completeNode();
  }

  public void generateInterface() {
    for (String intfName : vrf.getInterfaces().keySet()) {
      org.batfish.datamodel.Interface intf = vrf.getInterfaces().get(intfName);
      if (intf.getActive()) {
        for (ConcreteInterfaceAddress cid : intf.getAllConcreteAddresses()) {
          intfs.add(
              new Interface(
                  node, intf.getName(), cid.getIp().toString(), cid.getPrefix().toString()));
        }
      }
    }

    long maxAddr = 0;
    for (Interface intf : intfs) {
      long numAddr = IpHelper.ipToNumber(intf.ip);
      maxAddr = Math.max(maxAddr, numAddr);
      if (intf.intf.contains("Loopback")) {
        routerId = Math.max(routerId, numAddr);
      }
    }
    if (routerId == 0L) {
      routerId = maxAddr;
    }
  }

  public void generateStaticRoute() {
    staticRoutes =
        vrf.getStaticRoutes().stream()
            .map(
                r ->
                    new StaticRoute(
                        node,
                        r.getNetwork().toString(),
                        r.getNextHopIp().toString(),
                        r.getAdministrativeCost()))
            .collect(Collectors.toList());
  }

  public void generateOspf() {

    Collection<OspfProcess> ops = vrf.getOspfProcesses().values();

    if (!ops.isEmpty()) {
      long rid = ops.iterator().next().getRouterId().asLong();
      routerId = Math.max(rid, routerId);

      ops.forEach(
          op -> {
            Map<OspfNeighborConfigId, OspfNeighborConfig> onMap = op.getOspfNeighborConfigs();
            for (OspfNeighborConfigId configId : onMap.keySet()) {
              OspfNeighborConfig neighborConfig = onMap.get(configId);
              org.batfish.datamodel.Interface intf1 =
                  config.getAllInterfaces().get(configId.getInterfaceName());
              OspfIntfSetting ois = new OspfIntfSetting(node, intf1.getName());
              if (intf1.getOspfEnabled()) {
                if (intf1.getOspfCost() != null) {
                  ois.cost = intf1.getOspfCost();
                  ois.area = neighborConfig.getArea();
                  ois.passive = neighborConfig.isPassive();
                  ois.process = Long.parseLong(op.getProcessId());
                  ospfIntfSetting.add(ois);
                }
              }
            }
          });

      ops.forEach(
          op -> {
            String exportPolicyName = op.getExportPolicy();
            long processId = Long.parseLong(op.getProcessId());
            RoutingPolicy exportPolicy = config.getRoutingPolicies().get(exportPolicyName);
            List<Statement> statements = exportPolicy.getStatements();
            for (Statement statement : statements) {
              String comment = statement.getComment();
              if (comment.contains("OSPF export routes for")) {
                String[] tokens = comment.split("\\s+");
                String protocol = tokens[tokens.length - 1];
                ospfRedises.add(new OspfRedis(node, true, protocol, processId));
              }
            }
          });

      ops.forEach(
          op -> {
            op.getAreas()
                .forEach(
                    (areaId, ospfArea) -> {
                      long processId = Long.parseLong(op.getProcessId());
                      if (ospfArea.getStubType().equals(StubType.STUB)) {
                        boolean suppressType3 = ospfArea.getStub().getSuppressType3();
                        ospfStubAreas.add(
                            OspfStubArea.stubArea(node, processId, areaId, suppressType3));
                      } else if (ospfArea.getStubType().equals(StubType.NSSA)) {
                        // TODO: resolve stubType==NSSA
                        NssaSettings nssa = ospfArea.getNssa();
                        boolean suppressType3 = nssa.getSuppressType3();
                        boolean suppressType7 = nssa.getSuppressType7();
                        ospfStubAreas.add(
                            OspfStubArea.nssaArea(
                                node, processId, areaId, suppressType3, true, suppressType7));
                      }
                    });
          });
    }
  }

  public void generateBgp() {

    routerId = vrf.getBgpProcess().getRouterId().asLong();

    vrf.getBgpProcess().getActiveNeighbors().values().stream()
        .filter(pc -> pc.getLocalAs().equals(pc.getRemoteAsns().singletonValue()))
        .forEach(
            pc -> {
              Node peerNode = getIpLoc(pc.getPeerAddress());
              if (peerNode != null && pc.getIpv4UnicastAddressFamily() != null) {
                if (pc.getIpv4UnicastAddressFamily().getRouteReflectorClient()) {
                  bgpReflectClients.add(
                      new BgpReflectClient(
                          node,
                          pc.getLocalIp().toString(),
                          peerNode,
                          pc.getPeerAddress().toString()));
                } else {
                  iBgpNeighbors.add(
                      new IBgpNeighbor(
                          node,
                          pc.getLocalIp().toString(),
                          peerNode,
                          pc.getPeerAddress().toString()));
                }
              }
            });

    eBgpNeighbors =
        vrf.getBgpProcess().getActiveNeighbors().values().stream()
            .filter(
                pc -> pc.getLocalIp() != null && ipVrfOwners.containsKey(pc.getPeerAddress()))
            .filter(pc -> !pc.getLocalAs().equals(pc.getRemoteAsns().enumerate().iterator().next()))
            .map(
                pc -> {
                  Node peerNode = getIpLoc(pc.getPeerAddress());
                  if (peerNode == null) {
                    return null;
                  }
                  return new EBgpNeighbor(
                      node, pc.getLocalIp().toString(), peerNode, pc.getPeerAddress().toString());
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    routerId = Math.max(routerId, vrf.getBgpProcess().getRouterId().asLong());

    String defaultExportPolicy = Names.generatedBgpCommonExportPolicyName(vrf.getName());
    RoutingPolicy policy = config.getRoutingPolicies().get(defaultExportPolicy);

    if (policy != null) {
      List<Statement> statements = policy.getStatements();
      for (Statement statement : statements) {
        if (statement instanceof If) {
          If ifStatement = (If) statement;
          if (ifStatement.getGuard() instanceof Disjunction) {
            Disjunction disjunction = (Disjunction) ifStatement.getGuard();
            List<BooleanExpr> disjuncts = disjunction.getDisjuncts();
            for (BooleanExpr disjunct : disjuncts) {
              if (disjunct instanceof Conjunction) {
                List<BooleanExpr> conjuncts = ((Conjunction) disjunct).getConjuncts();
                for (BooleanExpr conjunct : conjuncts) {
                  if (conjunct instanceof MatchPrefixSet) {
                    PrefixSetExpr prefixSet = ((MatchPrefixSet) conjunct).getPrefixSet();
                    if (prefixSet instanceof ExplicitPrefixSet) {
                      PrefixSpace prefixSpace = ((ExplicitPrefixSet) prefixSet).getPrefixSpace();
                      prefixSpace
                          .getPrefixRanges()
                          .forEach(
                              prefixRange ->
                                  bgpNetworks.add(
                                      new BgpNetwork(node, prefixRange.getPrefix().toString())));
                    }
                  } else if (conjunct instanceof MatchProtocol) {
                    ((MatchProtocol) conjunct)
                        .getProtocols()
                        .forEach(
                            routingProtocol ->
                                bgpRedises.add(new BgpRedis(node, routingProtocol.protocolName())));
                  }
                }
              }
            }
          }
        }
      }
    }

    if (vrf.getBgpProcess().getMultipathIbgp() || vrf.getBgpProcess().getMultipathEbgp()) {
      if (vrf.getBgpProcess().getMultipathEquivalentAsPathMatchMode()
          == MultipathEquivalentAsPathMatchMode.PATH_LENGTH) {
        bgpMultipath = new BgpMultipath(node, 16, true);
      } else {
        bgpMultipath = new BgpMultipath(node, 16, false);
      }
    }

    // only consider ebpg peer
    vrf.getBgpProcess().getActiveNeighbors().values().stream()
        .filter(Objects::nonNull)
        .filter(bpc -> ipVrfOwners.containsKey(bpc.getPeerAddress()))
        .filter(bpc -> bpc.getIpv4UnicastAddressFamily() != null)
        .forEach(
            bpc -> { // bpc: one port of local node
              if (!bpc.getIpv4UnicastAddressFamily().getImportPolicySources().isEmpty()) {
                RouteMapIn rmi = generateRouteMapIn(bpc);
                if (rmi.getPolicy() != null || !rmi.getPolicy().isEmpty()) {
                  routeMapIns.add(rmi);
                }
              }
              if (!bpc.getIpv4UnicastAddressFamily().getExportPolicySources().isEmpty()) {
                RouteMapOut rmo = generateRouteMapOut(bpc);
                if (rmo.getPolicy() != null || !rmo.getPolicy().isEmpty()) {
                  routeMapOuts.add(rmo);
                }
              }
            });

    bgpAggregations =
        config.getRoutingPolicies().keySet().stream()
            .filter(rpn -> rpn.contains("AGGREGATE_ROUTE"))
            .map(this::generateBgpAggregation)
            .collect(Collectors.toList());
  }

  private BgpAggregation generateBgpAggregation(String rpn) {
    String[] elems = rpn.split(":");
    String elem = elems[elems.length - 1];
    String prefixText = elem.substring(0, elem.length() - 1);
    return new BgpAggregation(node, new Prefix(prefixText));
  }

  public void completeNode() {
    node = new Node(node, as, routerId);
  }

  private RouteMapIn generateRouteMapIn(BgpActivePeerConfig bpc) {
    Node peerNode = getIpLoc(bpc.getPeerAddress());
    if (peerNode == null) {
      return null;
    }
    List<Policy> routeMaps =
        extractPolicy(bpc.getIpv4UnicastAddressFamily().getImportPolicySources().first());
    return new RouteMapIn(node, peerNode, routeMaps);
  }

  private RouteMapOut generateRouteMapOut(BgpActivePeerConfig bpc) {
    Node peerNode = getIpLoc(bpc.getPeerAddress());
    if (peerNode == null) {
      return null;
    }
    List<Policy> routeMaps =
        extractPolicy(bpc.getIpv4UnicastAddressFamily().getExportPolicySources().first());
    return new RouteMapOut(node, peerNode, routeMaps);
  }

  private List<Policy> extractPolicy(String policy) {
    // Match: nextHopIp, prefix, asPath, community
    // Set: nextHopIp, localPref, med, community
    RoutingPolicy rp = config.getRoutingPolicies().get(policy);
    return rp.getStatements().stream()
        .flatMap(
            ifStatement -> {
              // extract route-map info recursively: false<Statement> guard true<Statement>
              // route-map default to be permit
              return analyzeStatement(ifStatement).stream();
            })
        .collect(Collectors.toList());
  }

  private List<Policy> analyzeStatement(Statement statement) {
    List<Policy> routeMaps = new ArrayList<>();

    if (statement.getClass().equals(If.class)) {
      If anIf = (If) statement;
      // here using map to record Specific type of Match or Set, it should have only one entry
      Map<String, MatchCondition> matchs = analyzeGuard(anIf.getGuard());
      Map<String, SetAttribute> sets = analyzeTrueStatement(anIf.getTrueStatements());

      boolean returnFalse = sets.containsKey("ReturnFalse");

      if (returnFalse) {
        routeMaps.add(new Policy(false, matchs, null));
      } else {
        routeMaps.add(new Policy(true, matchs, sets));
      }

      for (Statement falseStatement : anIf.getFalseStatements()) {
        if (!analyzeStatement(falseStatement).isEmpty()) {
          routeMaps.addAll(analyzeStatement(falseStatement));
        }
      }
    } else if (statement.getClass().equals(StaticStatement.class)) {
      StaticStatement ss = (StaticStatement) statement;
      if (ss.getType().equals(Statements.ReturnTrue)) {
        // using null to represent empty match conditions
        routeMaps.add(new Policy(true, null, null));
      } else if (ss.getType().equals(Statements.ReturnFalse)) {
        routeMaps.add(new Policy(false, null, null));
      }
    }
    return routeMaps;
  }

  private Map<String, MatchCondition> analyzeGuard(BooleanExpr guard) {
    // TODO: batfish only support one routing filter for each policy
    Map<String, MatchCondition> matchConditions = new HashMap<>();
    String clazz = guard.getClass().getSimpleName();
    switch (clazz) {
      case "MatchPrefixSet":
        matchConditions.put(
            "MatchPrefixList", match4Prefix(((MatchPrefixSet) guard).getPrefixSet()));
        break;
      case "MatchCommunitySet":
        matchConditions.put(
            "MatchCommunity", match4Community(((MatchCommunitySet) guard).getExpr()));
        break;
      case "MatchAsPath":
        matchConditions.put("MatchAsPaths", match4AsPath(((MatchAsPath) guard).getExpr()));
        break;
      default:
        System.out.println("unresolved matching type: " + guard);
        break;
    }

    for (String key : matchConditions.keySet()) {
      if (matchConditions.get(key) == null) {
        matchConditions.remove(key);
      }
    }

    return matchConditions;
  }

  @Nullable
  private MatchPrefixList match4Prefix(PrefixSetExpr pse) {
    MatchPrefixList matchPrefixList = null;
    if (pse.getClass().equals(NamedPrefixSet.class)) {
      RouteFilterList routeFilterList =
          config.getRouteFilterLists().get(((NamedPrefixSet) pse).getName());
      Set<Prefix> prefixs =
          routeFilterList.getLines().stream()
              .map(
                  l -> {
                    String prefixStr = l.getIpWildcard().toPrefix().toString();
                    if (l.getLengthRange().isSingleValue()) {
                      return new CommonPrefix(prefixStr);
                    } else {
                      SubRange range = l.getLengthRange();
                      return new ExtendPrefix(prefixStr, range.getStart(), range.getEnd());
                    }
                  })
              .collect(Collectors.toSet());

      if (!prefixs.isEmpty()) matchPrefixList = new MatchPrefixList(prefixs);

    } else {
      System.out.println("unresolved matching prefixSetExpr: " + pse.getClass());
    }
    return matchPrefixList;
  }

  private MatchCommunity Name2RegexCommunity(NamedCommunitySet ncs) {
    CommunityList communityList = config.getCommunityLists().get(ncs.getName());
    MatchCommunity matchCommunity = null;

    List<DCommunity> communitySet =
        communityList.getLines().stream()
            .map(CommunityListLine::getMatchCondition)
            .map(this::extractCommunity)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (!communitySet.isEmpty()) matchCommunity = new MatchCommunity(communitySet);

    return matchCommunity;
  }

  private DCommunity extractCommunity(CommunitySetExpr cse) {
    String community = ".*:";
    if (cse instanceof RegexCommunitySet) {
      community = ((RegexCommunitySet) cse).getRegex();
    } else if (cse instanceof LiteralCommunity) {
      community = ((LiteralCommunity) cse).getCommunity().matchString();
    }

    String[] cmt = community.split(":");
    if (cmt.length == 1) {
      return new DCommunity(cmt[0], ".*");
    } else if (cmt.length == 2) {
      return new DCommunity(cmt[0], cmt[1]);
    } else {
      return null;
    }
  }

  private MatchCommunity match4Community(CommunitySetExpr cse) {
    // TODO: resolve communitySet matching
    MatchCommunity communityRegexSet = null;
    if (cse.getClass().equals(NamedCommunitySet.class)) {
      communityRegexSet = Name2RegexCommunity((NamedCommunitySet) cse);
    } else {
      System.out.println(">>>> tag class " + cse.getClass());
    }
    return communityRegexSet;
  }

  private MatchAsPaths match4AsPath(AsPathSetExpr ase) {
    // TODO: resolve asPathSet matching

    return null;
  }

  private Map<String, SetAttribute> analyzeTrueStatement(List<Statement> trueStatement) {

    Map<String, SetAttribute> setAttributes = new HashMap<>();

    for (Statement statement : trueStatement) {
      String clazz = statement.getClass().getSimpleName();

      switch (clazz) {
        case "SetMetric":
          setAttributes.put("SetMed", set4Metric(((SetMetric) statement).getMetric()));
          break;
        case "AddCommunity":
          if (set4Community(((AddCommunity) statement).getExpr()) != null)
            setAttributes.put("SetCommunity", set4Community(((AddCommunity) statement).getExpr()));
          break;
        case "SetNextHop":
          setAttributes.put("SetNhip", set4NextHop(((SetNextHop) statement).getExpr()));
          break;
        case "SetLocalPreference":
          setAttributes.put(
              "SetLocalPref",
              set4LocalPreference(((SetLocalPreference) statement).getLocalPreference()));
          break;
        case "StaticStatement":
          if (((StaticStatement) statement).getType().equals(Statements.ReturnFalse)) {
            setAttributes.put("ReturnFalse", new ReturnFalse());
          }
          break;
        default:
          break;
      }
    }

    for (String key : setAttributes.keySet()) {
      if (setAttributes.get(key) == null) {
        setAttributes.remove(key);
      }
    }

    return setAttributes;
  }

  private SetMed set4Metric(LongExpr le) {
    if (le instanceof LiteralLong) {
      long metric = ((LiteralLong) le).getValue();
      return new SetMed(metric);
    }
    return null;
  }

  private SetCommunity set4Community(CommunitySetExpr cse) {
    // TODO: resolve rewriting community
    Set<SetCommunity> communitySet = new HashSet<>();
    if (cse.getClass().equals(LiteralCommunitySet.class)) {
      Set<Community> communities = ((LiteralCommunitySet) cse).getCommunities();

      communitySet =
          communities.stream()
              .map(c -> new SetCommunity(true, DCommunity.parse(c.asBigInt())))
              .collect(Collectors.toSet());

    } else {
      System.out.println(cse.getClass().getSimpleName());
    }
    return communitySet.iterator().next();
  }

  private SetNhop set4NextHop(NextHopExpr nhe) {
    // TODO: resolve rewriting nexthop
    if (nhe instanceof IpNextHop) {
      List<Ip> ips = ((IpNextHop) nhe).getIps();
      if (!ips.isEmpty()) {
        return new SetNhop(ips.get(0));
      }
    }
    return null;
  }

  private SetLocalPref set4LocalPreference(LongExpr le) {
    // TODO: resolve rewriting localPreference
    if (le instanceof LiteralLong) {
      long localPreference = ((LiteralLong) le).getValue();
      return new SetLocalPref(localPreference);
    }

    return null;
  }

  private Node getIpLoc(Ip ip) {
    Map<String, Set<String>> nodeVrf = ipVrfOwners.get(ip);
    if (nodeVrf == null) {
      return null;
    }
    Entry<String, Set<String>> entry = nodeVrf.entrySet().iterator().next();
    return new Node(entry.getKey(), entry.getValue().iterator().next());
  }

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  public List<Interface> getIntfs() {
    return intfs;
  }

  public void setIntfs(List<Interface> intfs) {
    this.intfs = intfs;
  }

  public List<StaticRoute> getStaticRoutes() {
    return staticRoutes;
  }

  public void setStaticRoutes(List<StaticRoute> staticRoutes) {
    this.staticRoutes = staticRoutes;
  }

  public List<EBgpNeighbor> geteBgpNeighbors() {
    return eBgpNeighbors;
  }

  public void seteBgpNeighbors(List<EBgpNeighbor> eBgpNeighbors) {
    this.eBgpNeighbors = eBgpNeighbors;
  }

  public List<IBgpNeighbor> getiBgpNeighbors() {
    return iBgpNeighbors;
  }

  public void setiBgpNeighbors(List<IBgpNeighbor> iBgpNeighbors) {
    this.iBgpNeighbors = iBgpNeighbors;
  }

  public List<RouteMapIn> getRouteMapIns() {
    return routeMapIns;
  }

  public void setRouteMapIns(List<RouteMapIn> routeMapIns) {
    this.routeMapIns = routeMapIns;
  }

  public List<RouteMapOut> getRouteMapOuts() {
    return routeMapOuts;
  }

  public void setRouteMapOuts(List<RouteMapOut> routeMapOuts) {
    this.routeMapOuts = routeMapOuts;
  }

  public List<BgpNetwork> getBgpNetworks() {
    return bgpNetworks;
  }

  public void setBgpNetworks(List<BgpNetwork> bgpNetworks) {
    this.bgpNetworks = bgpNetworks;
  }

  public List<BgpAggregation> getBgpAggregations() {
    return bgpAggregations;
  }

  public void setBgpAggregations(List<BgpAggregation> bgpAggregations) {
    this.bgpAggregations = bgpAggregations;
  }

  public List<OspfIntfSetting> getOspfIntfSetting() {
    return ospfIntfSetting;
  }

  public void setOspfIntfSetting(List<OspfIntfSetting> ospfIntfSetting) {
    this.ospfIntfSetting = ospfIntfSetting;
  }

  public List<OspfStubArea> getOspfStubAreas() {
    return ospfStubAreas;
  }

  public List<OspfRedis> getOspfRedises() {
    return ospfRedises;
  }

  public List<BgpReflectClient> getBgpReflectClients() {
    return bgpReflectClients;
  }

  public void setBgpReflectClients(List<BgpReflectClient> bgpReflectClients) {
    this.bgpReflectClients = bgpReflectClients;
  }

  public BgpMultipath getBgpMultipath() {
    return bgpMultipath;
  }

  public void setBgpMultipath(BgpMultipath bgpMultipath) {
    this.bgpMultipath = bgpMultipath;
  }

  public List<BgpRedis> getBgpRedises() {
    return bgpRedises;
  }

  public List<Relation> getDeviceInfo() {
    List<Relation> relations = new ArrayList<>();

    relations.add(getNode());
    relations.addAll(getIntfs());
    relations.addAll(getStaticRoutes());
    relations.addAll(getOspfIntfSetting());
    relations.addAll(getOspfRedises());
    relations.addAll(getOspfStubAreas());
    relations.addAll(getiBgpNeighbors());
    relations.addAll(geteBgpNeighbors());
    relations.addAll(getBgpReflectClients());
    relations.addAll(getBgpNetworks());
    relations.addAll(getBgpAggregations());
    if (getBgpMultipath().num > 0) {
      relations.add(getBgpMultipath());
    }
    relations.addAll(getRouteMapIns());
    relations.addAll(getRouteMapOuts());

    return relations;
  }

  // Compare two device and return the diff
  public Pair<List<Relation>, List<Relation>> compareTo(Device other) {
    Set<Relation> currRelations= new HashSet<>(getDeviceInfo());
    Set<Relation> otherRelations= new HashSet<>(other.getDeviceInfo());

    Set<Relation> toAdd = new HashSet<>(currRelations);
    toAdd.removeAll(otherRelations);
    Set<Relation> toRemove = otherRelations;
    toRemove.removeAll(currRelations);

    return new Pair<>(new ArrayList<>(toAdd), new ArrayList<>(toRemove));
  }
}
