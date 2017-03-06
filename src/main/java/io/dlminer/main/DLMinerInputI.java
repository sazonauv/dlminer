package io.dlminer.main;

import org.semanticweb.owlapi.model.OWLOntologyFormat;

import io.dlminer.ont.Logic;
import io.dlminer.ont.OntologyFormat;
import io.dlminer.ont.ReasonerLoader;
import io.dlminer.ont.ReasonerName;

/**
 * @author Slava Sazonau
 * The University of Manchester
 * Information Management Group
 * 
 * The interface contains default parameters of the DL-Miner algorithm
 * and error messages.
 */
public interface DLMinerInputI {
	
	// ====================== default parameters ======================
	
	/**
	 * Default maxRoleDepth
	 */
	Integer DEF_MAX_ROLE_DEPTH = 1;
	
	
	/**
	 * Default maxHypothesesNumber
	 */
	Integer DEF_MAX_HYPOTHESES_NUMBER = 500000;
	
	
	/**
	 * Default reasonerTimeout in seconds
	 */
	Integer DEF_REASONER_TIMEOUT = 6000;
	
	
	/**
	 * Default minPrecision
	 */
	Double DEF_MIN_PRECISION = 0.9;
	
	
	/**
	 * Default minConceptSupport
	 */
	Integer DEF_MIN_CONCEPT_SUPPORT = 10;
	
	
	
	/**
	 * Default beamSize
	 */
	Integer DEF_BEAM_SIZE = 10000;
	
	
	/**
	 * Default maxConceptLength
	 */
	Integer DEF_MAX_CONCEPT_LENGTH = 5;
	
	
	/**
	 * Default ontology format to store hypotheses
	 */
	OntologyFormat DEF_HYPOTHESIS_FORMAT = OntologyFormat.OWLXML;
	
	
	/**
	 * Default logic for hypotheses
	 */
	Logic DEF_LOGIC = Logic.EL;
	
	
	
	/**
	 * Default reasoner
	 */
	ReasonerName DEF_REASONER_NAME = ReasonerLoader.DEF_REASONER;
	
	

	/**
	 * Default flag for using minimal support
	 */
	Boolean DEF_USE_MIN_SUPPORT = true;
	
	
	/**
	 * Default flag for using disjointness
	 */
	Boolean DEF_USE_DISJOINTNESS = false;
	
	
	
	/**
	 * Default flag for using disjunction
	 */
	Boolean DEF_USE_DISJUNCTION = false;
	
	

	/**
	 * Default flag for using universal restriction
	 */
	Boolean DEF_USE_UNIVERSAL = false;



	/**
	 * Default flag for using negation
	 */
	Boolean DEF_USE_NEGATION = false;


	
	
	/**
	 * Default flag for using minimal precision
	 */
	Boolean DEF_USE_MIN_PRECISION = true;
	
	

	/**
	 * Default flag for using consistency
	 */
	Boolean DEF_USE_CONSISTENCY = false;
	
	
	

	/**
	 * Default mode the algorithm operates in
	 */
	DLMinerMode DEF_DLMINER_MODE = DLMinerMode.NORM;
	
	
	/**
	 * Default flag for using cleaning
	 */
	Boolean DEF_USE_CLEANING = false;
	
	
	/**
	 * Default flag for using the Closed World Assumption (CWA)
	 */
	Boolean DEF_USE_CWA = false;
	
		
	
	// ====================== error messages ======================
	
	// null parameter errors
	String NULL_MAX_ROLE_DEPTH_ERR = "maxRoleDepth cannot be empty!";
	String NULL_MAX_HYPOTHESES_NUMBER_ERR = "maxHypothesesNumber cannot be empty!";
	String NULL_REASONER_TIMEOUT_ERR = "reasonerTimeout cannot be empty!";
	String NULL_MIN_PRECISION_ERR = "minPrecision cannot be empty!";
	String NULL_MIN_CONCEPT_SUPPORT_ERR = "minConceptSupport cannot be empty!";
	
	
	// wrong parameter value errors
	String WRONG_MAX_ROLE_DEPTH_ERR = "maxRoleDepth cannot be negative (can only be zero or positive)!";
	String WRONG_MAX_HYPOTHESES_NUMBER_ERR = "maxHypothesesNumber can only be positive!";
	String WRONG_REASONER_TIMEOUT_ERR = "reasonerTimeout can only be positive!";
	String WRONG_MIN_PRECISION_ERR = "minPrecision can only be positive!";
	String WRONG_MIN_CONCEPT_SUPPORT_ERR = "minConceptSupport can only be positive!";
	
	
	// ontology errors
	String NULL_ONTOLOGY_ERR = "ontology cannot be empty!";
	String INCONSISTENT_ONTOLOGY_ERR = "your ontology is inconsistent! DL-Miner can only process consistent ontologies.";
	String PARSING_ONTOLOGY_ERR = "your ontology cannot be parsed! check your file.";


				
}
