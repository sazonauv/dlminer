package io.dlminer.main;

import io.dlminer.learn.AxiomConfig;
import io.dlminer.ont.OntologyFormat;
import io.dlminer.ont.ReasonerName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import io.dlminer.refine.OperatorConfig;
import org.semanticweb.owlapi.model.OWLOntology;

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
     * Configuration for concept generation procedures
     */
    private OperatorConfig operatorConfig;



    /**
     * Configuration for axiom generation procedures
     */
    private AxiomConfig axiomConfig;
	

	
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
	 * An input ontology that DL-Miner processes. The ontology must be consistent.	  
	 * The file should be in one of standard OWL syntaxes, 
	 * see https://en.wikipedia.org/wiki/Web_Ontology_Language.
	 */
	private InputStream ontologyStream;



    /**
     * The file of an input ontology
     */
    private File ontologyFile;



    /**
     * An input ontology that DL-Miner processes.
     */
    private OWLOntology ontology;

	
	
	/**
	 * The format to store hypotheses, e.g. OWL/XML, Turtle
	 */
	private OntologyFormat hypothesisFormat;
	

	
	/**
	 * The reasoner for internal operations, 
	 * e.g. classification, instance checking
	 */
	private ReasonerName reasonerName;
	
	

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




    public File getOntologyFile() {
        return ontologyFile;
    }


	
	/**
	 * @return the ontologyStream
	 */
	public InputStream getOntologyStream() {
		return ontologyStream;
	}



	/**
	 * @param ontologyStream the ontologyStream to set
	 */
	public void setOntologyStream(InputStream ontologyStream) {
		this.ontologyStream = ontologyStream;
	}
	
	
	
	/**
	 * @param ontologyFile the ontologyStream to set
	 */
	public void setOntologyFile(File ontologyFile) {
	    this.ontologyFile = ontologyFile;
		InputStream ontologyStream = null;
        try {
        	ontologyStream = new FileInputStream(ontologyFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.ontologyStream = ontologyStream;
	}



    public OWLOntology getOntology() {
        return ontology;
    }

    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
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


    public OperatorConfig getOperatorConfig() {
        return operatorConfig;
    }


    public AxiomConfig getAxiomConfig() {
        return axiomConfig;
    }


    // ====================== constructors ======================


    /**
     * @param ontologyFilePath ontologyFilePath
     */
    public DLMinerInput(String ontologyFilePath) {
        setOntologyFile(new File(ontologyFilePath));
        init();
    }
	

	/**
	 * @param ontologyFile ontologyStream
	 */
	public DLMinerInput(File ontologyFile) {
		setOntologyFile(ontologyFile);
        init();
	}


    /**
     * @param ontology ontology
     */
    public DLMinerInput(OWLOntology ontology) {
        setOntology(ontology);
        init();
    }
	
	


	private void init() {

	    // concept config
	    operatorConfig = new OperatorConfig();
        operatorConfig.logic = DLMinerInputI.DEF_LOGIC;
	    operatorConfig.maxDepth = DLMinerInputI.DEF_MAX_ROLE_DEPTH;
	    operatorConfig.minSupport = DLMinerInputI.DEF_MIN_CONCEPT_SUPPORT;
	    operatorConfig.beamSize = DLMinerInputI.DEF_BEAM_SIZE;
	    operatorConfig.maxLength = DLMinerInputI.DEF_MAX_CONCEPT_LENGTH;
	    operatorConfig.checkDisjointness = DLMinerInputI.DEF_USE_DISJOINTNESS;
	    operatorConfig.useDisjunction = DLMinerInputI.DEF_USE_DISJUNCTION;
	    operatorConfig.useUniversalRestriction = DLMinerInputI.DEF_USE_UNIVERSAL;
	    operatorConfig.useNegation = DLMinerInputI.DEF_USE_NEGATION;
	    operatorConfig.useDataProperties = DLMinerInputI.DEF_USE_DATA_PROPERTIES;
	    operatorConfig.dataThresholdsNumber = DLMinerInputI.DEF_DATA_THRESHOLDS_NUMBER;
	    operatorConfig.useObjectPropertySubsumptions = DLMinerInputI.DEF_USE_OBJECT_PROPERTY_SUBSUMPTIONS;
        operatorConfig.useInverseObjectProperties = DLMinerInputI.DEF_USE_INVERSE_OBJECT_PROPERTIES;
	    operatorConfig.useObjectPropertyChains = DLMinerInputI.DEF_USE_OBJECT_PROPERTY_CHAINS;
	    operatorConfig.checkRedundancy = DLMinerInputI.DEF_USE_REDUNDANCY;
        operatorConfig.useReasonerForAtomicClassInstances = DLMinerInputI.DEF_USE_REASONER_FOR_ATOMIC_CLASS_INSTANCES;
	    operatorConfig.useReasonerForClassInstances = DLMinerInputI.DEF_USE_REASONER_FOR_CLASS_INSTANCES;
	    operatorConfig.storeInstances = DLMinerInputI.DEF_STORE_INSTANCES;

	    // axiom config
	    axiomConfig = new AxiomConfig();
	    axiomConfig.dlminerMode = DLMinerInputI.DEF_DLMINER_MODE;
	    axiomConfig.minPrecision = DLMinerInputI.DEF_MIN_PRECISION;
	    axiomConfig.useMinSupport = DLMinerInputI.DEF_USE_MIN_SUPPORT;
	    axiomConfig.useMinPrecision = DLMinerInputI.DEF_USE_MIN_PRECISION;
	    axiomConfig.useConsistency = DLMinerInputI.DEF_USE_CONSISTENCY;
	    axiomConfig.useCleaning = DLMinerInputI.DEF_USE_CLEANING;
	    axiomConfig.axiomPattern = DLMinerInputI.DEF_AXIOM_PATTERN;


	    // other parameters
		maxHypothesesNumber = DLMinerInputI.DEF_MAX_HYPOTHESES_NUMBER;
		reasonerTimeout = DLMinerInputI.DEF_REASONER_TIMEOUT;
		hypothesisFormat = DLMinerInputI.DEF_HYPOTHESIS_FORMAT;
		reasonerName = DLMinerInputI.DEF_REASONER_NAME;
		useClosedWorldAssumption = DLMinerInputI.DEF_USE_CWA;
		useComplexMeasures = DLMinerInputI.DEF_USE_COMPLEX_MEASURES;

	}


}
