package io.dlminer.sort;

import java.util.Comparator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import io.dlminer.ont.LengthMetric;


/**
 * @author Slava Sazonau
 * compares axioms by their length
 */
public class ConceptLengthComparator 
extends AbstractComparator 
implements Comparator<OWLClassExpression> {	
	
	public ConceptLengthComparator(SortingOrder order) {
		this.order = order;
	}

	@Override
	public int compare(OWLClassExpression expr1, OWLClassExpression expr2) {		
		if (order.equals(SortingOrder.ASC)) {
			return LengthMetric.length(expr1).compareTo(LengthMetric.length(expr2));
		}
		return - LengthMetric.length(expr1).compareTo(LengthMetric.length(expr2));
	}
}