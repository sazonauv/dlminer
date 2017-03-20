package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

public abstract class CEdge {


	public OWLPropertyExpression label;

	public CNode subject;
	public CNode object;
	
	
	
	protected void init(CNode subject, OWLPropertyExpression label, CNode object) {
		this.subject = subject;
		this.label = label;
		this.object = object;
	}



	
	@Override
	public String toString() {
		return subject + " " + label + " " + object;
	}

}
