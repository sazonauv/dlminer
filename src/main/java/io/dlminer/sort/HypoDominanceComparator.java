package io.dlminer.sort;

import io.dlminer.learn.Hypothesis;

import java.util.Comparator;



public class HypoDominanceComparator implements Comparator<Hypothesis> {	

	@Override
	public int compare(Hypothesis h1, Hypothesis h2) {				
		// h1 dominates
		if ( dominates(h1, h2) ) {
			return -1;
		} else
		// h2 dominates
		if ( dominates(h2, h1) ) {
			return 1;			
		} else {
		// equal
			return 0;
		}		
	}
	
	public static boolean dominates(Hypothesis h1, Hypothesis h2) {
//		if (h1.signature.equals(h2.signature)) {
			// at least one measure is worse
			for (int i=0; i<h1.measures.length; i++) {
				if (h1.measures[i] < h2.measures[i]) {
					return false;
				}
			}
			// at least one measure is better
			for (int i=0; i<h1.measures.length; i++) {
				if (h1.measures[i] > h2.measures[i]) {
					return true;
				}
			}
			// all measures are equal			
//		}
		return false;
	}
	
	/*public static boolean dominatesEqual(Hypothesis h1, Hypothesis h2) {
		if (h1.signature.equals(h2.signature)) {
			int cfit;
			int cbra;
			if (h1.fitness != null && h1.braveness != null
					&& h2.fitness != null && h2.braveness != null) {
				cfit = h1.fitness.compareTo(h2.fitness);
				cbra = h1.braveness.compareTo(h2.braveness);					
			} else {
				cfit = h1.support.compareTo(h2.support);
				cbra = h1.assumption.compareTo(h2.assumption);							
			}
			// h1 dominates or equals
			if ((cfit >= 0 && cbra <= 0)) {
				return true;
			}
		}
		return false;
	}*/
}