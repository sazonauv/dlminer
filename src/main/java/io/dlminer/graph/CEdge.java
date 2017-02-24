package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public abstract class CEdge {


	public OWLObjectPropertyExpression label;

	public CNode subject;
	public CNode object;
	
	
	
	protected void init(CNode subject, 
			OWLObjectPropertyExpression label, CNode object) {
		this.subject = subject;
		this.label = label;
		this.object = object;
	}
	

	
	@Override
	public String toString() {
		return subject + " " + label + " " + object;
	}

}
