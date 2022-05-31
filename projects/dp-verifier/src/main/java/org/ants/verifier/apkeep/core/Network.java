package org.ants.verifier.apkeep.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.ants.verifier.apkeep.checker.*;
import org.ants.verifier.apkeep.element.*;
import org.ants.verifier.apkeep.utils.*;
import org.ants.verifier.common.*;

import javafx.util.Pair;

public class Network {
	
	protected String name; // network name

	protected HashMap<PositionTuple, HashSet<PositionTuple>> topology; // network topology
	protected HashMap<String,HashSet<String>> edge_ports;
	protected HashSet<String> end_hosts;
	
	/*
	 * The Port Predicate Map
	 * Each element has a set of ports, each of which is guarded by a set of predicates
	 */
	protected HashMap<String, Element> elements;
	
	/*
	 * The BDD data structure for encoding packet sets with Boolean formula
	 */
	protected BDDACLWrapper bdd_engine;
	
	/*
	 * The key data structure that handles the split and merge of predicates
	 */
	public APKeeper apk; // the APKeeper for forwarding devices
	
	protected HashSet<String> acl_node_names;
	
	public Network(String name) throws IOException
	{
		this.name = name;
		
		topology = new HashMap<PositionTuple, HashSet<PositionTuple>>();
		edge_ports = new HashMap<String,HashSet<String>>();
		end_hosts = new HashSet<>();
		
		elements = new HashMap<String, Element>();
		
		bdd_engine = new BDDACLWrapper();
		apk = null;
		
		acl_node_names = new HashSet<>();
		Element.setBDDWrapper(bdd_engine);
	}
	public void initializeNetwork(ArrayList<String> l1_links, ArrayList<String> edge_ports, Map<String, Map<String, Object>> dpDevices) {
		InitializeAPK();
		constructTopology(l1_links);
		setEdgePorts(edge_ports);
		setEndHosts(edge_ports);
		addHostsToTopology();
		parseACLConfigs(dpDevices);
	}
	/**
	 * add ForwardElement from layer ONE topology
	 * @param l1_link
	 */
	public void constructTopology(ArrayList<String> l1_link)
	{	
		for(String linestr : l1_link)
		{
			String[] tokens = linestr.split(" ");
			if(!elements.containsKey(tokens[0])){
				ForwardElement e = new ForwardElement(tokens[0]);
				elements.put(e.name, e);
				e.SetAPC(apk);
				e.Initialize();
			}
			if(!elements.containsKey(tokens[2])){
				ForwardElement e = new ForwardElement(tokens[2]);
				elements.put(e.name, e);
				e.SetAPC(apk);
				e.Initialize();
			}
			AddOneWayLink(tokens[0], tokens[1], tokens[2], tokens[3]);
		}
	}
	public void setEdgePorts(ArrayList<String> edge_port) {
		edge_ports.clear();
		for(String linestr : edge_port)
		{
			String[] tokens = linestr.split(" ");
			
			if(edge_ports.containsKey(tokens[0]) == false) {
				HashSet<String> ports = new HashSet<String>();
				ports.add(tokens[1]);
				
				edge_ports.put(tokens[0], ports);
			}
			else {
				HashSet<String> ports = edge_ports.get(tokens[0]);
				ports.add(tokens[1]);
			}
		}
	}
	/**
	 * every device who contains an edge port is an end host, as the network entrance of packets
	 * every edge port is an end host itself, as the network exit of packets
	 * @param edge_ports
	 */
	private void setEndHosts(ArrayList<String> edge_ports) {
		end_hosts = new HashSet<String>();
		for(String port : edge_ports) {
			end_hosts.add(port.split(" ")[0]);
			end_hosts.add(port.split(" ")[0]+","+port.split(" ")[1]);
		}
	}
	/**
	 * link edge port to its end host in topology
	 */
	public void addHostsToTopology() {
		for(String host : end_hosts) {
			String[] tokens = host.split(",");
			if(tokens.length != 2) continue;
			String d1 = host.split(",")[0];
			String p1 = host.split(",")[1];
			AddOneWayLink(d1,p1,host,"inport");
		}
	}
	public void attachACLNodeToTopology(String aclElement, String fwdElement, String port, String direction) {
		String nodeName = aclElement + "_" + port + "_" + direction;
		acl_node_names.add(nodeName);
		PositionTuple inpt = new PositionTuple(aclElement,"inport");
		if(direction.equals("in")) {
			PositionTuple pt2 = new PositionTuple(fwdElement,port);
			for(PositionTuple pt1 : topology.keySet()) {
				if(topology.get(pt1).contains(pt2)) {
					topology.get(pt1).remove(pt2);
					topology.get(pt1).add(inpt);
				}
			}
			AddOneWayLink(aclElement,"permit",fwdElement,port);
		}
		else if(direction.equals("out")){
			PositionTuple pt1 = new PositionTuple(fwdElement,port);
			for(PositionTuple pt2 : topology.get(pt1)) {
				AddOneWayLink(aclElement,"permit",pt2.getDeviceName(),pt2.getPortName());
			}
			topology.get(pt1).clear();
			topology.get(pt1).add(inpt);
		}
	}
	public void parseACLConfigs(Map<String, Map<String, Object>> dpDevices) {
		if(dpDevices == null) return;
		for (Map.Entry<String, Map<String, Object>> entry : dpDevices.entrySet()) {
		    String deviceName = entry.getKey();
//		    System.out.println(nodeName);
		    for (Map.Entry<String, Object> contents : entry.getValue().entrySet()) {
		        if (contents.getKey().equals("acl")) {
		            for (Map<String, Object> content : (List<Map<String, Object>>) contents.getValue()) {
		                for (Map.Entry<String, Object> aclContent : content.entrySet()) {
//		                    System.out.println(aclContent.getKey());
		                	String aclname = aclContent.getKey();
		                	String element = deviceName+"_"+aclname;
		                	ACLElement e = new ACLElement(element);
		    				elements.put(e.name, e);
		    				e.SetAPC(apk);
		    				e.Initialize();
		                	
		                    for (Map.Entry<String, Object> acl : ((Map<String, Object>) aclContent.getValue()).entrySet()) {
		                        if (acl.getKey().equals("applications")) {
		                            for (Map<String, String> binding : (List<Map<String, String>>) acl.getValue()) {
		                                String intf = binding.get("interface");
		                                String dir = binding.get("direction");
		                                attachACLNodeToTopology(element,deviceName,intf,dir);
		                            }
		                        }
		                    }
		                }
		            }
		        }
		    }
//		    System.out.println();
		}
	}
	/* 
	 * Network topology
	 */
	public void AddOneWayLink(String d1, String p1, String d2, String p2)
	{
		PositionTuple pt1 = new PositionTuple(d1, p1);
		PositionTuple pt2 = new PositionTuple(d2, p2);
		// links are one way
		if(topology.containsKey(pt1))
		{
			topology.get(pt1).add(pt2);
		}else
		{
			HashSet<PositionTuple> newset = new HashSet<PositionTuple>();
			newset.add(pt2);
			topology.put(pt1, newset);
		}
	}
	
