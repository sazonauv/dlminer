package io.dlminer.ont;

import io.dlminer.graph.Graph;
import io.dlminer.print.CSVWriter;
import io.dlminer.print.Out;
import io.dlminer.sort.ArrayIndexComparator;
import io.dlminer.sort.SortingOrder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DLExpressivityChecker;


public class OntologyAnalyser {
	
	public static final String[] METRIC_NAMES = {	
		"ont_logic",
		"ind_num",
		"ca_num", 
		"ra_num", 
		"degree",
		"connect",
		"abox_sig", 
		"tbox_sig", 
		"jaccard_atbox_sig"
	};
	
	private OntologyHandler handler;
	
	private String[] metrics;
	
	
	
	public OntologyAnalyser(OntologyHandler handler) {		
		this.handler = handler;
	}
	


	public String[] extractMetrics() {
		if (metrics != null) {
			return metrics;
		}
		// init a visitor
		handler.initAxiomVisitor();
		
		Set<OWLEntity> aboxsig = handler.getABoxSignature();
		Set<OWLEntity> tboxsig = handler.getTBoxSignature();
		
		metrics = new String[METRIC_NAMES.length];
		
		metrics[0] = getExpressivity();
		
		metrics[1] = Integer.toString(handler.getIndividuals().size());
		
		Set<OWLClassAssertionAxiom> cas = handler.getClassAssertions();
		metrics[2] = Integer.toString(cas.size());
		Set<OWLObjectPropertyAssertionAxiom> ras = handler.getObjectPropertyAssertions();
		metrics[3] = Integer.toString(ras.size());

		metrics[4] = Double.toString(getAverageDegree());
		metrics[5] = Double.toString(getConnectedness());		
		
		metrics[6] = Integer.toString(aboxsig.size());
		metrics[7] = Integer.toString(tboxsig.size());
		
		metrics[8] = Double.toString(jaccard(aboxsig, tboxsig));		
		
		return metrics;
	}
	
	


	private double countNumberRestrictions() {
		return handler.getNumberRestrictions().size();
	}


	private double countUniversals() {				
		return handler.getUniversals().size();
	}


	public String getExpressivity() {
		Set<OWLOntology> onts = new HashSet<>();
		onts.add(handler.getOntology());
		DLExpressivityChecker checker = new DLExpressivityChecker(onts);
		return checker.getDescriptionLogicName();
	}
	


	private double getConnectedness() {
		Set<OWLObjectPropertyAssertionAxiom> ras = handler.getObjectPropertyAssertions();
		Set<OWLNamedIndividual> inds = handler.getIndividuals();
		if (inds.size()>0) {
			Set<Set<OWLNamedIndividual>> cons = getConnectedIndividuals(inds, ras);		
			return (double)inds.size()/cons.size();
		} else {
			return 0;
		}
	}
	
	public Set<Set<OWLNamedIndividual>> getConnectedIndividuals(Set<OWLNamedIndividual> inds, Set<OWLObjectPropertyAssertionAxiom> ras) {
		// build an undirected graph
		Graph<OWLNamedIndividual> aboxGraph = new Graph<>(inds);
		for (OWLAxiom ra : ras) {
			Set<OWLNamedIndividual> pair = ra.getIndividualsInSignature();
			for (OWLNamedIndividual ind1 : pair) {
				for (OWLNamedIndividual ind2 : pair) {
					if (!ind1.equals(ind2)) {
						aboxGraph.addChild(ind1, ind2);
					}
				}
				break;
			}
		}
		// get connected subgraphs
		Set<Set<OWLNamedIndividual>> cons = aboxGraph.getConnectedSets();
		return cons;
	}
	
	private double getAverageDegree() {
		Set<OWLObjectPropertyAssertionAxiom> ras = handler.getObjectPropertyAssertions();
		Set<OWLNamedIndividual> inds = handler.getIndividuals();
		if (inds.size()>0) {			
			return (double)ras.size()/inds.size();
		} else {
			return 0;
		}
	}
	
	private double getDegreeK(Set<OWLAxiom> ras, int k) {
		Set<OWLNamedIndividual> inds = handler.getIndividuals();
		if (inds.size()>0) {
			// map individuals to their degrees
			Map<OWLNamedIndividual, Double> indMap = 
					new HashMap<>(inds.size());
			for (OWLNamedIndividual ind : inds) {
				indMap.put(ind, new Double(0));
			}
			// count degrees
			for (OWLAxiom ra : ras) {
				Set<OWLNamedIndividual> raInds = ra.getIndividualsInSignature();
				for (OWLNamedIndividual raInd : raInds) {
					indMap.put(raInd, indMap.get(raInd)+1);
				}
			}
			// count individuals with degree >= k
			double ndegreek = 0;
			for (OWLNamedIndividual ind : indMap.keySet()) {
				if (indMap.get(ind) >= k) {
					ndegreek++;
				}
			}
			return ndegreek/inds.size();
		} else {
			return 0;
		}
	}
	

