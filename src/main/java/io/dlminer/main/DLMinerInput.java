package io.dlminer.main;

import io.dlminer.ont.Logic;
import io.dlminer.ont.OntologyFormat;
import io.dlminer.ont.ReasonerName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Slava Sazonau
 * The University of Manchester
 * Information Management Group
 * 
 * The class encapsulates input parameters of the DL-Miner algorithm.
 */
public class DLMinerInput {
	
	// ====================== attributes ====================== 
	
	/**
	 * The maximal role depth in a concept that DL-Miner should inspect,
	 * e.g. "hasParent some Person" has role depth = 1 and
	 * "hasParent some (Person and hasParent some Person)" has role depth = 2.
	 */
	private Integer maxRoleDepth;

	
	/**
	 * The maximal number of hypotheses that DL-Miner is allowed to return.
	 * If this number is exceeded, the algorithm returns only top hypotheses.
	 */
	private Integer maxHypothesesNumber;
	
	
	/**
	 * The timeout in seconds that the reasoner is permitted to process
	 * an ontology. If the timeout is exceeded, the reasoner
	 * throws an exception and DL-miner does not even start its computations. 
	 * Therefore, the timeout is recommended to be set to a sufficiently
	 * large value, e.g. 6000 seconds = 100 minutes.
	 */
	private Integer reasonerTimeout;

	
	/**
	 * The minimal precision that a hypothesis must have, 
	 * e.g. 0.8 means that a hypothesis must be at least 80% accurate.
	 * It weighs the support of a hypothesis against its
	 * assumption. The higher the minimal precision is,
	 * the more credible hypotheses DL-Miner tend to return, 
	 * but fewer of them. Some recommended values are 0.8, 0.9.
	 */
	private Double minPrecision;

	
	
	/**
	 * The minimal support in data that a concept is required to have
	 * in order to be inspected by DL-Miner. This is measured in number of
	 * individuals and ontology specific. The higher the minimal support is,
	 * the faster DL-Miner terminates, but returns fewer hypotheses.
	 * It is recommended to be set between 1% and 5% of all individuals for big ontologies, 
	 * e.g. 100 - 500 for an ontology with 10,000 individuals.
	 */
	private Integer minConceptSupport;

	
	
	
	/**
	 * An input ontology that DL-Miner processes. The ontology must be consistent.	  
	 * The file should be in one of standard OWL syntaxes, 
	 * see https://en.wikipedia.org/wiki/Web_Ontology_Language.
	 */
	private InputStream ontologyFile;

	
	
	/**
	 * The beam size in the beam search routine. This parameter is important
	 * to maintain tractability for ontologies with a lot of data.
	 */
	private Integer beamSize;
	
	
	
	/**
	 * The format to store hypotheses, e.g. OWL/XML, Turtle
	 */
	private OntologyFormat hypothesisFormat;
	
	
	/**
	 * The maximal length of a concept that DL-Miner should inspect,
	 * e.g. "hasParent some Person" has concept length = 2 and
	 * "hasParent some (Person and hasParent some Person)" has concept length = 5.
	 */
	private Integer maxConceptLength;
	
	
	/**
	 * The logic for hypotheses
	 */
	private Logic logic;
	
	
	
	/**
	 * The reasoner for internal operations, 
	 * e.g. classification, instance checking
	 */
	private ReasonerName reasonerName;
	
	
	
	/**
	 * The flag indicating whether the minimal support 
	 * is used to filter hypotheses (normally is set to TRUE)
	 */
	private Boolean useMinSupport;
	
	
	
	
	/**
	 * The flag indicating whether disjoint classes are checked
	 */
	private Boolean useDisjointness;
	
	
	/**
	 * The flag indicating whether disjunction is used for constructing classes
	 */
	private Boolean useDisjunction;
	
	
	
	/**
	 * The flag indicating whether universal restriction is used for constructing classes
	 */
	private Boolean useUniversalRestriction;
	
	
	/**
	 * The flag indicating whether negation is used for constructing classes
	 */
	private Boolean useNegation;
	
	
	
	/**
	 * The flag indicating whether the minimal precision is used to filter hypotheses
	 */
	private Boolean useMinPrecision;
	
	
	
