package io.dlminer.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class HSTree<I> {
	
	private HSNode<Set<I>> root;
	private Set<HSNode<Set<I>>> nodes;

	public HSTree(HSNode<Set<I>> root) {
		this.root = root;
		init();		
	}
	
	private void init() {		
		nodes = new HashSet<>();
		nodes.add(root);
	}
		
	public boolean addChild(HSNode<Set<I>> parent, HSNode<Set<I>> child) {		
		if (parent == null || child == null) {
			return false;
		} else {			
			nodes.add(child);
			return parent.addChild(child);
		}	
	}

	public boolean addChilds(HSNode<Set<I>> parent, Set<HSNode<Set<I>>> childs) {
		boolean allAdded = true;
		for (HSNode<Set<I>> child : childs) {
			if (!addChild(parent, child)) {
				allAdded = false;
			}
		}
		return allAdded;
	}


	/**
	 * @param node - a starting node
	 * @param dfs - depth-first search
	 * @return visited nodes
	 */
	private LinkedList<HSNode<Set<I>>> traverse(HSNode<Set<I>> node, boolean dfs) {		
		LinkedList<HSNode<Set<I>>> hist = new LinkedList<>();
		hist.add(node);
		LinkedList<HSNode<Set<I>>> visited = new LinkedList<>();
		while (!hist.isEmpty()) {
			HSNode<Set<I>> next = null;
			if (!dfs) {
				next = hist.pollFirst();
			} else {
				next = hist.pollLast();
			}
			visited.add(next);			
			// do not store visited nodes
			Set<HSNode<Set<I>>> childs = next.getChilds();
			if (childs != null) {
				for (HSNode<Set<I>> ch : childs) {
					if (!visited.contains(ch)) {
						hist.add(ch);
					}
				}
			}			
		}
		return visited;
	}
		
	
	public boolean hasPath(Set<I> path) {
		boolean hasPath = false;
		for (HSNode<Set<I>> n : nodes) {
			if (n.path.equals(path)) {
				hasPath = true;
				break;
			}
		}
		return hasPath;
	}
	
	
	@Override
	public String toString() {
		String str = "\nHash Set Tree:";
		LinkedList<HSNode<Set<I>>> visited = traverse(root, true);
		for (HSNode<Set<I>> n : visited) {
			str += ("\n" + n.toString());
		}
		return str;		
	}

	public HSNode<Set<I>> getRoot() {
		return root;
	}

	public Set<HSNode<Set<I>>> getNodes() {
		return nodes;
	}
	
	public Set<Set<I>> getMinimalSubsets() {
		Set<Set<I>> minSubsets = new HashSet<>();
		int minSize = Integer.MAX_VALUE;
		for (HSNode<Set<I>> n : nodes) {			
			if (n.value != null && minSize > n.value.size()) {
				minSize = n.value.size();
			}
		}
		for (HSNode<Set<I>> n : nodes) {
			if (n.value != null && minSize == n.value.size()) {
				minSubsets.add(n.value);
			}
		}
		return minSubsets;
	}
	
	public void merge(HSTree<I> tree) {
		for (HSNode<Set<I>> n : nodes) {
			mergeNode(n, tree, true);			
		}
	}
		
	private void mergeNode(HSNode<Set<I>> node, HSTree<I> tree, boolean dfs) {
		LinkedList<HSNode<Set<I>>> hist = new LinkedList<>();
		LinkedList<HSNode<Set<I>>> mhist = new LinkedList<>();
		hist.add(tree.root);
		mhist.add(node);
		Set<HSNode<Set<I>>> visited = new HashSet<>();
		while (!hist.isEmpty()) {
			HSNode<Set<I>> next = null;
			HSNode<Set<I>> mnext = null;
			if (!dfs) {
				next = hist.pollFirst();
				mnext = mhist.pollFirst();
			} else {
				next = hist.pollLast();
				mnext = mhist.pollLast();
			}
			// merge a node			
			mnext.value.addAll(next.value);			
			mnext.path.addAll(next.path);			
			// add to visited
			visited.add(next);			
			// do not store visited nodes
			Set<HSNode<Set<I>>> childs = next.getChilds();
			if (childs != null) {
				for (HSNode<Set<I>> ch : childs) {
					if (!visited.contains(ch)) {
						hist.add(ch);
						// add a child						
						HSNode<Set<I>> mch = new HSNode<>(node.value, node.path);
						mnext.addChild(mch);
						// add to history
						mhist.add(mch);
					}
				}
			}			
		}
	}

}