	private Set<OWLEntity> intersection(Set<OWLEntity> sig1, Set<OWLEntity> sig2) {
		Set<OWLEntity> inters = new HashSet<>(sig1);
		inters.retainAll(sig2);
		return inters;
	}
	
	private Set<OWLEntity> union(Set<OWLEntity> sig1, Set<OWLEntity> sig2) {
		Set<OWLEntity> union = new HashSet<>(sig1);
		union.addAll(sig2);
		return union;
	}
	
	private double jaccard(Set<OWLEntity> sig1, Set<OWLEntity> sig2) {
		return (double) intersection(sig1, sig2).size()/union(sig1, sig2).size();
	}
	
		
	
	private Set<OWLClass> findSignificantConcepts(int nConcepts, Set<OWLClass> classes, OWLReasoner reasoner) {
		// sort concepts by their size
		List<OWLClass> cls = new ArrayList<>(classes);
		int[] sizes = new int[cls.size()];
		for (int i=0; i<cls.size(); i++) {
			sizes[i] = reasoner.getInstances(cls.get(i), false).getFlattened().size();
		}		
		ArrayIndexComparator comp = new ArrayIndexComparator(sizes, SortingOrder.DESC);
		Integer[] inds = comp.createIndexArray();
		Arrays.sort(inds, comp);
		// return
		Set<OWLClass> bigs = new HashSet<>(nConcepts);
		for (int i=0; i<nConcepts; i++) {
			bigs.add(cls.get(inds[i]));
		}
		return bigs;
	}
	
	
	private Set<OWLObjectProperty> findSignificantRoles(int nRoles, Set<OWLObjectProperty> properties, OWLReasoner reasoner) {
		Set<OWLNamedIndividual> inds = handler.getIndividuals();		
		// get role sizes
		List<OWLObjectProperty> roles = new ArrayList<>(properties);
		int[] sizes = new int[roles.size()];
		for (int i=0; i<roles.size(); i++) {
			OWLObjectProperty role = roles.get(i);
			for (OWLNamedIndividual ind : inds) {
				Set<OWLNamedIndividual> points = reasoner.getObjectPropertyValues(ind, role).getFlattened();
				sizes[i] += points.size();						
			}
		}
		// sort roles by their size
		ArrayIndexComparator comp = new ArrayIndexComparator(sizes, SortingOrder.DESC);
		Integer[] indices = comp.createIndexArray();
		Arrays.sort(indices, comp);
		// return
		Set<OWLObjectProperty> bigs = new HashSet<>(nRoles);
		for (int i=0; i<nRoles; i++) {
			bigs.add(roles.get(indices[i]));
		}
		return bigs;
	}


	public Set<OWLEntity> findSeedSignature(int nConcepts, int nRoles, OWLReasoner reasoner) {	
		Set<OWLAxiom> axioms = handler.copyAxioms();
		Set<OWLEntity> modSig = OntologyHandler.getSignature(axioms);
		Set<OWLClass> modConcs = OntologyHandler.getClassesInSignature(axioms);
		Set<OWLObjectProperty> modRoles = OntologyHandler.getObjectPropertiesInSignature(axioms);		
		Out.p("ABox module signature size: " + modSig.size() + 
				" including " + modConcs.size() + " classes, " + 
				modRoles.size() + " roles");		
		int nC = nConcepts > modConcs.size() ? modConcs.size() : nConcepts;
		int nR = nRoles > modRoles.size() ? modRoles.size() : nRoles;			
		// get significant concepts
		Set<OWLClass> concs = null;
		if (nConcepts >= modConcs.size()) {
			concs = modConcs;
		} else {
			concs = findSignificantConcepts(nC, modConcs, reasoner);
		}		
		Out.p(concs.size() + " concepts are selected");
		// get significant roles		
		Set<OWLObjectProperty> roles = null;
		if (nRoles >= modRoles.size()) {
			roles = modRoles;
		} else {
			roles = findSignificantRoles(nR, modRoles, reasoner);
		}				
		Out.p(roles.size() + " roles are selected");
		Set<OWLEntity> signature = new HashSet<OWLEntity>(concs);
		signature.addAll(roles);					
		return signature;
	}
	
	
	
