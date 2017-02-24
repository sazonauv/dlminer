package io.dlminer.learn;

public enum Language {
	// atomics
	A("A"),
	// subconcepts
	C("C"),
	NOT_C("NOT_C"),
	// existential restrictions
	R_SOME_C("R_SOME_C"),
	R_SOME_NOT_C("R_SOME_NOT_C"),
	R_SOME_S_SOME_C("R_SOME_S_SOME_C"),
	INV_R_SOME_C("INV_R_SOME_C"),
	// universal restrictions
	R_ONLY_C("R_ONLY_C"),
	R_ONLY_NOT_C("R_ONLY_NOT_C"),
	// conjunctions
	C_AND_D("C_AND_D"),
	C_AND_NOT_D("C_AND_NOT_D"),	
	C_AND_R_SOME_D("C_AND_R_SOME_D"),
	NOT_C_AND_R_SOME_D("NOT_C_AND_R_SOME_D"),
	R_SOME_C_AND_D("R_SOME_C_AND_D"),	
	// disjunctions
	C_OR_D("C_OR_D"),
	C_OR_NOT_D("C_OR_NOT_D"),
	C_OR_R_SOME_D("C_OR_R_SOME_D"),
	NOT_C_OR_R_SOME_D("NOT_C_OR_R_SOME_D"),
	R_SOME_C_OR_D("R_SOME_C_OR_D"),
	// learned concepts
	CDL("CDL"), 
	// data-driven concepts
	DATA_C("DATA_C"),	
	// roles
	R("R"),
	INV_R("INV_R"),
	R_CHAIN_S("R_CHAIN_S"),
	INV_R_CHAIN_S("INV_R_CHAIN_S");
	
	
	public static String DELIMITER = "#";
	public static String ILLEGAL_LANG = "Illegal input language";
	public static String ILLEGAL_INIT_LANG = "Illegal initial language: only A (atomic concept) and C (sub-concepts) are allowed";
	
	private final String name;

	Language(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}	
		
}
