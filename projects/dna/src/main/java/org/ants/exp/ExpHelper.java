package org.ants.exp;

import org.ants.main.DNA;
import org.ants.parser.relation.Relation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class TimeRecord {
    String dscp;
    Long time;

    public TimeRecord(String dscp, Long time) {
        this.dscp = dscp;
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("%s: %.3f ms", dscp, time / 1e6);
    }
}

class Timer {
    public static long preTime = 0;
    public static long curTime = 0;

    public static void timestamp() {
        curTime = System.nanoTime();
    }

    public static Long dumpTime() {
        preTime = curTime;
        curTime = System.nanoTime();
        return curTime - preTime;
    }
}

class ExpRecord {
    public String dscp;
    public Map<String, List<Relation>> input;
    public ArrayList<String> dpgOutput;
    public ArrayList<String> dpvOutput;
    public ArrayList<Long> timeCost;

    public ExpRecord(String dscp, Map<String, List<Relation>> input, ArrayList<Long> timeCost) {
        this.dscp = dscp;
        this.input = input;
        this.timeCost = timeCost;
    }

    public void setDpgOutput(ArrayList<String> dpgOutput) {
        this.dpgOutput = dpgOutput;
    }

    public void setDpvOutput(ArrayList<String> dpvOutput) {
        this.dpvOutput = dpvOutput;
    }
}

public class ExpHelper {
    public List<TimeRecord> timeBook;
    public Map<String, List<Relation>> baseRelations;
    public int batchSize;
    public static Random rand = new Random(System.currentTimeMillis());

    public ExpHelper(String configPath, int batchSize) throws IOException {
        this.batchSize = batchSize;
        this.timeBook = new ArrayList<>();

        Timer.timestamp();
        DNA.init(configPath);
        baseRelations = DNA.parser.getControlPlane().get("insert").stream().collect(Collectors.groupingBy(r -> r.getClass().getSimpleName()));
        timeBook.add(new TimeRecord("warmup", Timer.dumpTime()));
    }

    public ExpRecord runTransaction(String dscp, Map<String, List<Relation>> updates) throws IOException {
        // System.out.println(dscp);
        // System.out.println(updates.toString());
        // System.out.println();
        ArrayList<Long> timeCost = new ArrayList<>();
        Timer.timestamp();
        ArrayList<String> dpgOutput = DNA.generator.generateFibUpdates(updates);
        timeCost.add(Timer.dumpTime());

        ArrayList<String> dpvOutput = DNA.verifier.run(dpgOutput, null);
        timeCost.add(Timer.dumpTime());

        ExpRecord expRecord = new ExpRecord(dscp, updates, timeCost);
        expRecord.setDpgOutput(dpgOutput);
        expRecord.setDpvOutput(dpvOutput);
        return expRecord;
    }

    public List<ExpRecord> runSingleUpdate(Relation relation, int round) throws IOException{
        List<ExpRecord> res = new ArrayList<>();
        List<Relation> relList = new ArrayList<>();
        relList.add(relation);

        Map<String, List<Relation>> updates = new HashMap<>();
        updates.put("delete", relList);
        res.add(runTransaction(String.format("change_update_%d", round + 1), updates));

        Map<String, List<Relation>> restores = new HashMap<>();
        restores.put("insert", relList);
        res.add(runTransaction(String.format("change_restore_%d", round + 1), restores));
        return res;
    }

    /*
        Insert then delete
     */
    public List<ExpRecord> runSingleUpdateReverse(Relation relation, int round) throws IOException{
        List<ExpRecord> res = new ArrayList<>();
        List<Relation> relList = new ArrayList<>();
        relList.add(relation);

        Map<String, List<Relation>> updates = new HashMap<>();
        updates.put("insert", relList);
        res.add(runTransaction(String.format("change_update_%d", round + 1), updates));

        Map<String, List<Relation>> restores = new HashMap<>();
        restores.put("delete", relList);
        res.add(runTransaction(String.format("change_restore_%d", round + 1), restores));
        return res;
    }

    public List<ExpRecord> runBatchUpdate(String relationClazz) throws IOException{
        Timer.timestamp();
        List<Relation> relations = baseRelations.get(relationClazz);
        List<ExpRecord> res = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            Relation relation = relations.get(rand.nextInt(relations.size()));
            res.addAll(runSingleUpdate(relation, i));
        }
        return res;
    }

    public List<ExpRecord> runBatchUpdateReverse(String relationClazz) throws IOException{
        Timer.timestamp();
        List<Relation> relations = baseRelations.get(relationClazz);
        List<ExpRecord> res = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            Relation relation = relations.get(rand.nextInt(relations.size()));
            res.addAll(runSingleUpdateReverse(relation, i));
        }
        return res;
    }

    public void bookSumTime(String dscp, List<ExpRecord> records) {
        long updateTime = 0, updateDPGTime = 0, updateDPVTime = 0;
        long restoreTime = 0, restoreDPGTime = 0, restoreDPVTime = 0;
        for (ExpRecord record : records) {
            if (record.dscp.startsWith("change_update")) {
                updateDPGTime += record.timeCost.get(0);
                updateDPVTime += record.timeCost.get(1);
                updateTime += record.timeCost.stream().reduce(Long::sum).orElse(0L);
            } else if (record.dscp.startsWith("change_restore")) {
                restoreDPGTime += record.timeCost.get(0);
                restoreDPVTime += record.timeCost.get(1);
                restoreTime += record.timeCost.stream().reduce(Long::sum).orElse(0L);
            }
        }
        timeBook.add(new TimeRecord(dscp + "-update", updateTime / batchSize));
        timeBook.add(new TimeRecord(dscp + "-update-dpg", updateDPGTime / batchSize));
        timeBook.add(new TimeRecord(dscp + "-update-dpv", updateDPVTime / batchSize));
        timeBook.add(new TimeRecord(dscp + "-restore", restoreTime / batchSize));
        timeBook.add(new TimeRecord(dscp + "-restore-dpg", restoreDPGTime / batchSize));
        timeBook.add(new TimeRecord(dscp + "-restore-dpv", restoreDPVTime / batchSize));
    }

    public void saveResult(String dirName, List<ExpRecord> expRecords) {
        File dir = Paths.get(DNA.workingPath, dirName).toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (ExpRecord expRecord : expRecords) {
            String filename = expRecord.dscp;
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(Paths.get(dir.toString(), filename).toString()));
                bw.write("input: \n");
                for (Relation r : expRecord.input.getOrDefault("insert", new ArrayList<>())) {
                    bw.write(String.format("insert %s", r));
                }
                for (Relation r : expRecord.input.getOrDefault("delete", new ArrayList<>())) {
                    bw.write(String.format("delete %s", r));
                }
                bw.write(String.format("\nDPG time: %.3f ms\n", expRecord.timeCost.get(0) / 1e6));  // to ms
                for (String s : expRecord.dpgOutput) {
                    bw.write(s + "\n");
                }
                bw.write(String.format("\nDPV time: %.3f ms\n", expRecord.timeCost.get(1) / 1e6));
                for (String s : expRecord.dpvOutput) {
                    bw.write(s + "\n");
                }

                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void summaryTime() {
        try {
            FileWriter fw = new FileWriter(Paths.get(DNA.workingPath, "time_summary").toString());
            for (TimeRecord record : timeBook) {
                fw.write(record + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