	/*
	 * Initialize one instance of APKeeper
	 */
	public void InitializeAPK()
	{
		apk = new APKeeper(bdd_engine);

		for(Element e : elements.values()) {
			e.SetAPC(apk);
			e.Initialize();
		}
		
		apk.Initialize();
	}
	/*
	 * Process different types of rule update
	 */
	public HashMap<String,HashSet<Integer>> UpdateBatchRules(ArrayList<String> rules, 
			Map<String, Map<String, Object>> dpDevices, ReachabilityChecker rch_checker) {
		if(dpDevices != null) {
			ArrayList<String> acl_rules = parseDumpedJson(dpDevices);
			rules.addAll(acl_rules);
		}
		
		HashMap<String,HashSet<Integer>> moved_aps = new HashMap<String,HashSet<Integer>>();
		HashMap<String,HashMap<String,HashSet<Pair<String,String>>>> fwd_rules = new HashMap<String,HashMap<String,HashSet<Pair<String,String>>>>();
		for(String linestr : rules) {
			String[] tokens = linestr.split(" ");
			if (tokens[1].equals("fwd")) {
				addFWDRule(fwd_rules, linestr);
			}
			else if (tokens[1].equals("acl")) {
				/*
				 * single-rule model update for ACL rule
				 */
				UpdateACLRule(linestr,moved_aps, rch_checker);
			}
		}
		/*
		 * batched model update
		 */
		updateFWDRuleBatch(fwd_rules,moved_aps, rch_checker);
		
		apk.TryMergeAPBatch(moved_aps, rch_checker);
		return moved_aps;
	}
	public ArrayList<String> parseDumpedJson(Map<String, Map<String, Object>> dpDevices){
		ArrayList<String> parsed_rules = new ArrayList<>();
		for (Map.Entry<String, Map<String, Object>> entry : dpDevices.entrySet()) {
		    String deviceName = entry.getKey();
//		    System.out.println(nodeName);
		    for (Map.Entry<String, Object> contents : entry.getValue().entrySet()) {
		        if (contents.getKey().equals("acl")) {
		            for (Map<String, Object> content : (List<Map<String, Object>>) contents.getValue()) {
		                for (Map.Entry<String, Object> aclContent : content.entrySet()) {
//		                    System.out.println(aclContent.getKey());
		                	String aclname = aclContent.getKey();
		                	String element = deviceName+"_"+aclname;
		                    for (Map.Entry<String, Object> acl : ((Map<String, Object>) aclContent.getValue()).entrySet()) {
		                        if (acl.getKey().equals("rules")) {
		                            for (String rule : (List<String>) acl.getValue()) {
//		                                System.out.println(rule);
		                            	String aclrule = "+ acl "+element+" "+rule;
		                            	parsed_rules.add(aclname);
		                            }
		                        }
		                    }
		                }
		            }
		        }
		    }
//		    System.out.println();
		}
		return parsed_rules;
	}
	/**
	 * Reorganize forwarding rules by IP prefix, element, operation and interface
	 * @param fwd_rules
	 * @param linestr
	 */
	protected void addFWDRule(HashMap<String, HashMap<String, HashSet<Pair<String, String>>>> fwd_rules, String linestr) {
		String token = " ";
		String[] tokens = linestr.split(token);

		String op = tokens[0];
		String element_name = tokens[2];
		if(!elements.containsKey(element_name)) return;
		String ipInt = tokens[3];
		String prefixlen = tokens[4];
		String outport = tokens[5];
		String prio = tokens[6];
		String nexthop = tokens[7];
		
		/*
		 * filter control plane IP prefix
		 */
		if(nexthop.equals("0.0.0.0") 
				|| nexthop.toLowerCase().startsWith("loopback") 
				|| nexthop.equals("null")) {
			return;
		}
		
		/*
		 * firstly, categorize rules by IP prefix
		 */
		String ip = ipInt + "/" + prio;
		HashMap<String, HashSet<Pair<String, String>>> rules = fwd_rules.get(ip);
		if(rules == null) {
			rules = new HashMap<String, HashSet<Pair<String, String>>>();
		}
		/*
		 * secondly, categorize rules by Device
		 */
		HashSet<Pair<String,String>> actions = rules.get(element_name);
		if(actions == null) {
			actions = new HashSet<Pair<String,String>>();
		}
		/*
		 * finally, categorize rule by operation/interface
		 */
		Pair<String,String> pair = new Pair<String,String>(op,outport);
		actions.add(pair);
		rules.put(element_name, actions);
		fwd_rules.put(ip, rules);
	}
	protected ArrayList<ChangeItem> UpdateACLRule(String linestr, HashMap<String, HashSet<Integer>> moved_aps,
			ReachabilityChecker rch_checker) {
		String[] tokens = linestr.split(" ");

		ACLElement e = (ACLElement) elements.get(tokens[2]);
		/*
		 *	if (e == null) {
		 *		System.err.println("ACL element " + tokens[2] + " not found");
		 *		System.err.println(linestr);
		 *	}
		 */
		
		ACLRule r = new ACLRule(linestr.substring(tokens[0].length() + tokens[1].length() + tokens[2].length() + 3));

		/*
		 * compute change tuple
		 */
		ArrayList<ChangeItem> change_set = null;
		if (tokens[0].equals("+")){
			change_set = e.InsertACLRule(r);
		}
		else if (tokens[0].equals("-")){
			change_set = e.RemoveACLRule(r);
		}
		/*
		 * update PPM
		 */
		e.UpdatePortPredicateMap(change_set, linestr,moved_aps, rch_checker);
		return change_set;
	}
	protected void updateFWDRuleBatch(HashMap<String, HashMap<String, HashSet<Pair<String, String>>>> fwd_rules, 
			HashMap<String, HashSet<Integer>> moved_aps, ReachabilityChecker rch_checker) {
		ArrayList<String> updated_prefix = new ArrayList<String>();
		HashSet<String> updated_elements = new HashSet<String>();
		for(String ip : fwd_rules.keySet()) {
			updated_prefix.add(ip);
		}
		/*
		 * from longest to shortest
		 */
		Collections.sort(updated_prefix, new sortRulesByPriority());
		// first step - get the change_set & copy_set & remove_set
		HashMap<String, ArrayList<ChangeTuple>> change_set = new HashMap<String, ArrayList<ChangeTuple>>();
		HashMap<String, ArrayList<ChangeTuple>> remove_set = new HashMap<String, ArrayList<ChangeTuple>>();
		HashMap<String, ArrayList<ChangeTuple>> copyto_set = new HashMap<String, ArrayList<ChangeTuple>>();
		for(String ip : updated_prefix) {
			for(String element_name : fwd_rules.get(ip).keySet()) {
				HashSet<Pair<String,String>> actions = fwd_rules.get(ip).get(element_name);
				
				HashSet<String> to_ports = new HashSet<String>();
				HashSet<String> from_ports = new HashSet<String>();
				
				for(Pair<String,String> pair : actions) {
					if(pair.getKey().equals("+")) to_ports.add(pair.getValue());
					else if(pair.getKey().equals("-")) from_ports.add(pair.getValue());
				}
				
				// filter ports remained unchange
				HashSet<String> retained = new HashSet<String>(to_ports);
				retained.retainAll(from_ports);
				if(!retained.isEmpty()) {
					to_ports.removeAll(retained);
					from_ports.removeAll(retained);
				}
				
				ForwardElement e = (ForwardElement) elements.get(element_name);
				if(e == null) {
					System.err.println("Forwarding element " + element_name + " not found");
					System.exit(1);
				}
				e.updateFWRuleBatch(ip,to_ports,from_ports,change_set, copyto_set,remove_set);
				updated_elements.add(element_name);
			}
		}
		// the second step - get the moved_set
		HashMap<Integer, HashSet<Integer>> pred_aps = new HashMap<Integer,HashSet<Integer>>(); 
		HashMap<Integer, HashMap<String, HashSet<String>>> remove_ports = new HashMap<Integer, HashMap<String, HashSet<String>>>();
		HashMap<Integer, HashMap<String, HashSet<String>>> add_ports = new HashMap<Integer, HashMap<String, HashSet<String>>>();
		for(String element_name : updated_elements) {
			ForwardElement e = (ForwardElement) elements.get(element_name);
			if(e == null) {
				System.err.println("Forwarding element " + element_name + " not found");
				System.exit(1);
			}
			e.UpdatePortPredicateMap(change_set.get(element_name),copyto_set.get(element_name),
					remove_set.get(element_name), moved_aps, pred_aps,
					remove_ports, add_ports, rch_checker);
		}
		// the third step - update PPM in APKeeper
		for(int ap : remove_ports.keySet()) {
			if(add_ports.containsKey(ap)) {
				apk.UpdateTransferAPBatch(ap, remove_ports.get(ap), add_ports.get(ap));
			}
			else {
				apk.UpdateTransferAPBatch(ap, remove_ports.get(ap), new HashMap<String, HashSet<String>>());
			}
		}
		for(int ap : add_ports.keySet()) {
			if(remove_ports.containsKey(ap)) continue;
			apk.UpdateTransferAPBatch(ap, new HashMap<String, HashSet<String>>(), add_ports.get(ap));
		}
	}
	public HashMap<PositionTuple, HashSet<PositionTuple>> getTopology()
	{
		return topology;
	}
	
