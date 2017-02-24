package io.dlminer.main;

public enum DLMinerMode {	
	
	NORM("NORM"),
	CDL("CDL"),
	KBC("KBC");	
	
	private final String name;

	DLMinerMode(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}	
}