	public static void main(String[] args) {
		File dir = new File(args[0]);
		File csvPath = new File(args[1]);
		if (!csvPath.exists()) {
			csvPath.mkdirs();
		}
		File csvStatsFile = new File(csvPath, "ontology_metrics.csv");
		ReasonerName reasonerName = null;
        for (ReasonerName rname : ReasonerName.values()) {
        	if (rname.toString().equalsIgnoreCase(args[2])) {
        		reasonerName = rname;
        	}
        }
        long timeout = Long.parseLong(args[3]);
        int maxChecks = Integer.parseInt(args[4]);
		for (File ontFile : dir.listFiles()) {
			Out.p("\nLoading " + ontFile);
			OntologyHandler handler = new OntologyHandler(ontFile);			
			OntologyAnalyser analyser = new OntologyAnalyser(handler);
			analyser.extractMetrics();
			// reasoner
			Out.p("\nClassifying " + ontFile);
			handler = handler.extractBotDataModule();
			Set<OWLAxiom> axioms = handler.getAxioms();
			Out.p("contains " + AxiomMetric.countNegations(axioms) + " negations, "
					+ handler.countDisjointnessAxioms() + " disjointness axioms");
			OWLReasoner reasoner = null;			
			String exception = "";
			double clTime = 0;	
			double irTime = 0;
			try {				
				double t1 = System.currentTimeMillis();
				reasoner = ReasonerLoader.initReasoner(
						reasonerName, handler.getOntology(), timeout);
				// check if the ontology is consistent
	        	if (!reasoner.isConsistent()) {
	        		Out.p("\nThe ontology is inconsistent!");
	        		handler.removeInconsistency(reasoner);
	        		reasoner.flush();
	        	}
	        	Out.p("\nComputing class hierarchy");
	        	reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
	        			InferenceType.OBJECT_PROPERTY_HIERARCHY);
	        	double t2 = System.currentTimeMillis();
				clTime = (t2 - t1) / 1e3;
	        	Out.p("\nChecking instances");	        	
	        	Map<OWLClassExpression, Set<OWLNamedIndividual>> clInstMap = 
	        		checkInstances(handler, reasoner, maxChecks);
	        	double t3 = System.currentTimeMillis();
	        	irTime = (t3 - t2) / (1e3 * clInstMap.size());
				reasoner.dispose();
			} catch (Throwable e) {
				exception = e.getClass().toString();
				e.printStackTrace();				
			}
			
			// save statistics            
			analyser.saveMetricsToCSV(csvStatsFile, ontFile.getName(), 
					clTime, irTime, exception);			
		}
		
		Out.p("\nAll is done.");
	}



	private static Map<OWLClassExpression, Set<OWLNamedIndividual>> checkInstances(
			OntologyHandler handler, OWLReasoner reasoner, int maxChecks) {
		Map<OWLClassExpression, Set<OWLNamedIndividual>> classInstanceMap = new HashMap<>();
		Set<OWLClassExpression> exprs = handler.generateRandomClassExpressions(maxChecks);		
		int count = 0;		
		for (OWLClassExpression expr : exprs) {
			if (count >= maxChecks) {
				break;
			}
			if (expr.isOWLThing() || expr.isOWLNothing()) {
				continue;
			}			
			try {
				classInstanceMap.put(expr, reasoner.getInstances(expr, false).getFlattened());				
			} catch (Exception e) {
				e.printStackTrace();
			}
			Out.p(++count + " / " + exprs.size() + " classes are checked");			
		}
		return classInstanceMap;
	}



	private void saveMetricsToCSV(File csvStatsFile, 
			String ontName, double chTime, double irTime, String exception) {
		CSVWriter csvWriter = null;
		if (!csvStatsFile.exists()) {			
			try {
				// create a new file
				csvStatsFile.createNewFile();	
				csvWriter = new CSVWriter(csvStatsFile, false);
				csvWriter.createMetricsHeader();
				csvWriter.saveMetricsToCSV(ontName, metrics, chTime, irTime, exception);
				csvWriter.close();
			} catch (Exception e) {			
				e.printStackTrace();
			}
		} else {
			// add lines without erasing contents
			csvWriter = new CSVWriter(csvStatsFile, true);		
			csvWriter.saveMetricsToCSV(ontName, metrics, chTime, irTime, exception);
			csvWriter.close();
		}
		
		
	}
	
	
	
	
}
