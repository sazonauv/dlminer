package io.dlminer.sort;

import io.dlminer.ont.LengthMetric;
import io.dlminer.ont.OntologyHandler;

import org.semanticweb.owlapi.model.OWLAxiom;



/**
 * @author Slava Sazonau
 * compares axioms by their length
 */
public class AxiomLengthComparator extends AxiomComparator {	
	
	public AxiomLengthComparator(SortingOrder order) {
		this.order = order;
	}

	@Override
	public int compare(OWLAxiom axiom1, OWLAxiom axiom2) {		
		if (order.equals(SortingOrder.ASC)) {
			return LengthMetric.length(axiom1).compareTo(LengthMetric.length(axiom2));
		}
		return - LengthMetric.length(axiom1).compareTo(LengthMetric.length(axiom2));
	}
}
