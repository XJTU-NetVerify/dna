package org.ants.verifier.apkeep.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import org.ants.verifier.apkeep.core.*;
import org.ants.verifier.apkeep.element.*;
import org.ants.verifier.apkeep.utils.*;
import org.ants.verifier.common.*;

import javafx.util.Pair;

public class ReachabilityChecker {
	
	HashMap<PositionTuple, HashSet<PositionTuple>> topology;
	protected HashMap<String, Element> elements;
	HashSet<String> end_hosts;
	
	Hashtable<Integer, Hashtable<String, Hashtable<String, Integer>>> src_set;
	Hashtable<String,Hashtable<String, HashSet<Integer>>> pair_to_ap;
	
	HashMap<String, HashMap<String,HashSet<Integer>>> old_port_aps;
	
	/*
	 * local variants for BFS
	 */
	LinkedList<TraversalItem> queue;
	// start node and its affected APs
	HashMap<String, HashSet<Integer>> start_map;
	
	// traverse relation for src_set update
	HashMap<String, HashMap<String,HashSet<Integer>>> src_to_nodes;
	HashMap<Pair<String,String>,HashSet<Integer>> startNodesTree;

	HashMap<Pair<String, String>,HashMap<String, HashSet<Integer>>> reach_change;
	
	public ReachabilityChecker(Network n) {
		src_set = new Hashtable<>();
		pair_to_ap = new Hashtable<>();
		end_hosts = new HashSet<String>();
		old_port_aps = new HashMap<String, HashMap<String,HashSet<Integer>>>();
		
		queue = new LinkedList<TraversalItem>();
		start_map = new HashMap<String, HashSet<Integer>>();
		src_to_nodes = new HashMap<String, HashMap<String,HashSet<Integer>>>();
		startNodesTree = new HashMap<Pair<String,String>,HashSet<Integer>>();
		
		reach_change = new HashMap<>();
	}
	public void initializeChecker(Network net) {
		topology = net.getTopology();
		elements = net.getElements();
		end_hosts = net.getEndHosts();
		initializeReachabilityMatrix();
		setOldGraph();
	}
	public void setOldGraph() {
		old_port_aps.clear();
		for(Element e : elements.values()) {
			HashMap<String,HashSet<Integer>> port_aps_raw = new HashMap<String,HashSet<Integer>>();
			for(String port : e.port_aps_raw.keySet()) {
				port_aps_raw.put(port, new HashSet<Integer>(e.get_port_aps(port)));
			}
			old_port_aps.put(e.name, port_aps_raw);
		}
	}
	public void initializeReachabilityMatrix() {
		Hashtable<String,Hashtable<String,Integer>> origin_matrix = new Hashtable<>();
		Hashtable<String,Hashtable<String,HashSet<LinkedList<String>>>> origin_paths = new Hashtable<>();
		for(String host : end_hosts) {
			if(host.split(",").length == 2) continue;
			Hashtable<String,Integer> origin_paths1 = new Hashtable<String,Integer>();
			origin_paths1.put(host, 1);
			origin_matrix.put(host, origin_paths1);
			
			HashSet<Integer> trueSet = new HashSet<Integer>();
			trueSet.add(BDDACLWrapper.BDDTrue);
			pair_to_ap.put(host, new Hashtable<>());
			pair_to_ap.get(host).put(host, trueSet);
		}
		src_set.put(BDDACLWrapper.BDDTrue, origin_matrix);
	}

