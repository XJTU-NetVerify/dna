package org.ants.verifier.apkeep.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;

public class UtilityTools {
	
	public UtilityTools() {
		// TODO Auto-generated constructor stub
	}
	public static ArrayList<String> readFile(String inputFile) throws IOException{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<String> contents = new ArrayList<String>();
		
		String OneLine;
		while((OneLine = br.readLine()) != null) {
			String linestr = OneLine.trim();
			contents.add(linestr);
		}
		
		return contents;
	}
	public static int[] HashSetToArray(HashSet<Integer> hashset) {
		int[] intarray = new int[hashset.size()];
		int i = 0;
		for(int element : hashset) {
			intarray[i++] = element;
		}
		return intarray;
	}
	public static long BinToLong (int[] bin, int size)
	{
		long result = 0;
		long power = 1;
		for (int i=0; i<size; i++) {
			result += bin[size-i-1] * power;
			power *= 2;
		}
		return result;
	}
	public static String IpBinToString (int[] ip){
		StringBuffer bf = new StringBuffer();
		
		int[] ipSeg = new int[8];
		for (int i=0; i<4; i++) {
			for (int j=0; j<8; j++)
				ipSeg[j] = ip[i*8+j];
			bf.append(BinToLong(ipSeg, 8));
			if (i<3)
				bf.append(".");
		}
		return bf.toString();
	}
	public static long IPStringToLong(String IP)
	{
		long ip_long = 0;
		long power = 1;
		
		String[] octets = IP.split("\\.");
		for(int i = octets.length-1; i >=0 ; i--) {
			ip_long += Long.parseLong(octets[i]) * power;
			power *= 256;
		}
		
		return ip_long;
	}
	public static void copyMatrixTo(Hashtable<String, Hashtable<String, Integer>> origin_matrix,
			Hashtable<String, Hashtable<String, Integer>> copy_matrix) {
		// TODO Auto-generated method stub
		for(String key : origin_matrix.keySet()) {
			Hashtable<String, Integer> mapA = origin_matrix.get(key);
			Hashtable<String, Integer> mapB = new Hashtable<String, Integer>();
			mapB.putAll(mapA);
			
			copy_matrix.put(key, mapB);
		}
	}
	/**
	 * edge in edges should be as node1 <- {node2}, which node1 is destination of an edge
	 * @param edges
	 * @param nodes
	 * @return
	 */
	@SuppressWarnings("serial")
	public static ArrayList<String> topologicalSort(Hashtable<String, HashSet<String>> edges, Set<String> nodes){
		Hashtable<String, HashSet<String>> new_edges = new Hashtable<String, HashSet<String>>();
		ArrayList<String> sorted = new ArrayList<String>();
		Hashtable<String, Integer> in_degree = new Hashtable<String, Integer>();
		for(String node : nodes) in_degree.put(node, 0);
		LinkedList<String> queue = new LinkedList<String>();
		for(String src_node : edges.keySet()) {
			in_degree.put(src_node, in_degree.get(src_node)+edges.get(src_node).size());
			for(String dst_node : edges.get(src_node)) {
				if(new_edges.containsKey(dst_node)) {
					new_edges.get(dst_node).add(src_node);
				}
				else {
					new_edges.put(dst_node, new HashSet<String>() {{add(src_node);}});
				}
			}
		}
//		for(String node : in_degree.keySet()) System.out.println("in degree: "+node+" "+in_degree.get(node));
		for(String node : nodes) {
			if(in_degree.get(node) == 0) queue.addLast(node);
		}
		while(!queue.isEmpty()) {
			String node = queue.getFirst();
			sorted.add(node);
			queue.removeFirst();
			
			if(!new_edges.containsKey(node)) continue;
			for(String next : new_edges.get(node)) {
				in_degree.put(next, in_degree.get(next)-1);
				if(in_degree.get(next) == 0) queue.addLast(next);
			}
		}
		return sorted;
	}
}
