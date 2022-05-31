package org.ants.exp;

import org.ants.main.DNA;
import org.ants.parser.relation.Interface;
import org.ants.parser.relation.Relation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EvalIncreBorder {
    public ExpHelper helper;
    public static double factor = 30.0;

    public EvalIncreBorder(String configPath) throws IOException {
        helper = new ExpHelper(configPath, 0);
    }

    public <E> List<E> pickNRandomElements(List<E> list, int n, Random r) {
        int length = list.size();
        if (length < n) return null;

        for (int i = length - 1; i >= length - n; --i)
        {
            Collections.swap(list, i , r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    public <E> List<E> pickNRandomElements(List<E> list, int n) {
        return pickNRandomElements(list, n, ThreadLocalRandom.current());
    }

    public void runLinkFailureUpdate() throws IOException {
        List<Relation> intfRels = helper.baseRelations.get(Interface.class.getSimpleName());
        Map<String, Relation> intfRelMap = new HashMap<>();
        for (Relation intfRel : intfRels) {
            Interface intf = (Interface) intfRel;
            String relKey = String.format("%s:%s", intf.node.node, intf.intf);
            intfRelMap.put(relKey, intf);
        }
        Set<String> avoidRepeat = new HashSet<>();
        List<List<String>> links = new ArrayList<>();
        for (String link : DNA.parser.getTopology()) {
            String[] items = link.split(" ");
            String intf1 = String.format("%s:%s", items[0], items[1]);
            String intf2 = String.format("%s:%s", items[2], items[3]);
            if (avoidRepeat.contains(String.format("%s+%s", intf1, intf2)))
                continue;
            links.add(Arrays.asList(intf1, intf2));
            avoidRepeat.add(String.format("%s+%s", intf1, intf2));
            avoidRepeat.add(String.format("%s+%s", intf2, intf1));
        }

        int failure_num = 1;
        int total_num = links.size();
        while (failure_num <= total_num) {
            Timer.timestamp();
            List<ExpRecord> res = new ArrayList<>();

            int repeat_num = Math.max(1, (int)((double)(total_num - failure_num) / total_num * factor));
            for (int i = 0; i < repeat_num; ++i) {
                List<List<String>> selected_edges = pickNRandomElements(links, failure_num);

                List<Relation> deletes = new ArrayList<>();
                for (List<String> link : selected_edges) {
                    Interface intfRel1 = (Interface) intfRelMap.get(link.get(0));
                    Interface intfRel2 = (Interface) intfRelMap.get(link.get(1));
                    deletes.add(intfRel1);
                    deletes.add(intfRel2);
                }

                Map<String, List<Relation>> updates = new HashMap<>();
                updates.put("delete", deletes);
                res.add(helper.runTransaction(String.format("change_update_%d", i+1), updates));

                Map<String, List<Relation>> restores = new HashMap<>();
                restores.put("insert", deletes);
                res.add(helper.runTransaction(String.format("change_restore_%d", i+1), restores));
            }
            helper.saveResult(DNA.testcase + "_lf_" + failure_num, res);
            helper.batchSize = repeat_num;
            helper.bookSumTime("lf-" + failure_num, res);

            if (failure_num == 1)
                failure_num += 4;
            else
                failure_num += 5;
        }
    }

    public static void main(String[] args) throws IOException{
        String configPath = args[0];
        // String configPath = "../networks/config2spec-networks/bics/bgp";

        EvalIncreBorder evalSyntheticChange = new EvalIncreBorder(configPath);
        evalSyntheticChange.runLinkFailureUpdate();
        evalSyntheticChange.helper.summaryTime();
    }
}

