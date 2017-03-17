package io.dlminer.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.dlminer.refine.OperatorConfig;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import io.dlminer.learn.AxiomBuilder;
import io.dlminer.learn.ConceptBuilder;
import io.dlminer.learn.Hypothesis;
import io.dlminer.learn.HypothesisCleaner;
import io.dlminer.learn.HypothesisEntry;
import io.dlminer.learn.HypothesisEvaluator;
import io.dlminer.ont.AxiomMetric;
import io.dlminer.ont.Logic;
import io.dlminer.ont.OntologyFormat;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerLoader;
import io.dlminer.print.CSVWriter;
import io.dlminer.print.HypothesisWriter;
import io.dlminer.print.Out;
import io.dlminer.sort.HypothesisSorter;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;


/**
 * @author Slava Sazonau
 *         The University of Manchester
 *         Information Management Group
 *         <p>
 *         The class encapsulates main functions of the DL-Miner algorithm.
 */
public class DLMiner {

    // ====================== attributes ======================

    /**
     * Input parameters for DL-Miner including an ontology file.
     */
    private DLMinerInput input;


    /**
     * Output of DL-Miner.
     */
    private DLMinerOutput output;
    
    
    /**
     * Statistics of a DL-Miner run, 
     * e.g. number of hypotheses, performance, etc.
     */
    private DLMinerStats stats;
    
    
    
    // ====================== 	getters and setters ======================

    /**
     * @return the input
     */
    public DLMinerInput getInput() {
        return input;
    }


    /**
     * @return the output
     */
    public DLMinerOutput getOutput() {
        return output;
    }
    
    
    /**
     * @return the output
     */
    public DLMinerStats getStats() {
        return stats;
    }


    // ====================== constructors ======================

    /**
     * @param input Input parameters for DL-Miner
     */
    public DLMiner(DLMinerInput input) {
        this.input = input;
    }


    // ====================== methods ======================

    /**
     * The method checks input parameters and throws a named error if they are incorrect.
     *
     * @throws Exception a named error thrown if parameters are incorrect.
     */
    public void verifyParameters() throws Exception {

        // checks null values
        if (input.getMaxRoleDepth() == null) {
            throw new DLMinerException(DLMinerInputI.NULL_MAX_ROLE_DEPTH_ERR);
        }
        if (input.getMaxHypothesesNumber() == null) {
            throw new DLMinerException(DLMinerInputI.NULL_MAX_HYPOTHESES_NUMBER_ERR);
        }
        if (input.getReasonerTimeout() == null) {
            throw new DLMinerException(DLMinerInputI.NULL_REASONER_TIMEOUT_ERR);
        }
        if (input.getMinPrecision() == null) {
            throw new DLMinerException(DLMinerInputI.NULL_MIN_PRECISION_ERR);
        }
        if (input.getMinConceptSupport() == null) {
            throw new DLMinerException(DLMinerInputI.NULL_MIN_CONCEPT_SUPPORT_ERR);
        }

        // check wrong values
        if (input.getMaxRoleDepth() < 0) {
            throw new DLMinerException(DLMinerInputI.WRONG_MAX_ROLE_DEPTH_ERR);
        }
        if (input.getMaxHypothesesNumber() <= 0) {
            throw new DLMinerException(DLMinerInputI.WRONG_MAX_HYPOTHESES_NUMBER_ERR);
        }
        if (input.getReasonerTimeout() <= 0) {
            throw new DLMinerException(DLMinerInputI.WRONG_REASONER_TIMEOUT_ERR);
        }
        if (input.getMinPrecision() <= 0) {
            throw new DLMinerException(DLMinerInputI.WRONG_MIN_PRECISION_ERR);
        }
        if (input.getMinConceptSupport() <= 0) {
            throw new DLMinerException(DLMinerInputI.WRONG_MIN_CONCEPT_SUPPORT_ERR);
        }

    }


