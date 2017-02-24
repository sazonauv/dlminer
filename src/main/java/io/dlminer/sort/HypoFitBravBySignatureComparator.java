package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;

public class HypoFitBravBySignatureComparator extends HypoComparator {
	
	private double cost;	
	
	public HypoFitBravBySignatureComparator(double cost, SortingOrder order) {		
		this.cost = cost;
		this.order = order;
	}

	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		Double val1 = (double)(h1.fitness - cost*h1.braveness)/h1.signature.size();
		Double val2 = (double)(h2.fitness - cost*h2.braveness)/h2.signature.size();
		if (order.equals(SortingOrder.ASC)) {
			return val1.compareTo(val2);
		}
		return - val1.compareTo(val2);		
	}
}
