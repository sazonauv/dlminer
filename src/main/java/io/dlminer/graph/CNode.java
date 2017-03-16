package io.dlminer.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public abstract class CNode {

	protected static final String NULL_CONCEPT_ERROR = "Cannot use a successor with the NULL concept";
	protected static final String WRONG_EDGE_TYPE_ERROR = "Edge type can be either existential or universal";
    protected static final String WRONG_LITERAL_TYPE_ERROR = "Literals can only be numeric for data property " +
            "restrictions";


    public OWLClassExpression concept;
	
	public Integer coverage;
	
	

	protected int depth = -1;

	protected int size = -1;

	protected int length = -1;
	
	
	
	protected LinkedList<CEdge> outEdges;
	protected LinkedList<CEdge> inEdges;	
	
			
		
	
	public boolean addOutEdge(CEdge edge) {
		if (edge != null) {			
			if (outEdges == null) {
				outEdges = new LinkedList<>();
			}
			return outEdges.add(edge) && edge.object.addInEdge(edge);
		} else {
			return false;
		}
	}
	
	
	protected boolean addInEdge(CEdge edge) {
		if (inEdges == null) {
			inEdges = new LinkedList<>();
		}
		return inEdges.add(edge);
	}
	
	
	
	public LinkedList<CEdge> getOutEdges() {
		return outEdges;
	}
	
	
	public LinkedList<CEdge> getInEdges() {
		return inEdges;
	}
	
	
	protected boolean removeOutEdges(Set<CEdge> edges) {
		if (edges != null) {
			boolean result = true;
			for (CEdge edge : edges) {
				if (!removeOutEdge(edge)) {
					result = false;
				}
			}
			return result;
		} else {
			return false;
		}
	}


	protected boolean removeOutEdge(CEdge edge) {
		if (edge != null) {
			boolean done = outEdges.remove(edge);
			if (outEdges.isEmpty()) {
				outEdges = null;
			}
			return done && edge.object.removeInEdge(edge);
		} else {
			return false;
		}
	}


	protected boolean removeInEdge(CEdge edge) {
		boolean done = inEdges.remove(edge);
		if (inEdges.isEmpty()) {
			inEdges = null;
		}
		return done;
	}
	
	
	
	public abstract boolean isMoreSpecificThan(CNode node);



	protected boolean isEqualTo(CNode node) {
		if (concept != null && node.concept != null 
				&& concept.equals(node.concept)) {
			return true;
		}
		return this.isMoreSpecificThan(node) && node.isMoreSpecificThan(this);
	}
	
		
	
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public int hashCode() {
		return hash(0);
	}
	
	
	protected abstract int hash(int hash);
	
	
	@Override
	public abstract CNode clone();	
	
	
	
	@Override
	public String toString() {		
		return "(cover=" + coverage
		+ "; length=" + length()
		+ "; depth=" + depth()
		+ ")";
	}


	// recursion!
	private int countNodes(int count) {
		count++;			
		if (outEdges != null) {			
			for (CEdge e : outEdges) {
				count = e.object.countNodes(count);
			}		
		}
		return count;
	}

	// nodes number
	public int size() {
		if (size < 0) {
			size = countNodes(0);
		}
		return size;
	}
	
	
	public int depth() {
		if (depth < 0) {
			depth = countDepth();
		}
		return depth;
	}
	

    protected abstract int countDepth();

	
	// labels number
	public int length() {
		if (length < 0) {
			length = countLength(0);
		}
		return length;
	}
	
	
	protected abstract int countLength(int count);
	
	
	public LinkedList<CNode> traverse() {
		LinkedList<CNode> visits = new LinkedList<>();
		LinkedList<CNode> history = new LinkedList<>();
		history.add(this);
		while (!history.isEmpty()) {
			CNode n = history.pollFirst();
			visits.add(n);
			if (n.outEdges != null) {
				for (CEdge e : n.outEdges) {
				    if (!(e instanceof DataEdge)) {
                        history.add(e.object);
                    }
				}
			}
		}		
		return visits;
	}
	
	
	public OWLClassExpression getConcept() {
		if (concept == null) {
			OWLDataFactory factory = new OWLDataFactoryImpl();
			LinkedList<CNode> visits = traverse();
			while (!visits.isEmpty()) {
				CNode n = visits.pollLast();
				n.buildTopLevelConcept(factory);
			}		
		}
		return concept;
	}
	
	
	protected abstract void buildTopLevelConcept(OWLDataFactory factory);
	
	
	public CNode find(CNode target) {
		return find(target, false);
	}
	
	
	private CNode find(CNode target, boolean useRecursion) {
		if (useRecursion) {
			return findRecursion(target);
		}
		return findNoRecursion(target);
	}

	// recursion!
	private CNode findRecursion(CNode target) {		
		if (equals(target)) {
			return this;
		} 
		CNode equal = null;
		if (outEdges != null) {			
			for (CEdge e : outEdges) {
				equal = e.object.findRecursion(target);
				if (equal != null) {
					break;
				}
			}
		}		
		return equal;
	}
	
	
	
	private CNode findNoRecursion(CNode target) {
		if (equals(target)) {
			return this;
		}
		CNode equal = null;
		List<CNode> nodes = traverse();
		for (CNode node : nodes) {
			if (node.equals(target)) {
				equal = node;
				break;
			}
		}
		return equal;
	}
	
	
	
	protected abstract boolean isOWLThing();
	
	
	

	public int depthOf(CNode node) {
		return depth() - node.depth();
	}

	
	
	public boolean isLeaf() {
		return outEdges == null || outEdges.isEmpty();
	}

	
	
}