	public HashMap<String, Element> getElements()
	{
		return elements;
	}
	
	public HashSet<String> getEndHosts(){
		return end_hosts;
	}
	
	public HashSet<String> getACLNodes(){
		return acl_node_names;
	}
	public long getAPNum() {
		return apk.getAPNum();
	}
	public void writeReachabilityMatrix(String file, Hashtable<String,Hashtable<String,HashSet<Integer>>> reachable_matrix) throws IOException {
		File output_file = new File(file);
		FileWriter output_writer = new FileWriter(output_file);
		for(String srcNode : reachable_matrix.keySet()) {
			for(String dstNode : reachable_matrix.get(srcNode).keySet()) {
				if(srcNode.equals(dstNode)) continue;
//				if(srcNode.split(",").length == 2) continue;
				output_writer.write(srcNode+"->"+dstNode+":\n");
				output_writer.write(apk.getAPPrefixes(reachable_matrix.get(srcNode).get(dstNode))+"\n");
			}
		}
		output_writer.close();
	}
	public void writeReachabilityChanges(String file, HashMap<Pair<String, String>,HashMap<String, HashSet<Integer>>> reach_change) throws IOException {
		File output_file = new File(file);
		FileWriter output_writer = new FileWriter(output_file);
		for(Pair<String, String> host_pair : reach_change.keySet()) {
			HashSet<Integer> added_aps = reach_change.get(host_pair).get("+");
			HashSet<Integer> removed_aps = reach_change.get(host_pair).get("-");
			
			if(added_aps != null && removed_aps !=null) {
				HashSet<Integer> retained_aps = new HashSet<>(added_aps);
				retained_aps.retainAll(removed_aps);
				
				added_aps.removeAll(retained_aps);
				removed_aps.removeAll(retained_aps);
			}
			if(added_aps != null && !added_aps.isEmpty()) {
				output_writer.write("+ "+host_pair.getKey()+"->"+host_pair.getValue()+": " + apk.getAPPrefixes(added_aps) + "\n");
			}
			if(removed_aps != null && !removed_aps.isEmpty()) {
				output_writer.write("- "+host_pair.getKey()+"->"+host_pair.getValue()+": " + apk.getAPPrefixes(removed_aps) + "\n");
			}
		}
		output_writer.close();
	}

