package io.dlminer.sort;

import org.semanticweb.owlapi.model.OWLClassExpression;


public class OWLClassExpressionSignatureComparator 
	extends OWLClassExpressionComparator {
	
	public OWLClassExpressionSignatureComparator(SortingOrder order) {
		this.order = order;
	}

	@Override
	public int compare(OWLClassExpression ex1, OWLClassExpression ex2) {
		if (order.equals(SortingOrder.ASC)) {
			return Integer.compare(ex1.getSignature().size(), ex2.getSignature().size());
		} 
		return - Integer.compare(ex1.getSignature().size(), ex2.getSignature().size());
	}	
}