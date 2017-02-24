package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;


public class HypoLengthComparator extends HypoComparator {
	
	public HypoLengthComparator(SortingOrder order) {
		this.order = order;
	}
	
	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		if (order.equals(SortingOrder.ASC)) {
			return h1.length.compareTo(h2.length);
		} 
		return - h1.length.compareTo(h2.length);		
	}
}
