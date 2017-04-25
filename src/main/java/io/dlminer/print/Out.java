package io.dlminer.print;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.OWLRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import io.dlminer.learn.Hypothesis;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.sort.SortingOrder;
import io.dlminer.sort.StringLengthComparator;



public abstract class Out {
	
	public static final String IRI_SEPARATOR = "#";
	public static final String CLASS_SUFFIX = "_class";
	public static final String IND_SUFFIX = "_ind";
	public static final String LEFT_ANGLE = "<";
	public static final String RIGHT_ANGLE = ">";
	public static final String SPACE_REPLACER = ".";
	
	public static void printArray(double[] arr) {
		for (int i=0; i<arr.length; i++) {
			p(arr[i] + "\t");
		}
		p("");
	}
	
	public static void printPareto(List<Hypothesis> pareto) {
		double[] means = new double[15];
		double[] devs = new double[means.length];
		int[] counts = new int[means.length];
		// get means
		for (Hypothesis hypo : pareto) {
			means[hypo.braveness.intValue()] += hypo.fitness;
			counts[hypo.braveness.intValue()]++;
		}
		// normalise means
		for (int i=0; i<means.length; i++) {
			if (counts[i] > 0) {
				means[i] /= counts[i];
			}
		}

		// get deviations
		for (Hypothesis hypo : pareto) {
			devs[hypo.braveness.intValue()] += 
					(hypo.fitness-means[hypo.braveness.intValue()])*(hypo.fitness-means[hypo.braveness.intValue()]);
		}
		// normalise deviations
		for (int i=0; i<means.length; i++) {
			if (counts[i] > 0) {
				devs[i] /= counts[i];
				devs[i] = Math.sqrt(devs[i]);
			}
		}
		// print means and deviations
		for (int i=0; i<means.length; i++) {
			if (counts[i] > 0) {
				Out.p(i + "\t" + means[i] + "\t" + devs[i]);
			}
		}
	}
	
	
	
	
	public static void print(Collection<OWLAxiom> axioms, String iri) {
		Out.p("");
		for (OWLAxiom ax : axioms) {
			printAxiom(ax, iri);
		}
		Out.p("");
	}
	
	public static void sortAndPrintAxioms(Set<OWLAxiom> axioms, Set<OWLClassExpression> cls, String iri) {
		
		Map<OWLClassExpression, Set<OWLAxiom>> axMap = 
				new HashMap<>();
		for (OWLAxiom ax : axioms) {			
			for (OWLClassExpression cl : cls) {
				if (getString(ax, iri).contains(getString(cl, iri))) {
					Set<OWLAxiom> clAxs = axMap.get(cl);
					if (clAxs != null) {
						clAxs.add(ax);
					} else {
						clAxs = new HashSet<>();
						clAxs.add(ax);
						axMap.put(cl, clAxs);
					}
				}
			}			
		}		
		for (OWLClassExpression cl : axMap.keySet()) {
			Out.p(getString(cl, iri) + ":");
			Set<OWLAxiom> clAxs = axMap.get(cl);
			for (OWLAxiom ax : clAxs) {
				Out.p("\t" + getString(ax, iri));
			}
		}
	}
	
	public static void print(Set<OWLAxiom> axioms, String iri, Set<OWLEntity> signature) {
		Out.p("");
		for (OWLAxiom ax : axioms) {
			if (signature.equals(ax.getSignature())) {
				printAxiom(ax, iri);
			}
		}
		Out.p("");
	}
	
	public static void printAxiom(OWLAxiom ax, String iri) {
		Out.p(ax.toString().replaceAll(iri, ""));
	}
	
	
	
	
	public static String getString(OWLAxiom ax, String iri) {
		return ax.toString().replaceAll(iri, "");		
	}
	
	public static String getString(OWLEntity en, String iri) {		
		return en.toString().replaceAll(iri, "");
	}
	
	public static String getString(OWLClassExpression cl, String iri) {		
		return cl.toString().replaceAll(iri, "").replaceAll(" ", SPACE_REPLACER)
				.replaceAll(IRI_SEPARATOR, "").replaceAll(LEFT_ANGLE, "").replaceAll(RIGHT_ANGLE, "");
	}
	