	/**
	 * The flag indicating whether consistency is used to filter hypotheses
	 */
	private Boolean useConsistency;
	
	
	
	
	/**
	 * The mode the algorithm operates in
	 */
	private DLMinerMode dlminerMode;
	
	
	
	
	/**
	 * The positive class for predictions
	 */
	private OWLClass positiveClass;
	
	
	/**
	 * The negative class for predictions
	 */
	private OWLClass negativeClass;
	
	
	
	
	/**
	 * The flag indicating whether hypotheses are cleaned
	 */
	private Boolean useCleaning;
	
	
	
	/**
	 * The name of seed class for learning
	 */
	private String seedClassName;
	
	
	
	
	/**
	 * The flag indicating whether the data is viewed under CWA
	 */
	private Boolean useClosedWorldAssumption;



    /**
     * The flag indicating whether data properties are used to generate concept expressions
     */
	private Boolean useDataProperties;


    /**
	 * The number of thresholds for expressions with data properties
	 */
	private Integer dataThresholdsNumber;



    /**
     * The flag indicating if complex measures should used
     */
	private Boolean useComplexMeasures;
	
	
	
	// ====================== 	getters and setters ====================== 

	/**
	 * @return the maxRoleDepth
	 */
	public Integer getMaxRoleDepth() {
		return maxRoleDepth;
	}



	/**
	 * @param maxRoleDepth the maxRoleDepth to set
	 */
	public void setMaxRoleDepth(Integer maxRoleDepth) {
		this.maxRoleDepth = maxRoleDepth;		
	}



	/**
	 * @return the maxHypothesesNumber
	 */
	public Integer getMaxHypothesesNumber() {
		return maxHypothesesNumber;
	}



	/**
	 * @param maxHypothesesNumber the maxHypothesesNumber to set
	 */
	public void setMaxHypothesesNumber(Integer maxHypothesesNumber) {
		this.maxHypothesesNumber = maxHypothesesNumber;		
	}



	/**
	 * @return the reasonerTimeout
	 */
	public Integer getReasonerTimeout() {
		return reasonerTimeout;
	}



	/**
	 * @param reasonerTimeout the reasonerTimeout to set
	 */
	public void setReasonerTimeout(Integer reasonerTimeout) {
		this.reasonerTimeout = reasonerTimeout;		
	}



	/**
	 * @return the minPrecision
	 */
	public Double getMinPrecision() {
		return minPrecision;
	}



	/**
	 * @param minPrecision the assumptionPenalty to set
	 */
	public void setMinPrecision(Double minPrecision) {
		this.minPrecision = minPrecision;		
	}



	/**
	 * @return the minConceptSupport
	 */
	public Integer getMinConceptSupport() {
		return minConceptSupport;
	}



	/**
	 * @param minConceptSupport the minConceptSupport to set
	 */
	public void setMinConceptSupport(Integer minConceptSupport) {
		this.minConceptSupport = minConceptSupport;		
	}
	
	
	
	/**
	 * @return the ontologyFile
	 */
	public InputStream getOntologyFile() {
		return ontologyFile;
	}



	/**
	 * @param ontologyFile the ontologyFile to set
	 */
	public void setOntologyFile(InputStream ontologyFile) {
		this.ontologyFile = ontologyFile;
	}
	
	
	
