package io.dlminer.sort;

import io.dlminer.ont.LengthMetric;

import java.util.Comparator;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author Slava Sazonau
 * compares strings by their length
 */
public class StringLengthComparator 
extends AbstractComparator
implements Comparator<String> {

	public StringLengthComparator(SortingOrder order) {
		this.order = order;
	}

	@Override
	public int compare(String s1, String s2) {		
		if (order.equals(SortingOrder.ASC)) {
			return Integer.compare(s1.length(), s2.length());
		}
		return - Integer.compare(s1.length(), s2.length());		
	}
	
}
