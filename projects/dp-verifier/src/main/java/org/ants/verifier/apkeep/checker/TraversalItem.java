package org.ants.verifier.apkeep.checker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class TraversalItem {
	public String nodeName;
	public HashMap<String,HashSet<Integer>> sourceNode_aps;
	public HashSet<Integer> atoms;
	public HashMap<String, HashMap<Integer, HashSet<LinkedList<String>>>> srcNodeApPath;
	public TraversalItem(String name, HashSet<Integer> aps) {
		// TODO Auto-generated constructor stub
		nodeName = name;
		sourceNode_aps = new HashMap<String,HashSet<Integer>>();
		atoms = new HashSet<Integer>(aps);
		srcNodeApPath = new HashMap<>();
	}

	@Override
	public String toString() {
		return nodeName+" "+atoms.toString();
	}
	@Override
	public boolean equals(Object o) {
		if(o instanceof TraversalItem) {
			TraversalItem another = (TraversalItem) o;
			if(!another.nodeName.equals(nodeName)) return false;
			if(!another.sourceNode_aps.equals(sourceNode_aps)) return false;
			if(!another.atoms.equals(atoms)) return false;
			if(!another.srcNodeApPath.equals(srcNodeApPath)) return false;
			return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return nodeName.hashCode()+sourceNode_aps.hashCode()+atoms.hashCode();
	}

	public void updateSourceMap(TraversalItem last_hop, HashSet<Integer> aps) {
		for(String src : last_hop.sourceNode_aps.keySet()) {
			HashSet<Integer> src_aps = new HashSet<Integer>(last_hop.sourceNode_aps.get(src));
			src_aps.retainAll(aps);
			if(src_aps.isEmpty()) continue;
			HashSet<Integer> src_atoms = sourceNode_aps.get(src);
			if(src_atoms == null) {
				src_atoms = new HashSet<Integer>();
			}
			src_atoms.addAll(src_aps);
			sourceNode_aps.put(src, src_atoms);
		}
	}
	public void updateSrcPath(TraversalItem last_hop, HashSet<Integer> aps) {
		for(String src : last_hop.srcNodeApPath.keySet()) {
			HashMap<Integer, HashSet<LinkedList<String>>> last_ap_paths = last_hop.srcNodeApPath.get(src);
			
			for(int ap : aps){
				if(!last_ap_paths.containsKey(ap)) continue;
				for(LinkedList<String> path : last_ap_paths.get(ap)) {
					LinkedList<String> new_path = new LinkedList<>(path);
					new_path.add(nodeName);
					
					if(!srcNodeApPath.containsKey(src)) srcNodeApPath.put(src, new HashMap<>());
					if(!srcNodeApPath.get(src).containsKey(ap)) srcNodeApPath.get(src).put(ap, new HashSet<>());
					srcNodeApPath.get(src).get(ap).add(new_path);
				}
			}
		}
	}

	public void setSourceMap(String node_name, HashSet<Integer> aps) {
		sourceNode_aps.put(node_name, new HashSet<Integer>(aps));
	}

	public void setSourcePath(String node_name, HashSet<Integer> aps) {
		if(!srcNodeApPath.containsKey(node_name)) {
			srcNodeApPath.put(node_name, new HashMap<>());
		}
		for(Integer ap : aps) {
			srcNodeApPath.get(node_name).put(ap,new HashSet<>());
			srcNodeApPath.get(node_name).get(ap).add(new LinkedList<String>());
		}
	}
}