	public ArrayList<String> getReachabilityChanges(HashMap<Pair<String, String>,HashMap<String, HashSet<Integer>>> reach_change) {
		ArrayList<String> changes = new ArrayList<>();
		for(Map.Entry<Pair<String, String>,HashMap<String, HashSet<Integer>>> entry: reach_change.entrySet()) {
			Pair<String, String> host_pair = entry.getKey();
			HashSet<Integer> added_aps = entry.getValue().get("+");
			HashSet<Integer> removed_aps = entry.getValue().get("-");

			if(added_aps != null && removed_aps !=null) {
				HashSet<Integer> retained_aps = new HashSet<>(added_aps);
				retained_aps.retainAll(removed_aps);

				added_aps.removeAll(retained_aps);
				removed_aps.removeAll(retained_aps);
			}
			if(added_aps != null && !added_aps.isEmpty()) {
				changes.add("+ "+host_pair.getKey()+"->"+host_pair.getValue()+": " + apk.getAPPrefixes(added_aps));
			}
			if(removed_aps != null && !removed_aps.isEmpty()) {
				changes.add("- "+host_pair.getKey()+"->"+host_pair.getValue()+": " + apk.getAPPrefixes(removed_aps));
			}
		}
		return changes;
	}
}
class sortRulesByPriority implements Comparator<Object> {
	@Override
    public int compare(Object o1, Object o2) {
		String ip1 = (String) o1;
		String ip2 = (String) o2;
    	int p1 = Integer.valueOf(ip1.split("/")[1]);
    	int p2 = Integer.valueOf(ip2.split("/")[1]);
    	return  p1-p2;
    }
}
