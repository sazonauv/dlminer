package io.dlminer.ont;

public enum Logic {	
	
	EL("EL"),
	ALC("ALC");
	
	private final String name;

	Logic(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
