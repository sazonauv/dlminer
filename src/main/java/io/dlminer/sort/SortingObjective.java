package io.dlminer.sort;

public enum SortingObjective {
	
	FITNESS("FITNESS"),
	BRAVENESS("BRAVENESS"),
	INTEREST("INTEREST"),
	LENGTH("LENGTH"),	
	GENERALITY("GENERALITY");
	
	
	private final String name;

	SortingObjective(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}