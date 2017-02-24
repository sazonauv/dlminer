package io.dlminer.learn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerLoader;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.Out;

public class EquivalentHypothesesTest {

	public static void main(String[] args) {	
		// read files
		Out.p("Reading files");
		File dir = new File(args[0]);		
		List<Set<OWLAxiom>> hypotheses = new ArrayList<>();		
		int count = 0;
		for (File file : dir.listFiles()) {
			OntologyHandler handler = new OntologyHandler(file);
			Set<OWLAxiom> hypothesis = handler.getAxioms();
			hypotheses.add(hypothesis);
			// debug
			count++;
			if (count % 100 == 0) {
				Out.p(count + " / " + dir.listFiles().length + " files are read");
			}
		}
		// load TBox
		Out.p("Loading TBox");	
		OntologyHandler handler = OntologyHandler.extractBotDataModule(args[1]);
		Set<OWLAxiom> aboxAxioms = handler.getABoxAxioms();
		handler.removeAxioms(aboxAxioms);
		// load two reasoners
		OntologyHandler handler1 = new OntologyHandler(handler.getAxioms());		
		OWLReasoner reasoner1 = null;		
		OntologyHandler handler2 = new OntologyHandler(handler.getAxioms());		
		OWLReasoner reasoner2 = null;		
		try {
			// Hermit is required here because Pellet is not updated 
			// if axioms are added to an empty ontology
			reasoner1 = ReasonerLoader.initReasoner(ReasonerName.HERMIT, handler1.getOntology());
			reasoner1.precomputeInferences(InferenceType.CLASS_HIERARCHY, 
					InferenceType.OBJECT_PROPERTY_HIERARCHY);		
			reasoner2 = ReasonerLoader.initReasoner(ReasonerName.HERMIT, handler2.getOntology());
			reasoner2.precomputeInferences(InferenceType.CLASS_HIERARCHY, 
					InferenceType.OBJECT_PROPERTY_HIERARCHY);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		// check how many pairs are equivalent
		Out.p("Checking equivalence");
		count = 0;
		for (int i=0; i<hypotheses.size(); i++) {
			Set<OWLAxiom> h1 = hypotheses.get(i);
			handler1.addAxioms(h1);
			reasoner1.flush();
			for (int j=i+1; j<hypotheses.size(); j++) {
				Set<OWLAxiom> h2 = hypotheses.get(j);
				if (reasoner1.isEntailed(h2)) {
					handler2.addAxioms(h2);
					reasoner2.flush();
					if (reasoner2.isEntailed(h1)) {
						count++;
					}
					handler2.removeAxioms(h2);
				}
			}
			handler1.removeAxioms(h1);
			// debug
			if (i % 1 == 0) {
				Out.p(i*hypotheses.size() + " pairs tested: " + count + " equivalent pairs found");
			}
		}
	}

}
