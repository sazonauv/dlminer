package io.dlminer.learn;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class ClassPair extends Pair<OWLClassExpression> {
	
	public ClassPair() {
		super();
	}

	public ClassPair(OWLClassExpression first, OWLClass second) {
		super(first, second);
	}
	
}
