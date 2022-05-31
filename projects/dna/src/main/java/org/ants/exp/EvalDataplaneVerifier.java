package org.ants.exp;

import org.ants.main.DNA;
import org.ants.verifier.DPVerifier;
import org.ants.verifier.apkeep.utils.UtilityTools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class EvalDataplaneVerifier {
    private static final String currentPath = System.getProperty("user.dir");

    public static void runFattreeUpdate(String configPath) throws IOException {
        configPath = Paths.get(configPath).toRealPath().toString();
        String testcase = Paths.get(configPath).toRealPath().getFileName().toString();
        String inPath = Paths.get(configPath, "dpv").toString();
        String outPath = Paths.get(currentPath, "results", testcase).toString();
        File dir = Paths.get(outPath).toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String topoFile = Paths.get(configPath, "layer1Topology").toString();
        String edgePortFile = Paths.get(configPath, "edgePorts").toString();


        ArrayList<String> topo = UtilityTools.readFile(topoFile);
        ArrayList<String> edge_ports = UtilityTools.readFile(edgePortFile);
        File[] inFiles = Paths.get(inPath).toFile().listFiles();
        for (File folder : inFiles) {
            if (!folder.isDirectory()) continue;
            System.out.println(folder.getName());
            String updateFolder = folder.getAbsolutePath();
            DPVerifier dpv = new DPVerifier(testcase, topo, edge_ports, null);

            // update base rules
            String baseFile = Paths.get(updateFolder, "change_base").toString();
            ArrayList<String> forwarding_rules = UtilityTools.readFile(baseFile);
            dpv.run(forwarding_rules, null);
            dpv.dumpResults(Paths.get(outPath, "change_base").toString());
            dpv.dpm_time = 0;
            dpv.dpv_time = 0;

            // update changes
            int len = Paths.get(updateFolder).toFile().listFiles().length;
            for (int i = 1; i <= len; i++) {
                System.out.println("round " + i);

                String update = Paths.get(updateFolder, "change_update_" + i).toString();
                File udpateFile = Paths.get(update).toFile();
                if (!udpateFile.exists()) {
                    break;
                }
                forwarding_rules = UtilityTools.readFile(update);
                dpv.run(forwarding_rules, null);
                dpv.dumpResults(Paths.get(outPath, "change_update_" + i).toString());

                String restore = Paths.get(updateFolder, "change_restore_" + i).toString();
                File restoreFile = Paths.get(restore).toFile();
                if (!restoreFile.exists()) {
                    continue;
                }
                forwarding_rules = UtilityTools.readFile(restore);
                dpv.run(forwarding_rules, null);
                dpv.dumpResults(Paths.get(outPath, "change_restore_" + i).toString());
            }
            System.out.println(dpv.dpm_time / 1e6 + "ms " + dpv.dpv_time / 1e6 + "ms");
        }
    }

    public static void main(String[] args) throws IOException {
        String configPath = args[0];
        // String configPath = "../networks/fattree/bgp/bgp_fattree20/";
        // String configPath = "../networks/config2spec-networks/bics/bgp/";
        runFattreeUpdate(configPath);
    }
}
