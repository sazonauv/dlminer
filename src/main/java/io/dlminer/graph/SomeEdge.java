package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class SomeEdge extends CEdge {

	public SomeEdge(ALCNode subject, OWLObjectPropertyExpression label, ALCNode object) {
		init(subject, label, object);
	}	

}