	public static String printArray(double[][] arr) {
		String res = "";
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				res += arr[i][j] + ", ";
			}
			res += "\n";
		}
		return res;
	}
	
	public static String printArray(Object[][] arr) {
		String res = "";
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				res += arr[i][j] + "\t";
			}
			res += "\n";
		}
		return res;
	}
	
	
	
	public static void print(OWLOntology ontology, 
			OWLRenderer renderer) {
		try {
			renderer.render(ontology, System.out);
		} catch (OWLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void printClassesMS(Collection<? extends OWLClassExpression> classes) {
		//ManchesterOWLSyntaxObjectRenderer rendering =
		//		new ManchesterOWLSyntaxObjectRenderer();
        OWLObjectRenderer rendering = new DLSyntaxObjectRenderer();
        Out.p("\nClasses in Manchester syntax:");
		for (OWLClassExpression cl : classes) {
			Out.p(rendering.render(cl));
		}
		Out.p("\n");
	}
	
	
	
	public static void printClassMS(OWLClassExpression cl) {
        OWLObjectRenderer rendering = new DLSyntaxObjectRenderer();
		Out.p(rendering.render(cl));
	}
	
	
	
	public static void printAxiomMS(OWLAxiom axiom) {
        OWLObjectRenderer rendering = new DLSyntaxObjectRenderer();
		Out.p(rendering.render(axiom));
	}
	
	
	
	public static void printAxiomsMS(Collection<? extends OWLAxiom> axioms) {
        OWLObjectRenderer rendering = new DLSyntaxObjectRenderer();
		Out.p("\nAxioms in Manchester syntax:");
		for (OWLAxiom axiom : axioms) {
			Out.p(rendering.render(axiom));
		}
		Out.p("\n");
	}
	
	
	
	public static void printAxiomsMSLabels(OntologyHandler handler) {
        OWLObjectRenderer rendering = new DLSyntaxObjectRenderer();
		List<String> axmsList = new ArrayList<>();
		Out.p("\nAxioms in Manchester syntax:");
		for (OWLAxiom axiom : handler.getLogicalAxioms()) {
			String axms = rendering.render(axiom);
			Set<OWLEntity> ents = axiom.getSignature();			
			for (OWLEntity ent : ents) {
				String entms = rendering.render(ent);
				String label = handler.getLabel(ent);
				if (label != null) {
					axms = axms.replaceAll(entms, label);
				}
			}
			axmsList.add(axms);
		}
		Collections.sort(axmsList, 
				new StringLengthComparator(SortingOrder.ASC));
		int count = 0;
		int step = 15;
		for (String s : axmsList) {
			count++;
			if (count % step == 0) { 
				Out.p(s);
			}
		}
		Out.p("\n");
	}
	
		
	
	

	public static void p(Object o) {
		System.out.println(o);
	}
	
	public static void s(Object o) {
		System.out.print(o);
	}

	public static void printHypothesisFile(File file) {
		FileInputStream inputStream;
		String result = null;
		try {
			inputStream = new FileInputStream(file);
			result = OwlFileImprover.convertToManchesterOWLSyntax(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		p(result);
	}
	
	
	public static void setLog(File path) {
		PrintStream out = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		try {
			out = new PrintStream(new FileOutputStream(path + "/log" + dateFormat.format(date) + ".txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.setOut(out);
	}
	
	
	public static void main(String[] args) {
		OntologyHandler handler = new OntologyHandler(args[0]);
		printAxiomsMSLabels(handler);
	}

	
	public static void printQualityValues(Collection<Hypothesis> hypotheses) {
		Out.p("\nPrinting scaled quality values:");
		for (Hypothesis h : hypotheses) {
			String measStr = "";
			for (int i=0; i<h.scaledMeasures.length; i++) {
				measStr += h.scaledMeasures[i] + "\t";
			}
			Out.p(measStr);
		}
	}

	public static void printHypothesesMS(Collection<Hypothesis> hypotheses) {
		Out.p("\nPrinting hypotheses in the Manchester syntax:\n");
		for (Hypothesis h : hypotheses) {
			for (OWLAxiom ax : h.axioms) {
				printAxiomMS(ax);
			}
		}
	}
	
	public static String fn(double number) {
		DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        return df.format(number);
	}
	
}
