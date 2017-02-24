package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;



public class HypoBravLowComparator extends HypoComparator {
	
	public HypoBravLowComparator(SortingOrder order) {
		this.order = order;
	}
	
	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		if (order.equals(SortingOrder.ASC)) {
			return h1.assumption.compareTo(h2.assumption);
		}
		return - h1.assumption.compareTo(h2.assumption);		
	}
}