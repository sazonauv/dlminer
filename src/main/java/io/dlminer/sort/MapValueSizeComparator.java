package io.dlminer.sort;

import java.util.Comparator;


public class MapValueSizeComparator<K, E> 
	extends AbstractComparator 
	implements Comparator<MapSetEntry<K, E>> {


	public MapValueSizeComparator(SortingOrder order) {
		this.order = order;
	}

		
	@Override
	public int compare(MapSetEntry<K, E> o1,
			MapSetEntry<K, E> o2) {
		if (order.equals(SortingOrder.ASC)) {
			return Integer.compare(o1.value.size(), o2.value.size());
		}
		return - Integer.compare(o1.value.size(), o2.value.size());
	}

}
