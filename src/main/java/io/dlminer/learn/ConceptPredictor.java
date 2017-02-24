package io.dlminer.learn;

import io.dlminer.ont.OntologyHandler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;


public class ConceptPredictor {
	
	private ConceptBuilder cbuilder;
	private Map<OWLClass, Set<OWLNamedIndividual>> posMap;
	private Set<OWLAxiom> rules;

	public ConceptPredictor(ConceptBuilder cbuilder,
							Map<OWLClass, Set<OWLNamedIndividual>> posMap, Hypothesis hypothesis) {
		this.cbuilder = cbuilder;
		this.posMap = posMap;
		init(hypothesis);
	}

	private void init(Hypothesis hypothesis) {
		rules = hypothesis.codedAxioms;		
	}

	public Set<OWLAxiom> getPredictions() {		
		Set<OWLAxiom> predictions = new HashSet<>();
		OWLDataFactory factory = cbuilder.getHandler().getDataFactory();
		for (OWLAxiom rule : rules) {
			OWLClass lhs = (OWLClass) OntologyHandler.getSubClass(rule);				
			OWLClass rhs = (OWLClass) OntologyHandler.getSuperClass(rule);			
			Set<OWLNamedIndividual> pos1 = posMap.get(lhs);
			Set<OWLNamedIndividual> pos2 = posMap.get(rhs);					
			Set<OWLNamedIndividual> bras = new HashSet<>();
			for (OWLNamedIndividual ind : pos1) {
				if (!pos2.contains(ind)) {
					bras.add(ind);
				}
			}
			if (!bras.isEmpty()) {
				OWLClassExpression expr = cbuilder.getExpressionByClass(rhs);
				if (expr.isClassExpressionLiteral()) {
					for (OWLNamedIndividual ind : bras) {
						OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(expr, ind);
						predictions.add(ax);
					}
				}
			}			
		}		
		return predictions;
	}
	
	

}
