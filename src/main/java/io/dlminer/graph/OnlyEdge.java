package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class OnlyEdge extends CEdge {

	public OnlyEdge(CNode subject, OWLObjectPropertyExpression label, CNode object) {
		init(subject, label, object);
	}

}
