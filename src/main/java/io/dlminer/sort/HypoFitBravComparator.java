package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;


public class HypoFitBravComparator extends HypoComparator {
	
	private double cost;	
	
	public HypoFitBravComparator(double cost, SortingOrder order) {		
		this.cost = cost;
		this.order = order;
	}


	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {
		Double val1 = (double) (h1.fitness - cost*h1.braveness);
		Double val2 = (double) (h2.fitness - cost*h2.braveness);
		if (order.equals(SortingOrder.ASC)) {
			return val1.compareTo(val2);
		}
		return - val1.compareTo(val2);		
	}
}
