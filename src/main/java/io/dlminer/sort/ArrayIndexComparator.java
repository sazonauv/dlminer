package io.dlminer.sort;

import java.util.Comparator;

/**
 * @author Slava Sazonau
 * 
 * The class is used as a comparator to receive indices of array elements
 * after sorting.
 * It does not change an input array and only produces indices.
 */
public class ArrayIndexComparator 
	extends AbstractComparator
	implements Comparator<Integer> {
	
	// An array for sorting in the object form
	private Double[] array;

	/**
	 * @param array: An array for sorting
	 */
	public ArrayIndexComparator(int[] array, SortingOrder order) {
		this.order = order;
		toObjectArray(array);
	}
	
	/**
	 * @param array: An array for sorting
	 */
	public ArrayIndexComparator(double[] array, SortingOrder order) {
		this.order = order;
		toObjectArray(array);
	}	
		
	/**Translates an input array to an object array
	 * 
	 * @param array: An array for sorting
	 */
	private void toObjectArray(int[] array) {
		this.array = new Double[array.length];
		for (int i=0; i<array.length; i++) {
			this.array[i] = new Double(array[i]);
		}
	}
	
	/**Translates an input array to an object array
	 * 
	 * @param array: An array for sorting
	 */
	private void toObjectArray(double[] array) {
		this.array = new Double[array.length];
		for (int i=0; i<array.length; i++) {
			this.array[i] = new Double(array[i]);
		}
	}
	
	/**
	 * @return indexes of array elements after sorting
	 */
	public Integer[] createIndexArray() {
		Integer[] indexes = new Integer[array.length];
		for (int i=0; i<array.length; i++) {
			indexes[i] = i;
		}
		return indexes;
	}

	@Override
	public int compare(Integer index1, Integer index2) {
		if (order.equals(SortingOrder.ASC)) {
			return array[index1].compareTo(array[index2]);
		} else {
			return - array[index1].compareTo(array[index2]);
		}
	}
}
