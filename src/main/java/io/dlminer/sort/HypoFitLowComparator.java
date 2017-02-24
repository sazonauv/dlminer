package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;



public class HypoFitLowComparator extends HypoComparator {
	
	public HypoFitLowComparator(SortingOrder order) {
		this.order = order;
	}
	
	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		if (order.equals(SortingOrder.ASC)) {
			return h1.support.compareTo(h2.support);
		}
		return - h1.support.compareTo(h2.support);		
		
	}
}