	public boolean isEndHost(String device) {
		return end_hosts.contains(device);
	}
	public HashSet<PositionTuple> LinkTransfer(PositionTuple pt)
	{	
		return topology.get(pt);
	}
	public void splitReach(int origin, int ap1, int ap2) {
		Hashtable<String,Hashtable<String,Integer>> ap1_matrix = new Hashtable<String,Hashtable<String,Integer>>();
		Hashtable<String,Hashtable<String,Integer>> ap2_matrix = new Hashtable<String,Hashtable<String,Integer>>();
		Hashtable<String,Hashtable<String,Integer>> origin_matrix = src_set.get(origin);
		
		// the origin has no path
		if(origin_matrix == null) {
			return;
		}
		
		UtilityTools.copyMatrixTo(origin_matrix,ap1_matrix);
		UtilityTools.copyMatrixTo(origin_matrix,ap2_matrix);
		
		
		src_set.remove(origin);
		src_set.put(ap1, ap1_matrix);
		src_set.put(ap2, ap2_matrix);
		
		//update pair_to_ap
		for(String device : ap1_matrix.keySet()) {
			if(!isEndHost(device)) continue;
			Hashtable<String,Integer> matrix = ap1_matrix.get(device);
			for(String src_device : matrix.keySet()) {
				if(!isEndHost(src_device)) continue;
				
				HashSet<Integer> pair_aps = pair_to_ap.get(src_device).get(device);
				pair_aps.remove(origin);
				pair_aps.add(ap1);
				pair_aps.add(ap2);
			}
		}
		for(String name : old_port_aps.keySet()) {
			for(String port : old_port_aps.get(name).keySet()) {
				if(old_port_aps.get(name).get(port).contains(origin)){
					old_port_aps.get(name).get(port).remove(origin);
					old_port_aps.get(name).get(port).add(ap1);
					old_port_aps.get(name).get(port).add(ap2);
				}
			}
		}
	}

