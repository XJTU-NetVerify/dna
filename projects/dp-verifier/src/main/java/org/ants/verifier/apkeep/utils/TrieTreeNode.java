package org.ants.verifier.apkeep.utils;

import java.util.ArrayList;

public class TrieTreeNode {

	final static int ipBits = 32;
	
    public ArrayList<PrefixItem> prefix_items;

    int node_level;
    int node_value;
    
    // children [0]: bit 0; children[1]: bit 1; children[2]: bit *
	TrieTreeNode parent;
    TrieTreeNode[] children;
	
	public TrieTreeNode(int level, int value)
	{
		children = new TrieTreeNode[3];
		prefix_items = new ArrayList<PrefixItem>();
		node_level = level;
		node_value = value;
	}
	
	public TrieTreeNode Insert(int[] prefixbin)
	{	
		int index = prefixbin[node_level];	
		
		if (children[index] == null) {
			//System.out.println("create node " + index + " at level " + (level+1));
			children[index] = new TrieTreeNode(node_level+1, index);
		}
		children[index].parent = this;
		
		// stop recursion when reaching wildcard or the bottom level
		if (index == 2 || node_level == 31) {
			return children[index];
		}
		
		return children[index].Insert(prefixbin);
	}

	public TrieTreeNode Search(int[] prefixbin)
	{	
		//System.out.println("Level " + level + ", " + "Bit " + prefixbin[level]);

		// should not reach here
		if (node_level > 31) {
			System.err.println("Error reaching here");
		}
		
		int index = prefixbin[node_level];
		if (children[index] !=  null) {
			// stop recursion when reaching wildcard or the bottom level
			if (index == 2 || node_level == 31)
				return children[index];
			return children[index].Search(prefixbin);
		}
		
		return null;
	}
	
	public void GetDescendantRecur(ArrayList<TrieTreeNode> descendants)
	{
		if (node_level == 32) {
			descendants.add(this);
			return;
		}
		
		if (children[2] != null) {
			descendants.add(children[2]);
			//return;
		}
		
		if (children[0] != null) {
			children[0].GetDescendantRecur(descendants);
		}
		
		if (children[1] != null) {
			children[1].GetDescendantRecur(descendants);
		}
		
		return;
	}

	public ArrayList<TrieTreeNode> GetDescendant()
	{
		ArrayList<TrieTreeNode> descendants = new ArrayList<TrieTreeNode>();
		
		if (node_level == 32 && node_value != 2) {
			return descendants;
		}
		
		if (parent == null) {
			System.err.println("Error finding descendant");
			System.exit(0);
		}
		
		if (parent.children[0] != null) {
			parent.children[0].GetDescendantRecur(descendants);
		}
		
		if (parent.children[1] != null) {
			parent.children[1].GetDescendantRecur(descendants);
		}
		
		return descendants;
	}
	
	public void GetAncestorRecur(ArrayList<TrieTreeNode> ancestors)
	{
		if (parent != null) {
			if (parent.children[2] != null)
				ancestors.add(parent.children[2]);
			parent.GetAncestorRecur(ancestors);
		}
	}
	
	public ArrayList<TrieTreeNode> GetAncestor()
	{
		ArrayList<TrieTreeNode> ancestors = new ArrayList<TrieTreeNode>();
		
		// should not reach here
		if (parent == null) {
			System.err.println("Error: parent not initialized");
			System.exit(0);
		}
		
		// search for all ancestors recursively
//		parent.GetAncestorRecur(ancestors);
		GetAncestorRecur(ancestors);
		if(ancestors.contains(this))
			ancestors.remove(this);
		
		return ancestors;
	}


	public ArrayList<TrieTreeNode> getAffectedNodes(int[] prefixbin) {
		// TODO Auto-generated method stub
		ArrayList<TrieTreeNode> affected_nodes = new ArrayList<TrieTreeNode>();
		
		// should not reach here
		if (node_level > 31) {
			System.err.println("Error reaching here");
		}
		
		int index = prefixbin[31-node_level];
		if(children[2] != null) {
			affected_nodes.add(children[2]);
		}
		if (children[index] !=  null) {
			if (index == 2) {
				// do nothing
			}
			else if(node_level == 31){
				affected_nodes.add(children[index]);
			}
			else {
				affected_nodes.addAll(children[index].getAffectedNodes(prefixbin));
			}
		}
		
		return affected_nodes;
	}
	
	public TrieTreeNode GetParent()
	{
		// should not reach here
		if (parent == null) {
			System.err.println("Error: parent not initialized");
			System.exit(0);
		}
		
		// the first level, return the all-wildcard node
		if (node_level == 1) {
			return parent;
		}
		
		// search for the first wildcard child
		TrieTreeNode ancestor = parent.parent;
		while (ancestor != null) {
			if (ancestor.children[2] != null)
				return ancestor.children[2];
			ancestor = ancestor.parent;
		}
		
		System.err.println("Error: no parent found: " + this.toString());
		System.exit(0);
		
		return null;
	}

	public boolean IsRedundant ()
	{
		for (int i=0; i<3; i++) {
			if (children[i] != null) {
				return false;
			}
		}
		return true;
	}
	
	public boolean IsInValid ()
	{
		return prefix_items.isEmpty();
	}
	
	public void Delete ()
	{
		if (parent != null){
			parent.children[node_value] = null;
			if (parent.IsRedundant()) {
				parent.Delete();
			}
		}
	}
    
    public boolean AddPrefixItem(PrefixItem new_item)
    {
    	if (prefix_items.contains(new_item)) {
    		return false;
    	}
        prefix_items.add(new_item);
        return true;
    }
    
    public boolean HasPrefixItem(PrefixItem new_item)
    {
    	return prefix_items.contains(new_item);
    }
    
    public void RemovePrefixItem(PrefixItem existing_item)
    {
        prefix_items.remove(existing_item);
    }
    
    
    public ArrayList<PrefixItem> GetPrefixItems()
    {
        return prefix_items;
    }
    
    public String toString() 
    {
    	return  "level: " + node_level + "; value: " + node_value;
    }
    
    public void PrintTrie (String prefix)
    {
    	if (node_value == 2 || node_level == 32)
    		System.out.println(prefix);
    	for (int i=0; i<3; i++) {
    		if (children[i] != null) {
    			children[i].PrintTrie(prefix+i);
    		}
    	}
    }
}