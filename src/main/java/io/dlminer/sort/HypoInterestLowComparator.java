package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;


public class HypoInterestLowComparator extends HypoComparator {
	
	public HypoInterestLowComparator(SortingOrder order) {
		this.order = order;
	}
	
	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		if (order.equals(SortingOrder.ASC)) {
			return Double.compare(h1.noveltyApprox, h2.noveltyApprox);
		} 
		return - Double.compare(h1.noveltyApprox, h2.noveltyApprox);		
	}
}
