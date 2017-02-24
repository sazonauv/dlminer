package io.dlminer.ont;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

//import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
//import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;
//import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
//import uk.ac.manchester.cs.jfact.JFactFactory;
//import uk.ac.ox.cs.pagoda.junit.PagodaDBPedia;
//import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;


public abstract class ReasonerLoader {
	
	public static final String INVALID_REASONER_ERR = "Valid reasoners: Hermit";
//			+ " | Pellet | Fact | JFact | Pellet | TrOWL ";
	
		
	/**
	 * Default reasoner
	 */
	public static final ReasonerName DEF_REASONER = ReasonerName.HERMIT;
	
	
	/**
	 * Create an OWL reasoner with a timeout in seconds
	 * @return OWL Reasoner
	 */
	public static OWLReasoner initReasoner(ReasonerName reasonerName, 
			OWLOntology ontology, long timeout) 
			throws Exception {
		// translate to milliseconds
		OWLReasonerConfiguration config = new SimpleConfiguration(timeout*1000);	
		return initReasonerConfigured(reasonerName, ontology, config);
	}
	
	
	/**
	 * Create an OWL reasoner
	 * @return OWL Reasoner
	 */
	public static OWLReasoner initReasoner(ReasonerName reasonerName, OWLOntology ontology) 
			throws Exception {		
		return initReasoner(reasonerName, ontology, Integer.MAX_VALUE);
	}
	
	
	
	/**
	 * Create a default OWL reasoner with a timeout in seconds
	 * @return OWL Reasoner
	 */
	public static OWLReasoner initReasoner(OWLOntology ontology, long timeout) 
			throws Exception {			
		return initReasoner(DEF_REASONER, ontology, timeout);
	}
	
	
	/**
	 * Create a default OWL reasoner
	 * @return OWL Reasoner
	 */
	public static OWLReasoner initReasoner(OWLOntology ontology) 
			throws Exception {		
		return initReasoner(DEF_REASONER, ontology);
	}
	
	
	
	private static OWLReasoner initReasonerConfigured(ReasonerName reasonerName, OWLOntology ontology, 
			OWLReasonerConfiguration config) 
			throws Exception {
		OWLReasonerFactory reasonerFactory = null;
		OWLReasoner reasoner = null;				

//		if (reasonerName.equals(ReasonerName.FACT)) {
//			reasonerFactory = new FaCTPlusPlusReasonerFactory();
//			reasoner = reasonerFactory.createReasoner(ontology, config);			
//		}		
//		else 
//		if (reasonerName.equals(ReasonerName.PELLET)) {
//			reasonerFactory = new PelletReasonerFactory();
//			reasoner = reasonerFactory.createReasoner(ontology, config);			
//		}
//		else 
		if (reasonerName.equals(ReasonerName.HERMIT)) {
			reasonerFactory = new Reasoner.ReasonerFactory();
			reasoner = reasonerFactory.createReasoner(ontology, config);			
		}		
//		else if (reasonerName.equals(ReasonerName.TROWL)) {
//			reasonerFactory = new RELReasonerFactory();
//			reasoner = reasonerFactory.createReasoner(ontology);			
//		}
//		else if (reasonerName.equals(ReasonerName.JFACT)) {
//			reasonerFactory = new JFactFactory();
//			reasoner = reasonerFactory.createReasoner(ontology, config);			
//		}
		else {
			throw new Exception("Unknown reasoner: " + reasonerName + ". " +
					INVALID_REASONER_ERR); 
		}		
		return reasoner;
	}
	
		
			
}

