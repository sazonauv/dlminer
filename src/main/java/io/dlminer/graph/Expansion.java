package io.dlminer.graph;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

public class Expansion extends ALCNode {

	// not null only for expansions
	public OWLNamedIndividual individual;	
	public Integer depth;	

	// points to the original ABox node
	public ALCNode pointer;


	public Expansion(Set<OWLClassExpression> label) {
		super(label);
	}

	
		
	@Override
	public String toString() {
		return super.toString();
	}



}
