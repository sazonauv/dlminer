package io.dlminer.ont;

import io.dlminer.print.Out;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

public class MetricTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			File file = new File(args[0]);
			OntologyHandler handler = new OntologyHandler(file);
			IRI iri = handler.getIRI();
			Set<OWLAxiom> axioms = handler.getTBoxAxioms();
			for (OWLAxiom axiom : axioms) {
				Out.printAxiom(axiom, iri.toString());
				Out.p("\t length = " + LengthMetric.length(axiom));
				Out.p("\t depth = " + DepthMetric.depth(axiom));
			}						
		}		
	}

}
