package io.dlminer.learn;

public class Pair<T> {
	
	public T first;
	public T second;
	
	
	public Pair() {
		
	}
	
	
	public Pair(T first, T second) {		
		this.first = first;
		this.second = second;
	}

	
	@Override
	public int hashCode() {
		return first.hashCode() * second.hashCode() + first.hashCode() ^ second.hashCode();
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Pair)) {
			return false;
		}
		Pair<T> pair = (Pair<T>) obj;
		return (pair.first.equals(first) && pair.second.equals(second))
				|| (pair.first.equals(second) && pair.second.equals(first));
	}

	
	@Override
	public String toString() {
		return "(" + first.toString() + ", " + second.toString() + ")";
	}

}
