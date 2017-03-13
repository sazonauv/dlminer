package io.dlminer.graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import io.dlminer.ont.LengthMetric;

public class ELNode extends CNode {
	
	
	public Set<OWLClassExpression> labels;
	
	
	
	public ELNode(Set<OWLClassExpression> labels) {
		if (labels == null) {
			throw new IllegalArgumentException(Graph.NULL_LABEL_ERROR);
		}
		this.labels = labels;		
	}
	
		
	
	
		
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ELNode)) {
			return false;
		}
		ELNode node = (ELNode) obj;		
		return isEqualTo(node);
	}
	
	
	
	
	// recursion!	
	@Override
	public ELNode clone() {
		ELNode node = new ELNode(new HashSet<>(labels));
		if (outEdges != null) {
			for (CEdge e : outEdges) {
				ELNode child = ((ELNode)e.object).clone();
				SomeEdge edge = new SomeEdge(node, e.label, child);
				node.addOutEdge(edge);
			}
		}		
		return node;
	}


	// recursion!
	@Override
	protected int hash(int hash) {
		hash += labels.hashCode();			
		if (outEdges != null) {			
			for (CEdge e : outEdges) {
				hash = e.label.hashCode() + e.object.hash(hash);
			}		
		}
		return hash;		
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

	
	
	private boolean isMoreSpecificThanELNode(ELNode node) {	
		// check concepts
		if (concept != null && node.concept != null 
				&& concept.equals(node.concept)) {
			return true;
		}
		if (!labels.containsAll(node.labels)) {
			return false;
		}
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
			for (CEdge e1 : outEdges) {
				if (e2.label.equals(e1.label)) {
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
			for (CEdge e1 : outEdges) {
				if (e2.label.equals(e1.label)
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
	
	
	
	private boolean isMoreSpecificThanALCNode(ALCNode node) {
		// check concepts
		if (concept != null && node.concept != null 
				&& concept.equals(node.concept)) {
			return true;
		}
		if (!labels.containsAll(node.clabels)) {
			return false;
		}
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
	
	
	
	// recursion!
	@Override
	protected int countLength(int count) {		
		if (labels.isEmpty()) {
			// count owl:Thing
			count++;
		} else {
			// count conjunctions
			count += labels.size() - 1;
			// count operands: A, not A
			for (OWLClassExpression expr : labels) {
				count += LengthMetric.length(expr);
			}
		}	
		// count edges
		if (outEdges != null) {
			// count each edge and its conjunction
			count += 2*outEdges.size();
			// owl:Thing's conjunction is not counted
			if (labels.isEmpty()) {
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
		if (concept != null) {
			return;
		}
		// gather expressions
		Set<OWLClassExpression> exprs = null;		
		if (outEdges == null) {
			exprs = labels;
		} else {
			exprs = new HashSet<>(labels);
			for (CEdge e : outEdges) {
				if (e.object.concept == null) {
					throw new IllegalArgumentException(NULL_CONCEPT_ERROR);
				} else {					
					OWLClassExpression existential = 
							factory.getOWLObjectSomeValuesFrom(e.label,	e.object.concept);
					exprs.add(existential);
				}
			}
		}				
		// process expressions				
		if (exprs.isEmpty()) {
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

	
	

	// instance checker methods

	public boolean containsEdgeLabel(OWLObjectProperty prop) {
		if (outEdges != null) {
			for (CEdge e : outEdges) {
				if (e.label.equals(prop)) {
					return true;
				}
			}
		}		
		return false;
	}

	
	public static ELNode parse(OWLClassExpression expr) {	
		ELNode node = new ELNode(new HashSet<OWLClassExpression>(2));
		node.parseExpression(expr);
		return node;
	}
	
	
	private void parseExpression(OWLClassExpression expr) {	
		concept = expr;
		if (expr instanceof OWLObjectSomeValuesFrom) {
			parseExistential((OWLObjectSomeValuesFrom) expr);
		} else if (expr instanceof OWLObjectIntersectionOf) {
			Set<OWLClassExpression> conjs = ((OWLObjectIntersectionOf) expr).asConjunctSet();
			for (OWLClassExpression conj : conjs) {
				if (conj instanceof OWLObjectSomeValuesFrom) {
					parseExistential((OWLObjectSomeValuesFrom) conj);
				} else {
					labels.add(conj);
				}
			}			
		} else {
			labels.add(expr);
		}		
	}
	
	
	private void parseExistential(OWLObjectSomeValuesFrom expr) {
		ELNode object = new ELNode(new HashSet<OWLClassExpression>(2));		
		SomeEdge edge = new SomeEdge(this, expr.getProperty(), object);
		addOutEdge(edge);
		object.parseExpression(expr.getFiller());
	}


	@Override
	public boolean isOWLThing() {		
		return (labels == null || labels.isEmpty())
				&& (outEdges == null || outEdges.isEmpty());
	}


	public boolean isAtomic() {		
		return labels.size() == 1 
				&& (outEdges == null || outEdges.isEmpty());
	}
	
		

	public boolean isSimpleExistential() {
		if (outEdges == null) {
			return false;
		}
		if (labels.size() == 0 && outEdges.size() == 1) {
			ELNode object = (ELNode) outEdges.getFirst().object;
			// check if it is TOP
			if (object.isOWLThing()) {
				return true;
			}
		}
		return false;
	}


	public boolean hasOnlySimpleExistentials() {
		if (outEdges == null) {
			return false;
		}
		for (CEdge edge : outEdges) {
			if (!((ELNode)edge.object).isOWLThing()) {
				return false;
			}
		}		
		return true;
	}
	
	
	
	// similarity and normalisation methods
	
	public double similarity(ELNode node) {		
		return (double)(getSimilarityWith(node, 0) + node.getSimilarityWith(this, 0))/(length() + node.length());
	}
	
	
	// recursion!
	private int getSimilarityWith(ELNode node, int similarity) {		
		for (OWLClassExpression expr1 : node.labels) {
			for (OWLClassExpression expr2 : labels) {
				if (expr1.equals(expr2)) {
					similarity++;
				}
			}
		}
		// find the most similar node
		if (outEdges != null && node.outEdges != null) {
			for (CEdge e1 : outEdges) {
				int maxSim = -1;				
				for (CEdge e2 : node.outEdges) {
					if (e1.label.equals(e2.label)) {
						int sim = ((ELNode)e1.object).getSimilarityWith((ELNode)e2.object, similarity);
						if (sim > maxSim) {
							maxSim = sim;
						}
					}
				}
				if (maxSim > -1) {
					similarity = maxSim + 1;
				}
			}
		}		
		return similarity;
	}
	
	
	// recursion!
	public ELNode multiply(ELNode node) {
		Set<OWLClassExpression> l = new HashSet<>(labels);
		// can be empty but never null
		l.retainAll(node.labels);
		ELNode n = new ELNode(l);
//		n.addOperand(this);
//		n.addOperand(node);
		if (outEdges != null && node.outEdges != null) {
			for (CEdge e1 : outEdges) {
				for (CEdge e2 : node.outEdges) {
					if (e1.label.equals(e2.label)) {
						SomeEdge e = new SomeEdge(n, e1.label, 
								((ELNode)e1.object).multiply((ELNode)e2.object));
						n.addOutEdge(e);
					}
				}
			}		
		}
		return n;
	}
	
	
	
	public ELNode normalise() {
		removeRedundantSuccessors();		
		return this;
	}
	
	
	// recursion!
	private void removeRedundantSuccessors() {
		if (outEdges != null) {
			Set<CEdge> duplicates = new HashSet<>();
			for (CEdge e1 : outEdges) {
				for (CEdge e2 : outEdges) {
					if (!e1.equals(e2)
							&& e1 instanceof SomeEdge == e2 instanceof SomeEdge
                            && e1 instanceof OnlyEdge == e2 instanceof OnlyEdge
							&& e1.label.equals(e2.label)
							&& !duplicates.contains(e1)
                            && CNode.isMoreSpecificThan(e1.object, e2.object)) {
						duplicates.add(e2);						
					}
				}
			}
			removeOutEdges(duplicates);
			for (CEdge e : outEdges) {
			    if (!(e instanceof DataEdge)) {
                    ((ELNode) e.object).removeRedundantSuccessors();
                }
			}
		}
	}
			
}
