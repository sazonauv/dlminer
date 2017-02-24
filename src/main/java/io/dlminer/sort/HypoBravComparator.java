package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;

public class HypoBravComparator extends HypoComparator {	
	
	
	public HypoBravComparator(SortingOrder order) {
		this.order = order;
	}

	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		if (order.equals(SortingOrder.ASC)) {
			return h1.braveness.compareTo(h2.braveness);
		}
		return - h1.braveness.compareTo(h2.braveness);		
	}
}
