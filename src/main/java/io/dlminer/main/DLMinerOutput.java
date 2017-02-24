package io.dlminer.main;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;

import io.dlminer.learn.Hypothesis;
import io.dlminer.learn.HypothesisEvaluator;

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


	
	
		
}