    /**
     * The method runs the DL-Miner algorithm.
     * Once DL-Miner terminates, the results (mined hypotheses and run statistics)
     * are stored in the attribute <code>output</code>.
     *
     * @throws Exception throws an error of the following types:
     *                   1) incorrect input parameter value
     *                   2) ontology parsing error
     *                   3) inconsistent ontology
     *                   4) reasoner timeout exceeded
     *                   5) empty output
     *                   6) other
     */
    public void run() throws Exception {

        // check the parameters
        verifyParameters();
        
        // init stats
        stats = new DLMinerStats();

        // parse the ontology file
        long start = System.currentTimeMillis();        
        OntologyHandler handler = null;
        if (input.isUseClosedWorldAssumption()) {
        	handler = new OntologyHandler(input.getOntologyFile());        	
        } else {
        	handler = OntologyHandler.extractBotDataModule(input.getOntologyFile());
        }
        
        Out.p("\nOntology size:");
        Out.p("\tTBox size = " + handler.getTBoxAxioms().size());
        Out.p("\tRBox size = " + handler.getRBoxAxioms().size());
        Out.p("\tABox size = " + handler.getABoxAxioms().size());
        Out.p("\tnumber of classes = " + handler.getClassesInSignature().size());
        Out.p("\tnumber of object properties = " + handler.getObjectPropertiesInSignature().size());
        Out.p("\tnumber of data properties = " + handler.getDataPropertiesInSignature().size());
        Out.p("\tnumber of individuals = " + handler.getIndividuals().size());
        long end = System.currentTimeMillis();
        double ontologyParsingTime = (double) (end - start) / 1e3;
        stats.setOntologyParsingTime(ontologyParsingTime);
        Out.p("Ontology parsing time = " + Out.fn(ontologyParsingTime) + " seconds");
               
        // process the ontology by the reasoner
        Out.p("\nInitialising the reasoner");    
        start = System.currentTimeMillis();        
        OWLReasoner reasoner = ReasonerLoader.initReasoner(
        		input.getReasonerName(),
        		handler.getOntology(),
                input.getReasonerTimeout());
        
        // check if the ontology is consistent
        if (!reasoner.isConsistent()) {
        	Out.p("\nThe ontology is inconsistent!");
        	handler.removeInconsistency(reasoner);
        	reasoner.flush();
        }        
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
        		InferenceType.OBJECT_PROPERTY_HIERARCHY,
                InferenceType.DATA_PROPERTY_HIERARCHY);
        handler.removeUnsatClasses(reasoner);
        handler.removeTautologies(reasoner);
        reasoner.flush();        
        end = System.currentTimeMillis();
        double ontologyReasoningTime = (double) (end - start) / 1e3;
        stats.setOntologyReasoningTime(ontologyReasoningTime);
        Out.p("Ontology reasoning time = " + Out.fn(ontologyReasoningTime) + " seconds");
       
        if (input.isUseClosedWorldAssumption()) {
        	handler.applyCWA(reasoner);
        	reasoner.flush();
        	if (!reasoner.isConsistent()) {
            	Out.p("\nThe ontology is inconsistent!");
            	handler.removeInconsistency(reasoner);
            	reasoner.flush();
            }
        }
        
        
        
        
        Out.p("\nInitialising the concept builder");
        // config
        OperatorConfig config = new OperatorConfig();
        config.maxLength = input.getMaxConceptLength();
        config.maxDepth = input.getMaxRoleDepth();
        config.checkDisjointness = input.isUseDisjointness();
        config.useDisjunction = input.isUseDisjunction();
        config.useNegation = input.isUseNegation();
        config.useUniversalRestriction = input.isUseUniversalRestriction();
        config.useDataProperties = input.isUseDataProperties();
        // check disjunctions
        if (input.getLogic().equals(Logic.EL)) {
            config.useDisjunction = false;
        }
        // check negations
        if (input.getLogic().equals(Logic.EL) || !handler.containsNegations()) {
            config.useNegation = false;
        }
        // check universals
        if (input.getLogic().equals(Logic.EL) ||
                (!handler.containsUniversals() && !handler.containsMaxRestrictions())) {
            config.useUniversalRestriction = false;
        }
        // check data properties
        if (!handler.containsDataProperties()) {
            config.useDataProperties = false;
        }

