package io.dlminer.graph;

import java.util.HashSet;
import java.util.Set;

public class LabelledNode<C, R> {
	
	public C label;
	
	public Set<LabelledEdge<C, R>> outEdges;
	private Set<LabelledEdge<C, R>> inEdges;
	
	
	
	public LabelledNode(C label) {		
		this.label = label;
		init();
	}
	
	private void init() {
		outEdges = null;
		inEdges = null;
	}
	
	public boolean addOutEdge(LabelledEdge<C, R> edge) {
		if (edge != null) {			
			if (outEdges == null) {
				outEdges = new HashSet<>();
			}
			return outEdges.add(edge) && edge.object.addInEdge(edge);
		} else {
			return false;
		}
	}
	
	private boolean addInEdge(LabelledEdge<C, R> edge) {
		if (inEdges == null) {
			inEdges = new HashSet<>();
		}
		return inEdges.add(edge);
	}
	
		
	
	@Override
	public String toString() {
		return label.toString();
	}	

}
