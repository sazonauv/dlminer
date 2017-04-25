package io.dlminer.print;

import io.dlminer.learn.Hypothesis;
import io.dlminer.ont.OntologyHandler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;


public class HypothesisWriter {
	
	private OWLDocumentFormat format;
	private IRI iri;
	
	
	public HypothesisWriter(OWLDocumentFormat format, IRI iri) {
		this.format = format;
		this.iri = iri;
	}
	
	
				
	public void saveHypothesesToSeparateFiles(Collection<Hypothesis> hypotheses, 
			File hypothesesDirectory) {		
		for (Hypothesis h : hypotheses) {					
			OntologyHandler handler = new OntologyHandler(h.axioms, iri);				
			File file = new File(hypothesesDirectory, h.id+".owl");
			handler.saveOntology(file, format);			
		}		
	}
	
	
	public void saveHypothesesToSingleFile(Collection<Hypothesis> hypotheses, OntologyHandler handler, 
			File hypothesesDirectory, int sampleSize) {		
		Out.p("\nSaving hypotheses");		
		// save to file top n hypotheses only		
		Set<OWLAxiom> axioms = new HashSet<>();
		int id = 0;		
		for (Hypothesis h : hypotheses) {
			id++;
			if (id > sampleSize) {
				break;
			}
			axioms.addAll(h.axioms);
		}		
		Out.p(axioms.size() + " axioms in total");
		handler.addAxioms(axioms);
		File file = new File(hypothesesDirectory, "terminology.owl");
		handler.saveOntology(file, format);
	}
	
		
	public static void setNumericIDs(Collection<Hypothesis> hypotheses) {
		int nDigits = Integer.toString(hypotheses.size()).length();
		int id = 0;
		for (Hypothesis h : hypotheses) {
			id++;
			String strId = Integer.toString(id);
			while (strId.length() < nDigits) {
				strId = "0" + strId;
			}
			h.id = strId;
		}
	}
	
	
	public static void setUniqueIDs(Collection<Hypothesis> hypotheses) {		
		for (Hypothesis h : hypotheses) {			
			h.id = UUID.randomUUID().toString();
		}
	}
	

}
