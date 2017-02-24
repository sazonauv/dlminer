package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;

public class HypoFitComparator extends HypoComparator {
	
	public HypoFitComparator(SortingOrder order) {
		this.order = order;
	}
	
	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		if (order.equals(SortingOrder.ASC)) {
			return h1.fitness.compareTo(h2.fitness);
		}
		return - h1.fitness.compareTo(h2.fitness);		
	}
}
