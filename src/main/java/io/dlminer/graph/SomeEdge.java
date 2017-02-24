package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class SomeEdge extends CEdge {

	public SomeEdge(CNode subject, OWLObjectPropertyExpression label, CNode object) {
		init(subject, label, object);
	}	

}
