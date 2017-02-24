package io.dlminer.refine;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;

public abstract class LearnerOperator implements Operator {

	public abstract Set<OWLClassExpression> refine(OWLClassExpression expr);
	
}
