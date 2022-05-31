package org.ants.exp;

import org.ants.parser.datamodel.Prefix;
import org.ants.parser.relation.*;
import org.ants.parser.relation.neighbor.EBgpNeighbor;
import org.ants.parser.relation.routeMap.Policy;
import org.ants.parser.relation.routeMap.RouteMapIn;
import org.ants.parser.relation.routeMap.setAttribute.SetAttribute;
import org.ants.parser.relation.routeMap.setAttribute.SetLocalPref;
import org.ants.main.DNA;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/*
  Run synthetic change evaluation, only for Fattree topology
 */
public class EvalSyntheticChangeBGP {
    public static Random rand = new Random(System.currentTimeMillis());
    public ExpHelper helper;

    public EvalSyntheticChangeBGP(String configPath) throws IOException{
        helper = new ExpHelper(configPath, 50);
    }

    public void runInterfaceUpdate() throws IOException{
        List<ExpRecord> expRecords = helper.runBatchUpdate(Interface.class.getSimpleName());
        helper.saveResult(DNA.testcase + "_lf", expRecords);
        helper.bookSumTime("Interface", expRecords);
    }

    public void runNetworkUpdate() throws IOException{
        List<ExpRecord> expRecords = helper.runBatchUpdate(BgpNetwork.class.getSimpleName());
        helper.saveResult(DNA.testcase + "_net", expRecords);
        helper.bookSumTime("Network", expRecords);
    }

    public void runNeighborUpdate() throws IOException{
        List<ExpRecord> expRecords = helper.runBatchUpdate(EBgpNeighbor.class.getSimpleName());
        helper.saveResult(DNA.testcase + "_bn", expRecords);
        helper.bookSumTime("Neighbor", expRecords);
    }

    public void runStaticRouteUpdate() throws IOException{
        List<ExpRecord> expRecords = helper.runBatchUpdate(StaticRoute.class.getSimpleName());
        helper.saveResult(DNA.testcase + "_sr", expRecords);
        helper.bookSumTime("StaticRoute", expRecords);
    }

    public void runLocalPrefUpdate() throws IOException{
        List<ExpRecord> res = new ArrayList<>();
        List<Relation> ebnRels = helper.baseRelations.get(EBgpNeighbor.class.getSimpleName());

        for (int i = 0; i < helper.batchSize; i++) {
            EBgpNeighbor ebn = (EBgpNeighbor) ebnRels.get(rand.nextInt(ebnRels.size()));
            Map<String, SetAttribute> setAttribute = new HashMap<>();
            setAttribute.put("SetLocalPref", new SetLocalPref(150));
            Policy policy = new Policy(true, new HashMap<>(), setAttribute);
            RouteMapIn routeMapIn = new RouteMapIn(ebn.node1, ebn.node2, Collections.singletonList(policy));

            res.addAll(helper.runSingleUpdateReverse(routeMapIn, i));
        }
        helper.saveResult(DNA.testcase + "_lp", res);
        helper.bookSumTime("LocalPref", res);
    }

    public void runMultipathUpdate() throws IOException{
        List<ExpRecord> res = new ArrayList<>();
        List<Relation> nodeRels = helper.baseRelations.get(Node.class.getSimpleName());

        for (int i = 0; i < helper.batchSize; i++) {
            Node node = (Node) nodeRels.get(rand.nextInt(nodeRels.size()));
            BgpMultipath bgpMultipath = new BgpMultipath(node, 16, true);
            res.addAll(helper.runSingleUpdateReverse(bgpMultipath,  i));
        }
        helper.saveResult(DNA.testcase + "_mp", res);
        helper.bookSumTime("MultiPath", res);
    }

    public void runAggregationUpdate() throws IOException{
        List<ExpRecord> res = new ArrayList<>();
        List<Relation> netRels = helper.baseRelations.get(BgpNetwork.class.getSimpleName());
        List<Relation> nodeRels = helper.baseRelations.get(Node.class.getSimpleName());
        List<BgpNetwork> evenNets = netRels.stream().map(r -> (BgpNetwork) r)
                .filter(net -> octect2IsEven(net.prefix.ip)).collect(Collectors.toList());
        List<Node> aggrNodes = nodeRels.stream().map(r -> (Node) r).filter(n -> n.node.startsWith("aggr")).collect(Collectors.toList());

        for (int i = 0; i < helper.batchSize; i++) {
            BgpNetwork bgpNet = evenNets.get(rand.nextInt(evenNets.size()));
            Node node = aggrNodes.get(rand.nextInt(aggrNodes.size()));
            BgpAggregation bgpAggregation = new BgpAggregation(node, new Prefix(String.format("%s/23", bgpNet.prefix.ip)));
            res.addAll(helper.runSingleUpdateReverse(bgpAggregation, i));
        }
        helper.saveResult(DNA.testcase + "_agg", res);
        helper.bookSumTime("Aggregation", res);
    }

    private boolean octect2IsEven(String ip) {
        String[] items = ip.split("\\.");
        return Integer.parseInt(items[2]) % 2 == 0;
    }

    public static void main(String[] args) throws IOException{
        String configPath = args[0];
        // String configPath = "../networks/fattree/bgp/bgp_fattree04";

        EvalSyntheticChangeBGP evalSyntheticChangeBGP = new EvalSyntheticChangeBGP(configPath);
        evalSyntheticChangeBGP.runInterfaceUpdate();
        evalSyntheticChangeBGP.runNetworkUpdate();
        evalSyntheticChangeBGP.runNeighborUpdate();
        evalSyntheticChangeBGP.runLocalPrefUpdate();
        evalSyntheticChangeBGP.runMultipathUpdate();
        evalSyntheticChangeBGP.runAggregationUpdate();
        evalSyntheticChangeBGP.runStaticRouteUpdate();
        evalSyntheticChangeBGP.helper.summaryTime();
    }
}
