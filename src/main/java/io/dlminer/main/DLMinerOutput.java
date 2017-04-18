package io.dlminer.main;

import java.util.Collection;

import io.dlminer.learn.AxiomBuilder;
import io.dlminer.learn.ConceptBuilder;
import io.dlminer.ont.OntologyHandler;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;

import io.dlminer.learn.Hypothesis;
import io.dlminer.learn.HypothesisEvaluator;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author Slava Sazonau
 * The University of Manchester
 * Information Management Group
 * 
 * The class encapsulates results of the DL-Miner algorithm.
 */
public class DLMinerOutput {
	
	// ====================== attributes ====================== 
	
		
	/**
	 * the result hypotheses produced by DL-Miner
	 */
	private Collection<Hypothesis> hypotheses;	
	
	
	
	/**
	 * the input ontology
	 */
	private OWLOntology ontology;
	
	
	
	
	/**
	 * the format to store hypotheses, e.g. OWL/XML, Turtle
	 */
	private OWLOntologyFormat hypothesisFormat;
	
	
	
	/**
     * additional evaluation of hypotheses
     */
    private HypothesisEvaluator evaluator;



    private OntologyHandler handler;

    private OWLReasoner reasoner;

    private ConceptBuilder conceptBuilder;

    private AxiomBuilder axiomBuilder;
    


	
	// ====================== 	getters and setters ====================== 
	
	
	/**
	 * @return the hypotheses
	 */
	public Collection<Hypothesis> getHypotheses() {
		return hypotheses;
	}
	
	
	public void setHypotheses(Collection<Hypothesis> hypotheses) {
		this.hypotheses = hypotheses;
	}


	
	/**
	 * @return the input ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}


	/**
	 * @param ontology the input ontology
	 */
	public void setOntology(OWLOntology ontology) {
		this.ontology = ontology;
	}
	

	/**
	 * @return the hypothesisFormat
	 */
	public OWLOntologyFormat getHypothesisFormat() {
		return hypothesisFormat;
	}


	/**
	 * @param hypothesisFormat the hypothesisFormat to set
	 */
	public void setHypothesisFormat(OWLOntologyFormat hypothesisFormat) {
		this.hypothesisFormat = hypothesisFormat;
	}
	
	
	
	public HypothesisEvaluator getEvaluator() {
		return evaluator;
	}


	public void setEvaluator(HypothesisEvaluator evaluator) {
		this.evaluator = evaluator;
	}


    public OntologyHandler getHandler() {
        return handler;
    }

    public void setHandler(OntologyHandler handler) {
        this.handler = handler;
    }

    public OWLReasoner getReasoner() {
        return reasoner;
    }

    public void setReasoner(OWLReasoner reasoner) {
        this.reasoner = reasoner;
    }

    public ConceptBuilder getConceptBuilder() {
        return conceptBuilder;
    }

    public void setConceptBuilder(ConceptBuilder conceptBuilder) {
        this.conceptBuilder = conceptBuilder;
    }

    public AxiomBuilder getAxiomBuilder() {
        return axiomBuilder;
    }

    public void setAxiomBuilder(AxiomBuilder axiomBuilder) {
        this.axiomBuilder = axiomBuilder;
    }
}
