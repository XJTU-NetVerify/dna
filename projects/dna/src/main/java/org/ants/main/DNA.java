package org.ants.main;

import com.alibaba.fastjson.JSON;
import org.ants.generator.DPGenerator;
import org.ants.parser.ConfigParser;
import org.ants.parser.relation.Relation;
import org.ants.verifier.DPVerifier;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class DNA {
    private static final String currentPath = System.getProperty("user.dir");
    public static String workingPath;
    public static String testcase;

    public static ConfigParser parser;
    public static DPGenerator generator;
    public static DPVerifier verifier;

    public static void init(String configPath) throws IOException {
        configPath = Paths.get(configPath).toRealPath().toString();
        testcase = Paths.get(configPath).toRealPath().getFileName().toString();
        workingPath = Paths.get(currentPath, "results", testcase).toString();

        parser = new ConfigParser(testcase, configPath, workingPath);
        Map<String, List<Relation>> configUpdates = parser.getControlPlaneDiff(null);
        HashSet<String> topos = parser.getTopology();
        HashSet<String> edgePorts = parser.getEdgePorts();
        Map<String, Map<String, Object>> dpDevices = parser.getDataPlaneModels();

        generator = new DPGenerator();
        ArrayList<String> fibUpdates = generator.generateFibUpdates(configUpdates);

        verifier = new DPVerifier(testcase, new ArrayList<>(topos), new ArrayList<>(edgePorts), dpDevices);
        ArrayList<String> dpChanges = verifier.run(fibUpdates, null);
    }

    public static void update() throws IOException {
        parser.updateConfig();
        Map<String, List<Relation>> configUpdates = parser.getControlPlaneDiff(null);
        ArrayList<String> fibUpdates = generator.generateFibUpdates(configUpdates);
        ArrayList<String> dpChanges = verifier.run(fibUpdates, null);
    }

    public static void update(String configPath) throws IOException {
        configPath = Paths.get(configPath).toRealPath().toString();
        parser.updateConfig(configPath);
        Map<String, List<Relation>> configUpdates = parser.getControlPlaneDiff(null);
        ArrayList<String> fibUpdates = generator.generateFibUpdates(configUpdates);
        ArrayList<String> dpChanges = verifier.run(fibUpdates, null);
    }

    public static void dumpDDlogInput(PrintStream printer) {
        printer.println(parser.getDDlogInputText());
    }

    public static void dumpTopo(PrintStream printer) {
        HashSet<String> topos = parser.getTopology();
        for (String line : topos) {
            printer.println(line);
        }
    }

    public static void dumpEdgePorts(PrintStream printer) {
        HashSet<String> edgePorts = parser.getEdgePorts();
        for (String line : edgePorts) {
            printer.println(line);
        }
    }

    public static void dumpFib(PrintStream printer) {
        ArrayList<String> fib = generator.getFib();
        for (String line : fib) {
            printer.println(line);
        }
    }

    public static void dumpPolicy(PrintStream printer) {
        ArrayList<String> policies = verifier.getPolicies();
        for (String line : policies) {
            printer.println(line);
        }
    }

    // Dump acl and vlan per device to JSONs.
    public static void dumpDPDevices() throws IOException {
        Path outputPath = Paths.get(workingPath, "dp_device_model");
        if (!outputPath.toFile().exists()) {
            outputPath.toFile().mkdirs();
        }

        Map<String, Map<String, Object>> dpDevices = parser.getDataPlaneModels();
        for (Map.Entry<String, Map<String, Object>> entry : dpDevices.entrySet()) {
            String nodeName = entry.getKey();
            String json = JSON.toJSONString(entry.getValue());

            PrintStream printer = new PrintStream(new BufferedOutputStream(new FileOutputStream(Paths.get(outputPath.toString(), nodeName + "_config.json").toString())), true);
            printer.println(json);
        }
    }

    public static void dumpAllToFile() throws IOException {
        dumpDDlogInput(new PrintStream(new BufferedOutputStream(new FileOutputStream(Paths.get(workingPath, "change_base").toString())), true));
        dumpTopo(new PrintStream(new BufferedOutputStream(new FileOutputStream(Paths.get(workingPath, "topo").toString())), true));
        dumpEdgePorts(new PrintStream(new BufferedOutputStream(new FileOutputStream(Paths.get(workingPath, "edge_ports").toString())), true));
        dumpFib(new PrintStream(new BufferedOutputStream(new FileOutputStream(Paths.get(workingPath, "fib").toString())), true));
        dumpPolicy(new PrintStream(new BufferedOutputStream(new FileOutputStream(Paths.get(workingPath, "policy").toString())), true));
        dumpDPDevices();
    }
}