	public void mergeReach(int merged_ap, HashSet<Integer> aps) {
		Hashtable<String,Hashtable<String,Integer>> ap1_matrix = src_set.get(aps.toArray()[0]);
		Hashtable<String,Hashtable<String,Integer>> merged_matrix = new Hashtable<>();
		
		if(ap1_matrix == null) {
			return;
		}
		else {
			UtilityTools.copyMatrixTo(ap1_matrix,merged_matrix);
		}
		
		for(Integer ap : aps) {
			src_set.remove(ap);
		}
		src_set.put(merged_ap, merged_matrix);
		
		//update pair_to_ap
		for(String device : merged_matrix.keySet()) {
			if(!isEndHost(device)) continue;
			Hashtable<String,Integer> matrix = merged_matrix.get(device);
			for(String src_device : matrix.keySet()) {
				if(!isEndHost(src_device)) continue;
				
				HashSet<Integer> pair_aps = pair_to_ap.get(src_device).get(device);
				pair_aps.removeAll(aps);
				pair_aps.add(merged_ap);
			}
		}
		
		int ap1 = (int) aps.toArray()[0];
		for(String name : old_port_aps.keySet()) {
			for(String port : old_port_aps.get(name).keySet()) {
				if(old_port_aps.get(name).get(port).contains(ap1)){
					old_port_aps.get(name).get(port).removeAll(aps);
					old_port_aps.get(name).get(port).add(merged_ap);
				}
			}
		}
	}
	private void updateSrcSet(String type) {
		// update start_nodes' src_set
		HashMap<Integer, HashSet<Pair<String,String>>> ap_edges = new HashMap<Integer, HashSet<Pair<String,String>>>();
		for(Pair<String,String> one_edge : startNodesTree.keySet()) {
			HashSet<Integer> aps = startNodesTree.get(one_edge);
			for(Integer ap : aps) {
				HashSet<Pair<String,String>> edges = ap_edges.get(ap);
				if(edges == null) {
					edges = new HashSet<Pair<String,String>>();
				}
				edges.add(one_edge);
				ap_edges.put(ap, edges);
			}
		}
		if(type.equals("delete")) {
			updateNonStartNodes(type);
			updateStartNodes(ap_edges, type);
		}
		else if(type.equals("add")) {
			updateStartNodes(ap_edges, type);
			updateNonStartNodes(type);
		}
	}
	private void updateStartNodes(HashMap<Integer, HashSet<Pair<String, String>>> ap_edges,String type) {
		for(Integer ap : ap_edges.keySet()) {
			if(!src_set.containsKey(ap)) {
				System.exit(0);
			}
			HashSet<Pair<String,String>> edges = ap_edges.get(ap);
			HashMap<String,HashSet<String>> one_way_edges = new HashMap<String,HashSet<String>>();
			// topo sort
			HashMap<String,Integer> indegree = new HashMap<String, Integer>();
			for(Pair<String,String> edge : edges) {
				String src = edge.getKey();
				String dst = edge.getValue();
				if(type.equals("add")) {
					src = edge.getKey();
					dst = edge.getValue();
				}
				else if(type.equals("delete")) {
					dst = edge.getKey();
					src = edge.getValue();
				}
				if(!indegree.containsKey(src)) indegree.put(src,0);
				if(!indegree.containsKey(dst)) indegree.put(dst, 1);
				else indegree.put(dst, indegree.get(dst)+1);
				
				HashSet<String> next_nodes = one_way_edges.get(src);
				if(next_nodes == null) {
					next_nodes = new HashSet<String>();
				}
				next_nodes.add(dst);
				one_way_edges.put(src,next_nodes);
			}
			LinkedList<String> q = new LinkedList<String>();
			for(String node : indegree.keySet()) {
				if(indegree.get(node) == 0) q.addLast(node);
			}
			// start update
			while(!q.isEmpty()) {
				String start_node = q.getFirst();
				q.removeFirst();
				if(!one_way_edges.containsKey(start_node)) continue;
				for(String next_node : one_way_edges.get(start_node)) {
					indegree.put(next_node, indegree.get(next_node)-1);
					if(indegree.get(next_node) == 0) q.addLast(next_node);
					// update reachable_matrix
					if(type.equals("delete")) {
						String temp = start_node;
						start_node = next_node;
						next_node = temp;
						Hashtable<String, Integer> start_paths = src_set.get(ap).get(start_node);
						if(start_paths == null) {
							// start node is unreachable from any hosts
							next_node = start_node;
							start_node = temp;
							continue;
						}
						Hashtable<String, Integer> next_paths = src_set.get(ap).get(next_node);
						if(next_paths == null) {
							System.exit(0);
						}
						for(String src_node : start_paths.keySet()) {
							if(!next_paths.containsKey(src_node)) {
								System.exit(0);
							}
							if(next_paths.get(src_node) < start_paths.get(src_node)) {
								System.exit(0);
							}
							next_paths.put(src_node, next_paths.get(src_node) - start_paths.get(src_node));
							
							if(next_paths.get(src_node) == 0) {
								next_paths.remove(src_node);
								if(isEndHost(next_node)) {
									HashSet<Integer> reachable_aps = pair_to_ap.get(src_node).get(next_node);
									if(reachable_aps == null) {
										System.exit(1);
									}
									reachable_aps.remove(ap);
									if(reachable_aps.isEmpty()) {
										pair_to_ap.get(src_node).remove(next_node);
									}
									Pair<String,String> host_pair = new Pair<>(src_node, next_node);
									if(!reach_change.containsKey(host_pair)) reach_change.put(host_pair, new HashMap<>());
									if(!reach_change.get(host_pair).containsKey("-")) reach_change.get(host_pair).put("-", new HashSet<>());
									reach_change.get(host_pair).get("-").add(ap);
								}
							}
						}
						next_node = start_node;
						start_node = temp;
					}
					else if(type.equals("add")) {
						Hashtable<String, Integer> start_paths = src_set.get(ap).get(start_node);
						if(start_paths == null) {
							// start node is unreachable from any hosts
							continue;
						}
						Hashtable<String, Integer> next_paths = src_set.get(ap).get(next_node);
						if(next_paths == null) {
							next_paths = new Hashtable<String, Integer>();
						}
						for(String src_node : start_paths.keySet()) {
							if(!next_paths.containsKey(src_node)) {
								int index = start_paths.get(src_node);
								next_paths.put(src_node, index);
							}
							else {
								next_paths.put(src_node, next_paths.get(src_node) + start_paths.get(src_node));
							}
							if(isEndHost(next_node)) {
								HashSet<Integer> pair_aps = pair_to_ap.get(src_node).get(next_node);
								if(pair_aps == null) {
									pair_aps = new HashSet<Integer>();
								}
								if(!pair_aps.contains(ap)) {
									Pair<String,String> host_pair = new Pair<>(src_node, next_node);
									if(!reach_change.containsKey(host_pair)) reach_change.put(host_pair, new HashMap<>());
									if(!reach_change.get(host_pair).containsKey("+")) reach_change.get(host_pair).put("+", new HashSet<>());
									reach_change.get(host_pair).get("+").add(ap);
								}
								pair_aps.add(ap);
								pair_to_ap.get(src_node).put(next_node,pair_aps);
							}
						}
						src_set.get(ap).put(next_node,next_paths);
					}
				}
			}
		}
	}
	private void updateNonStartNodes(String type) {
		for(String start_node : src_to_nodes.keySet()) {
			for(String node : src_to_nodes.get(start_node).keySet()) {
				for(Integer ap : src_to_nodes.get(start_node).get(node)) {
					if(!src_set.containsKey(ap)) {
						System.exit(0);
					}
					Hashtable<String, Integer> start_paths = src_set.get(ap).get(start_node);
					if(start_paths == null) {
						// start node is unreachable from any hosts
						continue;
					}
					if(type.equals("delete")) {
						Hashtable<String, Integer> node_paths = src_set.get(ap).get(node);
						if(node_paths == null) {
							System.exit(0);
						}
						for(String src_node : start_paths.keySet()) {
							if(!node_paths.containsKey(src_node)) {
								System.exit(0);
							}
							if(node_paths.get(src_node) < start_paths.get(src_node)) {
								System.exit(0);
							}
							node_paths.put(src_node, node_paths.get(src_node) - start_paths.get(src_node));
							// path change
							if(node_paths.get(src_node) == 0) {
								node_paths.remove(src_node);
								if(isEndHost(node)) {
									HashSet<Integer> reachable_aps = pair_to_ap.get(src_node).get(node);
									if(reachable_aps == null) {
										System.exit(1);
									}
									reachable_aps.remove(ap);
									// edge change
									if(reachable_aps.isEmpty()) {
										pair_to_ap.get(src_node).remove(node);
									}
									Pair<String,String> host_pair = new Pair<>(src_node, node);
									if(!reach_change.containsKey(host_pair)) reach_change.put(host_pair, new HashMap<>());
									if(!reach_change.get(host_pair).containsKey("-")) reach_change.get(host_pair).put("-", new HashSet<>());
									reach_change.get(host_pair).get("-").add(ap);
								}
							}
						}
					}
					else if(type.equals("add")) {
						Hashtable<String, Integer> node_paths = src_set.get(ap).get(node);
						if(node_paths == null) {
							node_paths = new Hashtable<String, Integer>();
						}
						for(String src_node : start_paths.keySet()) {
							if(!node_paths.containsKey(src_node)) {
								int index = start_paths.get(src_node);
								node_paths.put(src_node, index);
							}
							else {
								node_paths.put(src_node, node_paths.get(src_node) + start_paths.get(src_node));
							}
							// path change
							if(isEndHost(node)) {
								HashSet<Integer> pair_aps = pair_to_ap.get(src_node).get(node);
								if(pair_aps == null) {
									pair_aps = new HashSet<Integer>();
								}
								if(!pair_aps.contains(ap)) {
									Pair<String,String> host_pair = new Pair<>(src_node, node);
									if(!reach_change.containsKey(host_pair)) reach_change.put(host_pair, new HashMap<>());
									if(!reach_change.get(host_pair).containsKey("+")) reach_change.get(host_pair).put("+", new HashSet<>());
									reach_change.get(host_pair).get("+").add(ap);
								}
								pair_aps.add(ap);
								pair_to_ap.get(src_node).put(node,pair_aps);
							}
						}
						src_set.get(ap).put(node,node_paths);
					}
				}
			}
		}
	}
	public HashMap<ChangeItem,Integer> updateReachability(
			HashMap<String, HashSet<Integer>> moved_aps) {
		reach_change.clear();
		HashSet<Integer> all_moved_aps = new HashSet<Integer>();
		for(String element_name : moved_aps.keySet()) {
			HashSet<Integer> aps = moved_aps.get(element_name);
			all_moved_aps.addAll(aps);
		}
		if(all_moved_aps.isEmpty()) return null;
		TraverseForwardingGraphNoConstruction(moved_aps, "old");
		updateSrcSet("delete");
		TraverseForwardingGraphNoConstruction(moved_aps, "new");
		updateSrcSet("add");
		
		setOldGraph();
		return null;
	}
	private void pushInQueue(String node, HashSet<Integer> aps, TraversalItem last_hop) {
		for(TraversalItem ti : queue) {
			if(!ti.nodeName.equals(node)) continue;
			ti.atoms.addAll(aps);
			ti.updateSourceMap(last_hop,aps);
			return;
		}
		TraversalItem ti = new TraversalItem(node, aps);
		ti.updateSourceMap(last_hop,aps);
		queue.add(ti);
	}
	private void popOutQueue() {
		queue.removeFirst();
	}
	private void getStartNodesNoConstruction(HashMap<String, HashSet<Integer>> moved_aps) {
		start_map.clear();
		boolean isacl = false;
		for(String element_name : moved_aps.keySet()) {
			String start_node = element_name;
			Element e = elements.get(element_name);
			if(e == null) {
				// acl element
				start_node = element_name.split("_")[0];
			}
			// fwd element
			if(start_map.containsKey(start_node)) {
				start_map.get(start_node).addAll(moved_aps.get(element_name));
			}
			else {
				start_map.put(start_node,new HashSet<Integer>(moved_aps.get(element_name)));
			}
		}
		for(String element_name : start_map.keySet()) {
			TraversalItem ti = new TraversalItem(element_name,start_map.get(element_name));
			ti.setSourceMap(element_name,start_map.get(element_name));
			pushInQueue(element_name, start_map.get(element_name),ti);
		}
	}
	private void traverseFromNodeBatchNoConstruction(TraversalItem current_hop, String type) {
		String node_name = current_hop.nodeName;
		Element e = elements.get(node_name);
		if(e == null) {
			// acl node or a host node
			if(isEndHost(node_name)) return;
			for(Element element : elements.values()) {
				if(element instanceof ForwardElement) continue;
				if(node_name.startsWith(element.name)) {
					e = element;
					break;
				}
			}
			if(e == null) {
				System.err.println("element "+node_name+" not found!");
				return;
			}
		}
		HashMap<String, HashSet<Integer>> port_aps_raw = null;
		if(type.equals("old")) {
			port_aps_raw = old_port_aps.get(e.name);
		}
		else if(type.equals("new")){
			port_aps_raw = e.port_aps_raw;
		}
		if(port_aps_raw == null) {
			System.out.println(e.name + " " + e.port_aps_raw);
		}
		for(String port : port_aps_raw.keySet()) {
			if(port.equals("default")) continue;
			PositionTuple pt = new PositionTuple(node_name, port);
			if(LinkTransfer(pt) == null) continue; // no next hop
			// filter aps with reference of edge
			HashSet<Integer> aps = new HashSet<Integer>(current_hop.atoms);
			aps.retainAll(port_aps_raw.get(port));
			if(aps.isEmpty()) {
				continue;
			}
			for(PositionTuple connected_pt : LinkTransfer(pt)) {
				String next_node = connected_pt.getDeviceName();
				if(start_map.containsKey(next_node)) {
					HashSet<Integer> start_node_aps = start_map.get(next_node);
					HashSet<Integer> retained_aps = new HashSet<Integer>(aps);
					retained_aps.retainAll(start_node_aps);
					HashSet<Integer> remained_aps = new HashSet<Integer>(aps);
					remained_aps.removeAll(start_map.get(next_node));
					if(!retained_aps.isEmpty()) {
						// add one edge from current hop's source node to next node
						for(String src : current_hop.sourceNode_aps.keySet()) {
							boolean isloop = false;
							if(src.equals(next_node)) {
								isloop = true;
							}
							HashSet<Integer> src_aps = new HashSet<Integer>(current_hop.sourceNode_aps.get(src));
							src_aps.retainAll(retained_aps);
							if(src_aps.isEmpty()) continue;
							if(isloop) {
//								loop_ap_set.addAll(src_aps);
							}
							else {
								Pair<String,String> one_edge= new Pair<String,String>(src,next_node);
								HashSet<Integer> start_aps = startNodesTree.get(one_edge);
								if(start_aps == null) {
									start_aps = new HashSet<Integer>();
								}
								start_aps.addAll(src_aps);
								startNodesTree.put(one_edge, start_aps);
							}
						}
						if(remained_aps.isEmpty()) continue;
					}
					// update link relation form current hop's source node
					for(String src : current_hop.sourceNode_aps.keySet()) {
						HashSet<Integer> src_aps = new HashSet<Integer>(current_hop.sourceNode_aps.get(src));
						src_aps.retainAll(remained_aps);
						if(src_aps.isEmpty()) continue;
						HashMap<String,HashSet<Integer>> node_set = src_to_nodes.get(src);
						if(node_set == null) {
							node_set = new HashMap<String,HashSet<Integer>>();
						}
						if(node_set.containsKey(next_node)) {
							node_set.get(next_node).addAll(src_aps);
						}
						else {
							HashSet<Integer> node_aps = new HashSet<Integer>();
							node_aps.addAll(src_aps);
							node_set.put(next_node, node_aps);
						}
						src_to_nodes.put(src, node_set);
					}
					if(remained_aps.isEmpty()) continue;
					pushInQueue(next_node,remained_aps,current_hop);
				}
				else {
					// update link relation form current hop's source node
					for(String src : current_hop.sourceNode_aps.keySet()) {
						HashSet<Integer> src_aps = new HashSet<Integer>(current_hop.sourceNode_aps.get(src));
						src_aps.retainAll(aps);
						if(src_aps.isEmpty()) continue;
						HashMap<String,HashSet<Integer>> node_set = src_to_nodes.get(src);
						if(node_set == null) {
							node_set = new HashMap<String,HashSet<Integer>>();
						}
						if(node_set.containsKey(next_node)) {
							node_set.get(next_node).addAll(src_aps);
						}
						else {
							HashSet<Integer> node_aps = new HashSet<Integer>();
							node_aps.addAll(src_aps);
							node_set.put(next_node, node_aps);
						}
						src_to_nodes.put(src, node_set);
					}
					if(aps.isEmpty()) continue;
					pushInQueue(next_node,aps,current_hop);
				}
			}
		}
	}
	private void TraverseForwardingGraphNoConstruction(HashMap<String, HashSet<Integer>> moved_aps, String type) {
		// parameter element refers to actual element's name
		// port name in port_aps refers node's name, so does in node_ports
		// for return, change matrix contains change value at the end of this traverse
		HashMap<Integer, HashMap<String, HashMap<String, Integer>>> change_matrix
				= new HashMap<Integer, HashMap<String, HashMap<String, Integer>>>();
		queue.clear();
		src_to_nodes.clear();
		startNodesTree.clear();
		// get start node
		getStartNodesNoConstruction(moved_aps);
		// bfs to find reachable set for every start nodes
		while(!queue.isEmpty()) {
			TraversalItem ti = queue.getFirst();
//			// node is not included in DFG, continue
			// traverse
			traverseFromNodeBatchNoConstruction(ti, type);
			popOutQueue();
		}
	}
	
	public HashMap<Pair<String, String>,HashMap<String, HashSet<Integer>>> getChanges(){
		return reach_change;
	}
	public Hashtable<String, Hashtable<String, HashSet<Integer>>> getReachabilityMatrix(){
		return pair_to_ap;
	}
}
