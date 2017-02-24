package io.dlminer.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ListGraph<V> {
	
	private List<List<V>> graph;

	public ListGraph(Collection<V> nodes) {
		init(nodes);		
	}
	
	private void init(Collection<V> nodes) {
		graph = new LinkedList<>();
		for (V node : nodes) {
			List<V> edges = new LinkedList<V>();
			edges.add(node);
			graph.add(edges);
		}
	}
	
	public boolean addEdge(V node1, V node2) {
		boolean added = false;
		for (List<V> edges : graph) {			
			if (edges.get(0).equals(node1)) {
				added = edges.add(node2);
				break;
			}
		}
		return added;
	}
	
	public boolean removeEdge(V node1, V node2) {
		boolean removed = false;
		for (List<V> edges : graph) {			
			if (edges.get(0).equals(node1)) {
				removed = edges.remove(node2);
				break;
			}
		}
		return removed;
	}
	
	public List<V> getEdges(V node) {
		List<V> result = null;
		for (List<V> edges : graph) {			
			if (edges.get(0).equals(node)) {
				result = edges;
				break;
			}
		}
		return result;
	}
	
	public boolean addEdges(V node, Collection<V> nodes) {
		boolean allAdded = true;
		for (V n : nodes) {
			if (!addEdge(node, n)) {
				allAdded = false;
			}
		}
		return allAdded;
	}

	@Override
	public String toString() {
		String res = "\n";
		for (List<V> edges : graph) {
			res += ("\n" + edges);
		}
		res += "\n";
		return res;
	}
	
	

}
