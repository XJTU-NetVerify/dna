package org.ants.exp;

import org.ants.main.DNA;
import org.ants.parser.relation.Interface;
import org.ants.parser.relation.Node;
import org.ants.parser.relation.OspfIntfSetting;
import org.ants.parser.relation.OspfMultipath;
import org.ants.parser.relation.Relation;

import java.io.IOException;
import java.util.*;

public class EvalSyntheticChangeOSPF {
    public static Random rand = new Random(System.currentTimeMillis());
    public ExpHelper helper;

    public EvalSyntheticChangeOSPF(String configPath) throws IOException {
        helper = new ExpHelper(configPath, 50);
    }

    public void runInterfaceUpdate() throws IOException{
        List<ExpRecord> expRecords = helper.runBatchUpdate(Interface.class.getSimpleName());
        helper.saveResult(DNA.testcase + "_lf", expRecords);
        helper.bookSumTime("Interface", expRecords);
    }

    public void runLinkCostUpdate() throws IOException {
        List<ExpRecord> res = new ArrayList<>();
        List<Relation> ospf_intfs = helper.baseRelations.get(OspfIntfSetting.class.getSimpleName());

        for (int i = 0; i < helper.batchSize; i++) {
            OspfIntfSetting intf = (OspfIntfSetting) ospf_intfs.get(rand.nextInt(ospf_intfs.size()));
            OspfIntfSetting new_intf = new OspfIntfSetting(intf.node, intf.intf, 100, intf.area, intf.passive, intf.process);

            Map<String, List<Relation>> updates = new HashMap<>();
            updates.put("insert", Collections.singletonList(new_intf));
            updates.put("delete", Collections.singletonList(intf));
            res.add(helper.runTransaction(String.format("change_update_%d", i + 1), updates));
            updates.clear();

            updates.put("delete", Collections.singletonList(new_intf));
            updates.put("insert", Collections.singletonList(intf));
            res.add(helper.runTransaction(String.format("change_restore_%d", i + 1), updates));
        }
        helper.saveResult(DNA.testcase + "_lc", res);
        helper.bookSumTime("LinkCost", res);
    }

    public void runMultipathUpdate() throws IOException{
        List<ExpRecord> res = new ArrayList<>();
        List<Relation> nodeRels = helper.baseRelations.get(Node.class.getSimpleName());

        for (int i = 0; i < helper.batchSize; i++) {
            Node node = (Node) nodeRels.get(rand.nextInt(nodeRels.size()));
            OspfMultipath ospfMultipath = new OspfMultipath(node, 1, 1);
            res.addAll(helper.runSingleUpdateReverse(ospfMultipath,  i));
        }
        helper.saveResult(DNA.testcase + "_mp", res);
        helper.bookSumTime("MultiPath", res);
    }

    public static void main(String[] args) throws IOException{
        String configPath = args[0];
        // String configPath = "../networks/fattree/ospf/ospf_fattree04";

        EvalSyntheticChangeOSPF evalSyntheticChange = new EvalSyntheticChangeOSPF(configPath);
        evalSyntheticChange.runInterfaceUpdate();
        evalSyntheticChange.runLinkCostUpdate();
        evalSyntheticChange.runMultipathUpdate();
        evalSyntheticChange.helper.summaryTime();
    }
}

