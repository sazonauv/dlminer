package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;

public class HypoFitLowBravLowBySignatureComparator extends HypoComparator {
	
	private double cost;
	
	public HypoFitLowBravLowBySignatureComparator(double cost, SortingOrder order) {
		this.cost = cost;
		this.order = order;
	}

	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		Double val1 = (double) (h1.support - cost*h1.assumption) / h1.signature.size();
		Double val2 = (double) (h2.support - cost*h2.assumption) / h2.signature.size();
		if (order.equals(SortingOrder.ASC)) {
			return val1.compareTo(val2);
		} 
		return - val1.compareTo(val2);		
	}
}
