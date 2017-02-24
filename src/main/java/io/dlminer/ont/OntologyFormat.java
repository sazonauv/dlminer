package io.dlminer.ont;

public enum OntologyFormat {
	
	OWLXML("OWLXML"),
	TURTLE("TURTLE");	
	
	private final String name;

	OntologyFormat(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	
}