        // builder
        ConceptBuilder conceptBuilder = new ConceptBuilder(handler, reasoner,
                input.getMinConceptSupport(), input.getBeamSize(), config);
        conceptBuilder.init();
        
        // if prediction
        if (input.getPositiveClass() != null) {
        	conceptBuilder.setPositiveClass(input.getPositiveClass());        	
        }
        if (input.getNegativeClass() != null) {
        	conceptBuilder.setNegativeClass(input.getNegativeClass());        	
        }
        

        Out.p("\nInitialising the axiom builder");
        // setting the seed signature
        Set<OWLClass> seedClasses = null;
        if (input.getSeedClassName() != null) {
        	OWLClass seedClass = handler.getClassByIRI(input.getSeedClassName());
        	if (seedClass != null) {
        		seedClasses = new HashSet<>();
        		seedClasses.addAll(reasoner.getEquivalentClasses(seedClass).getEntities());
        		seedClasses.addAll(reasoner.getSubClasses(seedClass, false).getFlattened());
        		OWLDataFactory factory = handler.getDataFactory();
        		seedClasses.remove(factory.getOWLThing());
        		seedClasses.remove(factory.getOWLNothing());
        	}
        	Out.p("\n" + seedClasses.size() + " seed classes are selected");
        }
        AxiomBuilder axiomBuilder = new AxiomBuilder(conceptBuilder,
    			input.getMinConceptSupport(), input.getMinPrecision(), 
    			input.isUseMinSupport(), input.isUseMinPrecision(), 
    			input.getDlminerMode(), seedClasses);
       
        // build hypotheses
        Collection<Hypothesis> hypotheses = buildHypotheses(
        		reasoner, conceptBuilder, axiomBuilder);

