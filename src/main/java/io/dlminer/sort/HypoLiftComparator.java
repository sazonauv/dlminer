package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;

public class HypoLiftComparator extends HypoComparator {
	
	public HypoLiftComparator(SortingOrder order) {
		this.order = order;
	}
	
	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		if (order.equals(SortingOrder.ASC)) {
			return h1.lift.compareTo(h2.lift);
		} 
		return - h1.lift.compareTo(h2.lift);		
	}
}

