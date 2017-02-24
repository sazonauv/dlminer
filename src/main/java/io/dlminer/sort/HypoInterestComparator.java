package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;


public class HypoInterestComparator extends HypoComparator {
	
	public HypoInterestComparator(SortingOrder order) {
		this.order = order;
	}
	
	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		if (order.equals(SortingOrder.ASC)) {
			return Double.compare(h1.novelty, h2.novelty);
		} 
		return - Double.compare(h1.novelty, h2.novelty);		
	}
}