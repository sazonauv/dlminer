package io.dlminer.print;

public interface CSV {	
	
	// Delimiter used in CSV file
	public static final String EMPTY_VALUE = "-";
	public static final String SPLIT_LINE = " ========================= ";
	public static final String NULL_VALUE = "null";

	public static final String COMMA_DELIMITER = ",";
	public static final String NEW_LINE_SEPARATOR = "\n";

	public static final String[] LANGUAGES = new String[] {
		"A", "A#C", "A#C#R_SOME_C", "A#C#R_SOME_C#INV_R_SOME_C", 
		"A#C#R_SOME_C#INV_R_SOME_C#R", "A#C#R_SOME_C#INV_R_SOME_C#R#INV_R", 
		"A#C#R_SOME_C#INV_R_SOME_C#R#INV_R#R_CHAIN_S"	
	};

	public static final String[] SIZES = new String[] {
		"2", "4", "6", "8"	
	};
	
}
