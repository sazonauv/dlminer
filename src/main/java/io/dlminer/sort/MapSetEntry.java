package io.dlminer.sort;

import java.util.Set;


public class MapSetEntry<K, E> {
	
	public K key;
	
	public Set<E> value;

	public MapSetEntry(K key, Set<E> value) {
		this.key = key;
		this.value = value;
	}
	
			
}
