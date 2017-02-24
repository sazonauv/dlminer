package io.dlminer.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import io.dlminer.ont.LengthMetric;

public class ALCNode extends CNode {
	
	public Set<OWLClassExpression> clabels;
	public Set<OWLClassExpression> dlabels;
	
	
	public ALCNode(Set<OWLClassExpression> clabels,
			Set<OWLClassExpression> dlabels) {
		if (clabels == null || dlabels == null) {
			throw new IllegalArgumentException(Graph.NULL_LABEL_ERROR);
		}
		this.clabels = clabels;
		this.dlabels = dlabels;
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ALCNode)) {
			return false;
		}
		ALCNode node = (ALCNode) obj;		
		return isEqualTo(node);
	}
	
	
	
	@Override
	public boolean isMoreSpecificThan(CNode node) {		
		if (node instanceof ELNode) {
			return isMoreSpecificThanELNode((ELNode) node);
		} else if (node instanceof ALCNode) {
			return isMoreSpecificThanALCNode((ALCNode) node);
		} else {
			return false;
		}
	}
	
	
	private boolean isMoreSpecificThanALCNode(ALCNode node) {
		// check concepts
		if (concept != null && node.concept != null 
				&& concept.equals(node.concept)) {
			return true;
		}
		// check labels
		// (A1 and A2) < A1; A1 < (A1 or A2)
		if (!clabels.containsAll(node.clabels)
				|| !node.dlabels.containsAll(dlabels)) {
			return false;
		}
		// check edges
		LinkedList<CEdge> edges = node.outEdges;
		if (edges == null) {
			return true;
		} 
		if (outEdges == null) {
			return false;
		}
		// check edge labels
		for (CEdge e2 : edges) {
			boolean found = false;
			boolean isEx2 = e2 instanceof SomeEdge;
			for (CEdge e1 : outEdges) {
				if (e2.label.equals(e1.label)
						&& (isEx2 == e1 instanceof SomeEdge)) {
					found = true;
					break;					
				}				
			}
			if (!found) {
				return false;
			}
		}
		// check edge successors
		for (CEdge e2 : edges) {
			boolean found = false;
			boolean isEx2 = e2 instanceof SomeEdge;
			for (CEdge e1 : outEdges) {
				if (e2.label.equals(e1.label)
						&& (isEx2 == e1 instanceof SomeEdge)
						&& e1.object.isMoreSpecificThan(e2.object)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}					
		return true;
	}
	
	
	

	private boolean isMoreSpecificThanELNode(ELNode node) {
		// check concepts
		if (concept != null && node.concept != null 
				&& concept.equals(node.concept)) {
			return true;
		}
		// check labels		
		if (!dlabels.isEmpty()
				|| !clabels.containsAll(node.labels)) {
			return false;
		}		
		// check edges
		LinkedList<CEdge> edges = node.outEdges;
		if (edges == null) {
			return true;
		} 
		if (outEdges == null) {
			return false;
		}
		// check edge labels
		for (CEdge e2 : edges) {
			boolean found = false;
			boolean isEx2 = e2 instanceof SomeEdge;
			for (CEdge e1 : outEdges) {
				if (e2.label.equals(e1.label)
						&& (isEx2 == e1 instanceof SomeEdge)) {
					found = true;
					break;					
				}				
			}
			if (!found) {
				return false;
			}
		}
		// check edge successors
		for (CEdge e2 : edges) {
			boolean found = false;
			boolean isEx2 = e2 instanceof SomeEdge;
			for (CEdge e1 : outEdges) {
				if (e2.label.equals(e1.label)
						&& (isEx2 == e1 instanceof SomeEdge)
						&& e1.object.isMoreSpecificThan(e2.object)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}					
		return true;
	}
	
	
	
	@Override
	protected int hash(int hash) {
		hash += clabels.hashCode() + dlabels.hashCode();		
		if (outEdges != null) {			
			for (CEdge e : outEdges) {
				hash = e.label.hashCode() + e.object.hash(hash);
				if (e instanceof OnlyEdge) {
					hash += 1;
				}
			}		
		}
		return hash;
	}
	

	

	@Override
	public ALCNode clone() {
		ALCNode node = new ALCNode(new HashSet<>(clabels), new HashSet<>(dlabels));
		if (outEdges != null) {
			for (CEdge e : outEdges) {
				ALCNode child = ((ALCNode)e.object).clone();
				CEdge edge = null;
				if (e instanceof SomeEdge) {
					edge = new SomeEdge(node, e.label, child);
				} else if (e instanceof OnlyEdge) {
					edge = new OnlyEdge(node, e.label, child);
				}
				if (edge != null) {
					node.addOutEdge(edge);
				}
			}
		}		
		return node;
	}

	
	
	@Override
	protected int countLength(int count) {		
		if (clabels.isEmpty() && dlabels.isEmpty()) {
			// count owl:Thing
			count++;
		} else {
			// count conjunctions
			if (clabels.size() > 1) {
				count += clabels.size() - 1;
			}
			// count disjunctions
			if (dlabels.size() > 1) {
				count += dlabels.size() - 1;
			}
			// count the conjunction between them
			if (!clabels.isEmpty() && !dlabels.isEmpty()) {
				count++;
			}
			// count operands
			for (OWLClassExpression expr : clabels) {
				count += LengthMetric.length(expr);
			}
			for (OWLClassExpression expr : dlabels) {
				count += LengthMetric.length(expr);
			}			
		}				
		// count edges
		if (outEdges != null) {
			// count each edge and its conjunction
			count += 2*outEdges.size();
			// owl:Thing's conjunction is not counted
			if (clabels.isEmpty() && dlabels.isEmpty()) {
				count -= 2;
			}
			// call recursion on each edge
			for (CEdge e : outEdges) {
				count = e.object.countLength(count);				
			}		
		}
		return count;
	}

	
	
	@Override
	protected void buildTopLevelConcept(OWLDataFactory factory) {
		// gather expressions
		Set<OWLClassExpression> exprs = null;
		// process labels
		if (!clabels.isEmpty()) {
			exprs = new HashSet<>();
			exprs.addAll(clabels);
		}
		if (!dlabels.isEmpty()) {
			if (exprs == null) {
				exprs = new HashSet<>();
			}
			if (dlabels.size() == 1) {
				exprs.addAll(dlabels);
			} else {
				exprs.add(factory.getOWLObjectUnionOf(dlabels));
			}		
		}
		// process edges
		if (outEdges != null) {
			if (exprs == null) {
				exprs = new HashSet<>();
			}
			for (CEdge e : outEdges) {
				// must go bottom-up
				if (e.object.concept == null) {
					throw new IllegalArgumentException(NULL_CONCEPT_ERROR);
				} else {
					if (e instanceof SomeEdge) {
						OWLClassExpression existential = 
								factory.getOWLObjectSomeValuesFrom(e.label,	e.object.concept);
						exprs.add(existential);
					} else if (e instanceof OnlyEdge) {
						OWLClassExpression universal = 
								factory.getOWLObjectAllValuesFrom(e.label,	e.object.concept);
						exprs.add(universal);
					} else {
						throw new IllegalArgumentException(WRONG_EDGE_TYPE_ERROR);
					}
				}
			}
		}				
		// process expressions				
		if (exprs == null || exprs.isEmpty()) {
			concept = factory.getOWLThing();
		} else if (exprs.size() == 1) {
			for (OWLClassExpression c : exprs) {
				concept = c;
				break;
			}
		} else {
			concept = factory.getOWLObjectIntersectionOf(exprs);
		}
	}



	@Override
	public boolean isOWLThing() {
		return clabels.isEmpty() && dlabels.isEmpty() && isLeaf();
	}



	public boolean containsUniversal() {
		if (outEdges == null) {
			return false;
		}
		for (CEdge e : outEdges) {
			if (e instanceof OnlyEdge) {
				return true;
			}
		}		
		return false;
	}



	public boolean containsExistential() {
		if (outEdges == null) {
			return false;
		}
		for (CEdge e : outEdges) {
			if (e instanceof SomeEdge) {
				return true;
			}
		}		
		return false;
	}



}