        // initialise the evaluator
        Out.p("\nInitialising the evaluator");
        HypothesisEvaluator evaluator = new HypothesisEvaluator(
        		handler, reasoner, conceptBuilder,
        		input.isUseConsistency());
        
                                
        // create the output
        output = new DLMinerOutput();
        output.setHypotheses(hypotheses);
        output.setOntology(handler.getOntology());
        output.setEvaluator(evaluator);        
        stats.setConceptsNumber(conceptBuilder.getClassInstanceMap().size());
        stats.setRolesNumber(conceptBuilder.getRoleInstanceMap().size());
        stats.setHypothesesNumber(hypotheses.size());
        OWLOntologyFormat hypothesisFormat = null;
        if (input.getHypothesisFormat().equals(OntologyFormat.OWLXML)) {
        	hypothesisFormat = new OWLXMLOntologyFormat();
    	} else if (input.getHypothesisFormat().equals(OntologyFormat.TURTLE)) {
    		hypothesisFormat = new TurtleOntologyFormat();
    	}
        output.setHypothesisFormat(hypothesisFormat);
        if (!hypotheses.isEmpty()) {
            // average values
        	stats.setAverageSupport(HypothesisEvaluator.calculateAverageSupport(hypotheses));
        	stats.setAverageAssumption(HypothesisEvaluator.calculateAverageAssumption(hypotheses));
        	stats.setAverageLength(HypothesisEvaluator.calculateAverageLength(hypotheses));
        	stats.setAverageNovelty(HypothesisEvaluator.calculateAverageNovelty(hypotheses));
            // min values
        	stats.setMinSupport(HypothesisEvaluator.calculateMinSupport(hypotheses));
        	stats.setMinAssumption(HypothesisEvaluator.calculateMinAssumption(hypotheses));
        	stats.setMinLength(HypothesisEvaluator.calculateMinLength(hypotheses));
        	stats.setMinNovelty(HypothesisEvaluator.calculateMinNovelty(hypotheses));
            // max values
        	stats.setMaxSupport(HypothesisEvaluator.calculateMaxSupport(hypotheses));
        	stats.setMaxAssumption(HypothesisEvaluator.calculateMaxAssumption(hypotheses));
        	stats.setMaxLength(HypothesisEvaluator.calculateMaxLength(hypotheses));
        	stats.setMaxNovelty(HypothesisEvaluator.calculateMaxNovelty(hypotheses));
        } else {
            throw new DLMinerException(DLMinerOutputI.EMPTY_OUTPUT_ERROR);
        }        
    }
    
    
    
    
    private Collection<Hypothesis> buildHypotheses(OWLReasoner reasoner,
                                                   ConceptBuilder conceptBuilder,
                                                   AxiomBuilder axiomBuilder) {
        // initialise parameters
        Set<Hypothesis> hypotheses = new HashSet<>();
        double roleBuildingTime = 0;
        double conceptBuildingTime = 0;
        double hypothesesBuildingTime = 0;
        double hypothesesCleaningTime = 0;
        int maxConceptNumber = input.getMaxHypothesesNumber();
        
        long start = System.currentTimeMillis();
        if (!input.getDlminerMode().equals(DLMinerMode.CDL)) {
        	// build roles
        	Out.p("\nBuilding roles");        
        	conceptBuilder.buildRoles();        

        	// find role instances
        	Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleInstMap =
        			conceptBuilder.getRoleInstanceMap();        	
        	Out.p(roleInstMap.size() + " roles are built");

        	// build role axioms
        	Set<Hypothesis> roleAxioms = axiomBuilder.generateInitialRoleAxioms();
        	Out.p(roleAxioms.size() + " role axioms are built");

        	Out.p("\nAppending hypotheses");
        	hypotheses.addAll(roleAxioms);
        }
        long end = System.currentTimeMillis();
        double time = (double)(end - start) / 1e3;
        roleBuildingTime += time;
        hypothesesBuildingTime += time;
        
    	// build concepts
    	Out.p("\nBuilding at most " + maxConceptNumber 
    			+ " concepts of length at most " + input.getMaxConceptLength());        	
    	start = System.currentTimeMillis();
    	conceptBuilder.buildConcepts(maxConceptNumber);
    	end = System.currentTimeMillis();
    	conceptBuildingTime += (double)(end - start) / 1e3;
    	hypothesesBuildingTime += (double)(end - start) / 1e3;

    	// find concept instances
    	Map<OWLClass, Set<OWLNamedIndividual>> classInstMap =
    			conceptBuilder.getClassInstanceMap();       	
    	Out.p("\n" + classInstMap.size() + " concepts are built");

        // debug
//        Out.printClassesMS(conceptBuilder.getExpressionClassMap().keySet());

    	// build hypotheses
    	Out.p("\nBuilding hypotheses of length at most " + 2*input.getMaxConceptLength());
    	start = System.currentTimeMillis();        	
    	Set<Hypothesis> classAxioms = axiomBuilder.generateInitialClassAxioms();
    	end = System.currentTimeMillis();
    	hypothesesBuildingTime += (double)(end - start) / 1e3;
    	
    	   	
    	// filter out simple redundancies
    	Out.p("\nCleaning hypotheses");
    	start = System.currentTimeMillis();
    	Set<Hypothesis> cleanClassAxioms = classAxioms;        
    	if (input.isUseCleaning()) {
    		HypothesisCleaner cleaner = new HypothesisCleaner(
    				conceptBuilder, classAxioms, reasoner);
    		cleanClassAxioms = cleaner.cleanSeparately();
    	}
    	end = System.currentTimeMillis();
    	hypothesesCleaningTime += (double)(end - start) / 1e3;
    	Out.p(cleanClassAxioms.size() + " class axioms are built");
    	 	
    	    	
    	// add all hypotheses
    	Out.p("\nAppending hypotheses");
    	hypotheses.addAll(cleanClassAxioms);
    	Out.p(hypotheses.size() + " axioms are built in total");
        
        // remove unnecessary concepts
        conceptBuilder.retainClassDefinitions(hypotheses);
        
        // dispose the internal reasoner
        axiomBuilder.dispose();

        Out.p("\nConcept building time = " + Out.fn(conceptBuildingTime) + " seconds");
        Out.p("\nRole building time = " + Out.fn(roleBuildingTime) + " seconds");
        Out.p("\nHypotheses building time = " + Out.fn(hypothesesBuildingTime) + " seconds");
        Out.p("\nHypotheses cleaning time = " + Out.fn(hypothesesCleaningTime) + " seconds");

        // stats        
        stats.setConceptBuildingTime(conceptBuildingTime);
        stats.setRoleBuildingTime(roleBuildingTime);
        stats.setHypothesesBuildingTime(hypothesesBuildingTime);
        stats.setHypothesesCleaningTime(hypothesesCleaningTime);
           
        // sort by length
        List<Hypothesis> sortedHypotheses = HypothesisSorter.sortByLength(hypotheses);
        // add quality values
        addQualityValues(sortedHypotheses);
        return sortedHypotheses;
    }
    
    
    
    
    
    private void appendRandom(Set<Hypothesis> oldHypotheses, 
    		Set<Hypothesis> newHypotheses) {
    	int sampleNumber = input.getMaxHypothesesNumber() - oldHypotheses.size();
    	Collection<Hypothesis> randomHypotheses = HypothesisEvaluator.sampleHypotheses(
    			newHypotheses, sampleNumber);    	
    	Out.p("\nAppending " + randomHypotheses.size() + " random hypotheses");
    	append(oldHypotheses, randomHypotheses);
	}
    
    
    
    
    
    private void appendGood(Set<Hypothesis> oldHypotheses, 
    		Set<Hypothesis> newHypotheses) {
    	Set<Hypothesis> goodHypotheses = new HashSet<>();
    	int half = input.getMaxHypothesesNumber()/2;
    	for (Hypothesis h : newHypotheses) {
    		if (h.precision >= input.getMinPrecision()) {
    			goodHypotheses.add(h);
    		}
    		if (goodHypotheses.size() >= half) {
    			break;
    		}
    	}
    	Out.p("\nAppending " + goodHypotheses.size() + " good hypotheses");
    	append(oldHypotheses, goodHypotheses);
	}
    
    
    
    private void appendBad(Set<Hypothesis> oldHypotheses, 
    		Set<Hypothesis> newHypotheses) {
    	Set<Hypothesis> badHypotheses = new HashSet<>();
    	int half = input.getMaxHypothesesNumber()/2;
    	for (Hypothesis h : newHypotheses) {
    		if (h.precision < input.getMinPrecision()) {
    			badHypotheses.add(h);
    		}
    		if (badHypotheses.size() >= half) {
    			break;
    		}
    	}
    	Out.p("\nAppending " + badHypotheses.size() + " bad hypotheses");
    	append(oldHypotheses, badHypotheses);
	}


	

    
    private void append(Collection<Hypothesis> oldHypotheses, 
    		Collection<Hypothesis> newHypotheses) {
    	if (oldHypotheses.size() >= input.getMaxHypothesesNumber()) {
    		return;
    	}
    	if (oldHypotheses.size() + newHypotheses.size() <= input.getMaxHypothesesNumber()) {
    		oldHypotheses.addAll(newHypotheses);
    	} else {
    		int initialSize = oldHypotheses.size();
    		List<Hypothesis> sortedNewHypotheses = 
    				HypothesisSorter.sortByLength(newHypotheses);
    		// first add EL hypotheses
    		for (Hypothesis h : sortedNewHypotheses) {
    			if (AxiomMetric.isEL(h.axioms)) {
    				oldHypotheses.add(h);
    			}
    			if (oldHypotheses.size() >= input.getMaxHypothesesNumber()) {
    				break;
    			}
    		}
    		Out.p("\t" + (oldHypotheses.size() - initialSize) + " EL hypotheses are added");
    		// then as many others as the threshold permits
    		initialSize = oldHypotheses.size();
    		for (Hypothesis h : sortedNewHypotheses) {    			
    			if (!AxiomMetric.isEL(h.axioms)) {
    				oldHypotheses.add(h);
    			}
    			if (oldHypotheses.size() >= input.getMaxHypothesesNumber()) {
    				break;
    			}
    		}
    		Out.p("\t" + (oldHypotheses.size() - initialSize) + " ALC hypotheses are added");
    	}
    }


       
       
    /**
     * @param hypotheses the hypotheses to include
     * @param number the number of top hypotheses to include
     * @param iri IRI to set for hypotheses
     * @return the wrapper containing the result hypotheses as an ontology
     */
    private OntologyHandler getHypothesesAsOntology(Collection<Hypothesis> hypotheses, 
    		int number, IRI iri) {    	
        // create an output ontology and add hypotheses        
        OntologyHandler hypothesesHandler = new OntologyHandler(iri);
        // add all annotations
        hypothesesHandler.addAnnotations(output.getOntology());
        // return only the specified number of hypotheses
        int count = 0;
        for (Hypothesis hypothesis : hypotheses) {
            hypothesesHandler.addAxioms(hypothesis.axioms);
            count++;
            if (count >= number) {
                break;
            }
        }        
        return hypothesesHandler;
	}
    
    
    
    /**
     * @param file the file to store the output hypotheses
     * @param number the number of the hypotheses to store
     */
    public void saveHypotheses(File file, int number) {
    	IRI iri = output.getOntology().getOntologyID().getOntologyIRI();
    	saveHypotheses(output.getHypotheses(), file, iri, number);
    }
    
    
    
    /**
     * @param file the file to store the output hypotheses
     */
    public void saveHypotheses(File file) {   
    	saveHypotheses(file, input.getMaxHypothesesNumber());
	}
    
    
    
    /**
     * @param file the file to store the output hypotheses
     * @param iri the IRI to assign hypotheses
     * @param number the number of the hypotheses to store
     */
    public void saveHypotheses(File file, IRI iri, int number) {
    	saveHypotheses(output.getHypotheses(), file, iri, number);
    }
    
    
    
    /**
     * @param file the file to store the output hypotheses
     * @param iri the IRI to assign hypotheses
     */
    public void saveHypotheses(File file, IRI iri) {   
    	saveHypotheses(output.getHypotheses(), file, iri);
	}


    	
	/**
	 * @param hypotheses the hypotheses to store
	 * @param file the file to store the hypotheses
	 * @param iri the IRI to assign the hypotheses
	 */
	public void saveHypotheses(Collection<Hypothesis> hypotheses, File file, IRI iri) {
		saveHypotheses(hypotheses, file, iri, input.getMaxHypothesesNumber());
	}
	
	
	
	/**
	 * @param hypotheses the hypotheses to store
	 * @param file the file to store the hypotheses
	 * @param iri the IRI to assign the hypotheses
	 * @param number the number of hypotheses (from the beginning) to store
	 */
	public void saveHypotheses(Collection<Hypothesis> hypotheses, File file, IRI iri, int number) {
		OntologyHandler handler = getHypothesesAsOntology(hypotheses, number, iri);
		handler.saveOntology(file, output.getHypothesisFormat());
	}

	
	
		
	/**
     * @return the mapping of hypothesis IDs to data entries:
     * fitness, braveness, interest, length, entities, file.
     * Files are temporary, so they should be deleted once not needed.
     */
    public Map<String, HypothesisEntry> getHypothesesData() {
        OWLOntologyFormat format = new OWLFunctionalSyntaxOntologyFormat();     
        HypothesisSorter sorter = new HypothesisSorter(output.getHypotheses());
        sorter.indexByEntities();
        Map<Hypothesis, Set<String>> hypothesisToEntitiesMap = sorter.getHypothesisToEntitiesMap();
        Map<String, HypothesisEntry> hypothesisIdToEntryMap = new HashMap<>();
        Out.p("\nHypotheses:");
        for (Hypothesis hypothesis : hypothesisToEntitiesMap.keySet()) {
            File file = null;
            try {
                file = File.createTempFile(hypothesis.id, ".owl");
            } catch (IOException e) {
                e.printStackTrace();
            }
            IRI iri = output.getOntology().getOntologyID().getOntologyIRI();
            OntologyHandler handler = new OntologyHandler(hypothesis.axioms, iri);
            handler.saveOntology(file, format);
            HypothesisEntry entry = new HypothesisEntry(
                    hypothesis.support.intValue(),
                    hypothesis.assumption.intValue(),
                    hypothesis.noveltyApprox.intValue(),
                    hypothesis.length,
                    hypothesisToEntitiesMap.get(hypothesis),
                    file);
            hypothesisIdToEntryMap.put(hypothesis.id, entry);
//            Out.printHypothesisFile(file);
            break;
        }
        return hypothesisIdToEntryMap;
    }


    
		
	
	



	public void saveHypothesesAsSeparateFiles(File hypothesesPath) {
		IRI iri = output.getOntology().getOntologyID().getOntologyIRI();
		saveHypothesesAsSeparateFiles(hypothesesPath, iri);		
	}
	
	
	
	public void saveHypothesesAsSeparateFiles(File hypothesesPath, IRI iri) {
		HypothesisWriter hypoWriter = new HypothesisWriter(output.getHypothesisFormat(), iri);
		hypoWriter.saveHypothesesToSeparateFiles(output.getHypotheses(), hypothesesPath);
	}
	



	public static void addQualityValues(Collection<Hypothesis> hypotheses) {
		IRI iri = IRI.create("http://www.dlminer.io");
		OWLDataFactory factory = new OWLDataFactoryImpl();
		for (Hypothesis h : hypotheses) {	
			Set<OWLAnnotation> annots = new HashSet<>();
			// ids
//			if (h.id != null) {
//				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
//						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "_id"));
//				OWLLiteral value = factory.getOWLLiteral(h.id);
//				OWLAnnotation annot =
//						factory.getOWLAnnotation(annProp, value);
//				annots.add(annot);
//			}
			// logical measures			
			if (h.novelty != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "novelty"));
				OWLLiteral value = factory.getOWLLiteral(h.novelty);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}			
			if (h.dissimilarity != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "dissimilarity"));
				OWLLiteral value = factory.getOWLLiteral(h.dissimilarity);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			// statistical measures
			if (h.assumption != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "assumption"));
				OWLLiteral value = factory.getOWLLiteral(h.assumption);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			if (h.braveness != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "braveness"));
				OWLLiteral value = factory.getOWLLiteral(h.braveness);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			if (h.fitness != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "fitness"));
				OWLLiteral value = factory.getOWLLiteral(h.fitness);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			if (h.support != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "support"));
				OWLLiteral value = factory.getOWLLiteral(h.support);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			if (h.mainContradiction != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "contradiction"));
				OWLLiteral value = factory.getOWLLiteral(h.mainContradiction);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			if (h.precision != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "precision"));
				OWLLiteral value = factory.getOWLLiteral(h.precision);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			if (h.recall != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "recall"));
				OWLLiteral value = factory.getOWLLiteral(h.recall);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			if (h.lift != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "lift"));
				OWLLiteral value = factory.getOWLLiteral(h.lift);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			if (h.convictionNeg != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "conviction_neg"));
				OWLLiteral value = factory.getOWLLiteral(h.convictionNeg);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			if (h.convictionQue != null) {
				OWLAnnotationProperty annProp = factory.getOWLAnnotationProperty(
						IRI.create(iri.toString() + Out.IRI_SEPARATOR + "conviction_que"));
				OWLLiteral value = factory.getOWLLiteral(h.convictionQue);
				OWLAnnotation annot = 							
						factory.getOWLAnnotation(annProp, value);
				annots.add(annot);
			}
			// add annotations
			Set<OWLAxiom> annotatedAxioms = new HashSet<>();
			for (OWLAxiom ax : h.axioms) {
				OWLAxiom annAx = ax.getAnnotatedAxiom(annots);
				annotatedAxioms.add(annAx);
			}
			h.axioms = annotatedAxioms;
		}		
	}

	
	
	/**
	 * Disposes the reasoners, i.e. unleashes the consumed resources
	 */
	public void disposeReasoners() {				
		output.getEvaluator().dispose();
	}


	public Collection<Hypothesis> buildMultiHypotheses(
			Integer axiomNumber, Integer sampleSize) {
		Out.p("\nBuilding hypotheses with at most " + axiomNumber + " axioms");
		Collection<Hypothesis> sampleHypotheses = HypothesisEvaluator.sampleHypotheses(
				output.getHypotheses(), sampleSize);
		List<Hypothesis> hypotheses = new ArrayList<>(sampleHypotheses);
		if (axiomNumber <= 1) {
			return hypotheses;
		}
		Set<Hypothesis> multiHypotheses = new HashSet<>(hypotheses);
		List<Hypothesis> maxHypotheses = new ArrayList<>(hypotheses);
		for (int i=2; i<=axiomNumber; i++) {
			Set<Hypothesis> newHypotheses = new HashSet<>();
			while (!maxHypotheses.isEmpty()) {
				int index1 = (int)(Math.random()*maxHypotheses.size());
				Hypothesis mh = maxHypotheses.get(index1);
				maxHypotheses.remove(mh);
				Hypothesis nh = new Hypothesis(mh);
				while (nh.axioms.size() < i) {
					int index2 = (int)(Math.random()*hypotheses.size());
					Hypothesis h = hypotheses.get(index2);
					nh.add(h);
					if (newHypotheses.contains(nh)) {
						// repeat
						nh = new Hypothesis(mh);
					}
				}
				newHypotheses.add(nh);
			}			
			multiHypotheses.addAll(newHypotheses);
			maxHypotheses = new ArrayList<>(newHypotheses);			
		}
		// sort by length
		List<Hypothesis> sortedHypotheses = HypothesisSorter.sortByLength(multiHypotheses);
		Out.p(sortedHypotheses.size() + " hypotheses are built from " 
				+ hypotheses.size() + " axioms");
		return sortedHypotheses;
	}
	


	public void evaluateStrength(Collection<Hypothesis> hypotheses) {
		Out.p("\nEvaluating strength");
		HypothesisSorter sorter = new HypothesisSorter(hypotheses);
		double start = System.currentTimeMillis();
		sorter.orderByStrength();
		double time = (System.currentTimeMillis() - start)/1e3;
		stats.setStrengthTime(time);
		sorter.setStrengthRanks();
	}

	
	
	

	public Set<Hypothesis> evaluateAxioms(Set<OWLAxiom> axioms) {				
		// compute basic measures
		HypothesisEvaluator evaluator = output.getEvaluator();		
		Set<Hypothesis> hypos = evaluator.getBasicMeasures(axioms, output.getOntology());
		// compute complex measures
//		evaluator.evaluateMainMeasures(hypos, stats);
//		evaluator.evaluateComplexMeasures(hypos, stats);		
		return hypos;
	}
	


		

}