	/**
	 * @param ontologyFile the ontologyFile to set
	 */
	public void setOntologyFile(File ontologyFile) {
		InputStream ontologyStream = null;
        try {
        	ontologyStream = new FileInputStream(ontologyFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.ontologyFile = ontologyStream;
	}
	
	
	
	
	/**
	 * @return the beamSize
	 */
	public Integer getBeamSize() {
		return beamSize;
	}



	/**
	 * @param beamSize the beamSize to set
	 */
	public void setBeamSize(Integer beamSize) {
		this.beamSize = beamSize;
	}

	
	
	/**
	 * @return the hypothesisFormat
	 */
	public OntologyFormat getHypothesisFormat() {
		return hypothesisFormat;
	}



	/**
	 * @param hypothesisFormat the hypothesisFormat to set
	 */
	public void setHypothesisFormat(OntologyFormat hypothesisFormat) {
		this.hypothesisFormat = hypothesisFormat;
	}
	
	
	
	/**
	 * @return the maxConceptLength
	 */
	public Integer getMaxConceptLength() {
		return maxConceptLength;
	}



	/**
	 * @param maxConceptLength the maxConceptLength to set
	 */
	public void setMaxConceptLength(Integer maxConceptLength) {
		this.maxConceptLength = maxConceptLength;
	}
	
	
	
	/**
	 * @return the logic for hypotheses
	 */
	public Logic getLogic() {
		return logic;
	}



	/**
	 * @param logic the logic for hypotheses
	 */
	public void setLogic(Logic logic) {
		this.logic = logic;
	}

	
	/**
	 * @return the reasonerName
	 */
	public ReasonerName getReasonerName() {
		return reasonerName;
	}



	/**
	 * @param reasonerName the reasonerName
	 */
	public void setReasonerName(ReasonerName reasonerName) {
		this.reasonerName = reasonerName;
	}
	
	
	
	/**
	 * @return the useCleaning
	 */
	public boolean isUseCleaning() {
		return useCleaning;
	}



	/**
	 * @param useCleaning the useCleaning to set
	 */
	public void setUseCleaning(boolean useCleaning) {
		this.useCleaning = useCleaning;
	}



	/**
	 * @return the useMinSupport
	 */
	public boolean isUseMinSupport() {
		return useMinSupport;
	}



	/**
	 * @param useMinSupport the useMinSupport to set
	 */
	public void setUseMinSupport(boolean useMinSupport) {
		this.useMinSupport = useMinSupport;
	}



	/**
	 * @return the useDisjointness
	 */
	public boolean isUseDisjointness() {
		return useDisjointness;
	}



	/**
	 * @param useDisjointness the useDisjointness to set
	 */
	public void setUseDisjointness(Boolean useDisjointness) {
		this.useDisjointness = useDisjointness;
	}
	
	
	
	/**
	 * @return the useDisjunction
	 */
	public boolean isUseDisjunction() {
		return useDisjunction;
	}



	/**
	 * @param useDisjunction the useDisjunction to set
	 */
	public void setUseDisjunction(boolean useDisjunction) {
		this.useDisjunction = useDisjunction;
	}



	/**
	 * @return the useUniversalRestriction
	 */
	public boolean isUseUniversalRestriction() {
		return useUniversalRestriction;
	}



	/**
	 * @param useUniversalRestriction the useUniversalRestriction to set
	 */
	public void setUseUniversalRestriction(boolean useUniversalRestriction) {
		this.useUniversalRestriction = useUniversalRestriction;
	}



	/**
	 * @return the useNegation
	 */
	public boolean isUseNegation() {
		return useNegation;
	}



	/**
	 * @param useNegation the useNegation to set
	 */
	public void setUseNegation(boolean useNegation) {
		this.useNegation = useNegation;
	}



	/**
	 * @return the useMinPrecision
	 */
	public boolean isUseMinPrecision() {
		return useMinPrecision;
	}



	/**
	 * @param useMinPrecision the useMinPrecision to set
	 */
	public void setUseMinPrecision(Boolean useMinPrecision) {
		this.useMinPrecision = useMinPrecision;
	}
	
	
	public boolean isUseConsistency() {
		return useConsistency;
	}



	public void setUseConsistency(boolean useConsistency) {
		this.useConsistency = useConsistency;
	}
	
	
	
	/**
	 * @return the dlminerMode
	 */
	public DLMinerMode getDlminerMode() {
		return dlminerMode;
	}



	/**
	 * @param dlminerMode the dlminerMode to set
	 */
	public void setDlminerMode(DLMinerMode dlminerMode) {
		this.dlminerMode = dlminerMode;
	}

	

	/**
	 * @return the positiveClass
	 */
	public OWLClass getPositiveClass() {
		return positiveClass;
	}



	/**
	 * @param positiveClass the positiveClass to set
	 */
	public void setPositiveClass(OWLClass positiveClass) {
		this.positiveClass = positiveClass;
	}



	/**
	 * @return the negativeClass
	 */
	public OWLClass getNegativeClass() {
		return negativeClass;
	}



	/**
	 * @param negativeClass the negativeClass to set
	 */
	public void setNegativeClass(OWLClass negativeClass) {
		this.negativeClass = negativeClass;
	}

	
	/**
	 * @return the seedClassName
	 */
	public String getSeedClassName() {
		return seedClassName;
	}



	/**
	 * @param seedClassName the seedClassName to set
	 */
	public void setSeedClassName(String seedClassName) {
		this.seedClassName = seedClassName;
	}
	
	
	
	/**
	 * @return the useClosedWorldAssumption
	 */
	public boolean isUseClosedWorldAssumption() {
		return useClosedWorldAssumption;
	}



	/**
	 * @param useClosedWorldAssumption the useClosedWorldAssumption to set
	 */
	public void setUseClosedWorldAssumption(boolean useClosedWorldAssumption) {
		this.useClosedWorldAssumption = useClosedWorldAssumption;
	}


    /**
     *
     * @param useDataProperties the flag to use data properties
     */
    public void setUseDataProperties(boolean useDataProperties) {
        this.useDataProperties = useDataProperties;
    }


    /**
     *
     * @return the flag to use data properties
     */
    public boolean isUseDataProperties() {
        return useDataProperties;
    }




	/**
	 *
	 * @return the number of thresholds for expressions with data properties
	 */
	public Integer getDataThresholdsNumber() {
		return dataThresholdsNumber;
	}


	/**
	 *
	 * @param dataThresholdsNumber the number of thresholds for expressions with data properties
	 */
	public void setDataThresholdsNumber(Integer dataThresholdsNumber) {
		this.dataThresholdsNumber = dataThresholdsNumber;
	}



    public boolean isUseComplexMeasures() {
        return useComplexMeasures;
    }

    public void setUseComplexMeasures(boolean useComplexMeasures) {
        this.useComplexMeasures = useComplexMeasures;
    }
	
	
	// ====================== constructors ======================

	

	/**
	 * @param ontologyFile ontologyFile
	 */
	public DLMinerInput(File ontologyFile) {		
		setOntologyFile(ontologyFile);
        init();
	}
	
	
	/**
	 * @param ontologyFile ontologyFile
	 */
	public DLMinerInput(InputStream ontologyFile) {
		this.ontologyFile = ontologyFile;
		init();
	}
		

	private void init() {
		this.maxRoleDepth = DLMinerInputI.DEF_MAX_ROLE_DEPTH;
		this.maxHypothesesNumber = DLMinerInputI.DEF_MAX_HYPOTHESES_NUMBER;
		this.reasonerTimeout = DLMinerInputI.DEF_REASONER_TIMEOUT;
		this.minPrecision = DLMinerInputI.DEF_MIN_PRECISION;
		this.minConceptSupport = DLMinerInputI.DEF_MIN_CONCEPT_SUPPORT;
		this.beamSize = DLMinerInputI.DEF_BEAM_SIZE;
		this.maxConceptLength = DLMinerInputI.DEF_MAX_CONCEPT_LENGTH;
		this.hypothesisFormat = DLMinerInputI.DEF_HYPOTHESIS_FORMAT;
		this.logic = DLMinerInputI.DEF_LOGIC;
		this.reasonerName = DLMinerInputI.DEF_REASONER_NAME;
		this.useMinSupport = DLMinerInputI.DEF_USE_MIN_SUPPORT;
		this.useDisjointness = DLMinerInputI.DEF_USE_DISJOINTNESS;
		this.useDisjunction = DLMinerInputI.DEF_USE_DISJUNCTION;
		this.useUniversalRestriction = DLMinerInputI.DEF_USE_UNIVERSAL;
		this.useNegation = DLMinerInputI.DEF_USE_NEGATION;
		this.useMinPrecision = DLMinerInputI.DEF_USE_MIN_PRECISION;
		this.useConsistency = DLMinerInputI.DEF_USE_CONSISTENCY;
		this.dlminerMode = DLMinerInputI.DEF_DLMINER_MODE;
		this.useCleaning = DLMinerInputI.DEF_USE_CLEANING;
		this.useClosedWorldAssumption = DLMinerInputI.DEF_USE_CWA;
		this.useDataProperties = DLMinerInputI.DEF_USE_DATA_PROPERTIES;
		this.dataThresholdsNumber = DLMinerInputI.DEF_DATA_THRESHOLDS_NUMBER;
		this.useComplexMeasures = DLMinerInputI.DEF_USE_COMPLEX_MEASURES;
	}


}
