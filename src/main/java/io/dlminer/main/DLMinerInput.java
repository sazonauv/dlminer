package io.dlminer.main;

import io.dlminer.ont.Logic;
import io.dlminer.ont.OntologyFormat;
import io.dlminer.ont.ReasonerName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import io.dlminer.refine.OperatorConfig;
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
     * Configuration for hypothesis generation procedures
     */
    private OperatorConfig config;
	

	
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
	 * An input ontology that DL-Miner processes. The ontology must be consistent.	  
	 * The file should be in one of standard OWL syntaxes, 
	 * see https://en.wikipedia.org/wiki/Web_Ontology_Language.
	 */
	private InputStream ontologyFile;

	
	
	/**
	 * The format to store hypotheses, e.g. OWL/XML, Turtle
	 */
	private OntologyFormat hypothesisFormat;
	
	

	
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
     * The flag indicating if complex measures should used
     */
	private Boolean useComplexMeasures;
	
	
	
	// ====================== 	getters and setters ====================== 


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



    public boolean isUseComplexMeasures() {
        return useComplexMeasures;
    }

    public void setUseComplexMeasures(boolean useComplexMeasures) {
        this.useComplexMeasures = useComplexMeasures;
    }


    public OperatorConfig getConfig() {
        return config;
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

	    // configuration
	    config = new OperatorConfig();
	    config.maxDepth = DLMinerInputI.DEF_MAX_ROLE_DEPTH;
	    config.minSupport = DLMinerInputI.DEF_MIN_CONCEPT_SUPPORT;
	    config.beamSize = DLMinerInputI.DEF_BEAM_SIZE;
	    config.maxLength = DLMinerInputI.DEF_MAX_CONCEPT_LENGTH;
	    config.checkDisjointness = DLMinerInputI.DEF_USE_DISJOINTNESS;
	    config.useDisjunction = DLMinerInputI.DEF_USE_DISJUNCTION;
	    config.useUniversalRestriction = DLMinerInputI.DEF_USE_UNIVERSAL;
	    config.useNegation = DLMinerInputI.DEF_USE_NEGATION;
	    config.useDataProperties = DLMinerInputI.DEF_USE_DATA_PROPERTIES;
	    config.dataThresholdsNumber = DLMinerInputI.DEF_DATA_THRESHOLDS_NUMBER;
	    config.checkRedundancy = DLMinerInputI.DEF_USE_REDUNDANCY;
        config.useReasonerForAtomicClassInstances = DLMinerInputI.DEF_USE_REASONER_FOR_ATOMIC_CLASS_INSTANCES;
	    config.useReasonerForClassInstances = DLMinerInputI.DEF_USE_REASONER_FOR_CLASS_INSTANCES;

	    // other parameters
		maxHypothesesNumber = DLMinerInputI.DEF_MAX_HYPOTHESES_NUMBER;
		reasonerTimeout = DLMinerInputI.DEF_REASONER_TIMEOUT;
		minPrecision = DLMinerInputI.DEF_MIN_PRECISION;
		hypothesisFormat = DLMinerInputI.DEF_HYPOTHESIS_FORMAT;
		logic = DLMinerInputI.DEF_LOGIC;
		reasonerName = DLMinerInputI.DEF_REASONER_NAME;
		useMinSupport = DLMinerInputI.DEF_USE_MIN_SUPPORT;
		useMinPrecision = DLMinerInputI.DEF_USE_MIN_PRECISION;
		useConsistency = DLMinerInputI.DEF_USE_CONSISTENCY;
		dlminerMode = DLMinerInputI.DEF_DLMINER_MODE;
		useCleaning = DLMinerInputI.DEF_USE_CLEANING;
		useClosedWorldAssumption = DLMinerInputI.DEF_USE_CWA;
		useComplexMeasures = DLMinerInputI.DEF_USE_COMPLEX_MEASURES;

	}


}
