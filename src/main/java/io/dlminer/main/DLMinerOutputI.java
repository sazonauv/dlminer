package io.dlminer.main;

/**
 * @author Slava Sazonau
 * The University of Manchester
 * Information Management Group
 * 
 * The interface contains constants for the results of the DL-Miner algorithm
 * and error messages.
 */
public interface DLMinerOutputI {

		
	// ====================== error messages ======================
	
	String EMPTY_OUTPUT_ERROR = "The algorithm has produced the empty output. " +
			"This may be caused by excessively strict input parameters " +
			"or an ontology having little to be mined.";
	
	String AXIOM_BUILDING_ERROR = ": Ignore and continue building axioms";	
	String HYPOTHESIS_EVALUATION_ERROR = ": Ignore and continue evaluating hypotheses.";
	String WRONG_REASONER_MODE_ERROR = "ALC concepts can be constructed only using the NORMAL reasoner mode";
	String CONCEPT_BUILDING_ERROR = ": Ignore and continue building concepts";

	String REASONER_UPDATE_ERROR = ": Evaluation is not possible due to the reasoning error";

	String ENTAILMENT_CHEKING_ERROR = ": Entailment checking has failed";
	
}
