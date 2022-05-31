package org.ants.verifier;

import java.io.IOException;
import java.util.*;

import org.ants.verifier.apkeep.checker.ReachabilityChecker;
import org.ants.verifier.apkeep.core.Network;

public class DPVerifier {
    private final Network apkeepNetworkModel;
    private final ReachabilityChecker apkeepVerifier;
	private ArrayList<String> policies;

    public long dpm_time;
    public long dpv_time;

    public DPVerifier(String network_name, ArrayList<String> topo, ArrayList<String> edge_ports,
    		Map<String, Map<String, Object>> dpDevices) throws IOException{
    	apkeepNetworkModel = new Network(network_name);
    	apkeepNetworkModel.initializeNetwork(topo, edge_ports, dpDevices);

    	apkeepVerifier = new ReachabilityChecker(apkeepNetworkModel);
    	apkeepVerifier.initializeChecker(apkeepNetworkModel);

    	dpm_time=0;
    	dpv_time=0;
    }

    public ArrayList<String> run(ArrayList<String> forwarding_rules, Map<String, Map<String, Object>> dpDevices) throws IOException{
    	long t0 = System.nanoTime();
    	HashMap<String,HashSet<Integer>> moved_aps = apkeepNetworkModel.UpdateBatchRules(forwarding_rules, dpDevices, apkeepVerifier);
    	long t1 = System.nanoTime();
    	apkeepVerifier.updateReachability(moved_aps);
    	long t2 = System.nanoTime();
    	dpm_time+=(t1-t0);
    	dpv_time+=(t2-t1);
   		// System.out.println(apkeepNetworkModel.getAPNum()+" "+(t1-t0)/1000000+" "+(t2-t1)/1000000);

		policies = apkeepNetworkModel.getReachabilityChanges(apkeepVerifier.getChanges());
		return policies;
    }

	public ArrayList<String> getPolicies() {
		return policies;
	}

    public void dumpResults(String outputPath) throws IOException{
    	apkeepNetworkModel.writeReachabilityChanges(outputPath, apkeepVerifier.getChanges());
    }

}
