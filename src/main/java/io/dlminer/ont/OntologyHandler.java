package io.dlminer.ont;

import io.dlminer.graph.Graph;
import io.dlminer.learn.ConceptBuilder;
import io.dlminer.learn.Hypothesis;
import io.dlminer.learn.HypothesisEvaluator;
import io.dlminer.print.Out;
import io.dlminer.refine.ALCOperator;
import io.dlminer.refine.RefinementOperator;
import io.dlminer.sort.ArrayIndexComparator;
import io.dlminer.sort.AxiomLengthComparator;
import io.dlminer.sort.SortingOrder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.impl.blackbox.checker.InconsistentOntologyExplanationGeneratorFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitorEx;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.SpecificOntologyChangeBroadcastStrategy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 * @author Slava Sazonau
 * The University of Manchester
 * Information Management Group
 * 
 * The class is an ontology wrapper.
 */
public class OntologyHandler {

	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private AxiomVisitor visitor;
	
	
	public OntologyHandler(OWLOntology ontology) {
		this.ontology = ontology;
		manager = ontology.getOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		manager.setDefaultChangeBroadcastStrategy(
				new SpecificOntologyChangeBroadcastStrategy(this.ontology));
	}
	
	public OntologyHandler(String fileName) {		
		loadOntology(fileName);		
	}
	
	public OntologyHandler(File file) {		
		loadOntology(file);		
	}
	
	public OntologyHandler(InputStream stream) {	
		loadOntology(stream);
	}
		
	public OntologyHandler(Set<OWLAxiom> axioms, IRI iri) {		
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = null;
		try {
			if (iri != null) {
				ontology = manager.createOntology(axioms, iri);
			} else {
				ontology = manager.createOntology(axioms);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
	public OntologyHandler(Set<OWLAxiom> axioms) {		
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = null;
		try {			
			ontology = manager.createOntology(axioms);	
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
	public OntologyHandler(IRI iri) {		
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = null;
		try {
			if (iri != null) {
				ontology = manager.createOntology(new HashSet<OWLAxiom>(), iri);
			} else {
				ontology = manager.createOntology(new HashSet<OWLAxiom>());
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}		
	}
	
	
	public OntologyHandler() {		
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = null;
		try {
			ontology = manager.createOntology(new HashSet<OWLAxiom>());
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}		
	}
		
		
	private void loadOntology(String fileName) {
//		File ontFile = new File(fileName);
//		loadOntology(ontFile);
		loadOntologyWithImports(fileName);
	}
	
	
	private void loadOntology(File ontFile) {		
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(ontFile);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}		
	}
	
	
	private void loadOntology(InputStream stream) {		
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(stream);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}		
	}
	
	
	private void loadOntologyWithImports(String fileName) {
		File ontFile = new File(fileName);
		loadOntologyWithImports(ontFile);
	}
	
	private void loadOntologyWithImports(File ontFile) {		
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = null;		
		AutoIRIMapper mapper = new AutoIRIMapper(ontFile.getParentFile(), true);
		manager.addIRIMapper(mapper);		
		try {
			ontology = manager.loadOntologyFromOntologyDocument(ontFile);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		// include all imports
		for (OWLOntology imp : ontology.getImportsClosure()) {			
			manager.addAxioms(ontology, imp.getAxioms());
		}
	}
	
	
	public OWLOntologyManager getManager() {
		return manager;
	}		

	public OWLOntology getOntology() {
		return ontology;
	}

	public void removeAxiom(OWLAxiom ax) {
		manager.removeAxiom(ontology, ax);		
	}

	public void addAxiom(OWLAxiom ax) {
		manager.addAxiom(ontology, ax);
	}
	
	public void addAxioms(Collection<? extends OWLAxiom> axioms) {		
		if (axioms != null) {
			for (OWLAxiom ax : axioms) {
				addAxiom(ax);
			}
		}
	}
	
	public void addAxioms(Set<? extends OWLAxiom> axioms) {
		manager.addAxioms(ontology, axioms);
	}
	

		
	
	public void removeAxioms(Collection<? extends OWLAxiom> axioms) {
		if (axioms != null) {
			for (OWLAxiom ax : axioms) {
				removeAxiom(ax);
			}
		}
	}	
	
	public void removeAxioms(Set<? extends OWLAxiom> axioms) {
		manager.removeAxioms(ontology, axioms);
	}
	
	
	public void removeHypotheses(Collection<Hypothesis> hypotheses) {
		for (Hypothesis h : hypotheses) {
			removeAxioms(h.axioms);
		}
	}
	
	
	public void removeLogicalAxioms() {
		removeAxioms(ontology.getLogicalAxioms());
	}
	
	
	public void removeAxioms() {
		removeAxioms(ontology.getAxioms());
	}
	
	
		
	public void chunkOntology(File dir, double fraction) {
		if (fraction <= 0 || fraction >= 1) {
			throw new IllegalArgumentException("fraction must be in the interval (0, 1)");
		}
		try {
			if (!dir.exists()) {				
					dir.createNewFile();				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<OWLAxiom> partAxioms = new HashSet<>();
		int chunkCounter = 0;
		for (OWLAxiom axiom : ontology.getAxioms()) {			
			partAxioms.add(axiom);
			if ( ((double)(partAxioms.size())/ontology.getAxiomCount()) > fraction ) {
				chunkCounter++;
				File chunkFile = new File(dir, chunkCounter+".owl");
				OntologyHandler partHandler = new OntologyHandler(partAxioms, getIRI());
				partHandler.saveOntology(chunkFile);
				partAxioms = new HashSet<>();
			}
		}
	}
	
	
	
	
	
	
	public void saveOntology(File file) {	
		saveOntology(file, null);		
	}
	
	
	public void saveOntology(File file, OWLOntologyFormat format) {						
		try {
			if (!file.exists()) {				
					file.createNewFile();				
			}
			FileOutputStream outputStream = new FileOutputStream(file);			
			saveOntology(outputStream, format);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	
	
	
	
	
	
	
	public void saveOntology(OutputStream outputStream) {						
		saveOntology(outputStream, null);		
	}
	
	
	public void saveOntology(OutputStream outputStream, OWLOntologyFormat format) {						
		try {
			BufferedOutputStream buffOutputStream = new BufferedOutputStream(outputStream);
			if (format != null) {
				manager.saveOntology(ontology, format, buffOutputStream);
			} else {
				manager.saveOntology(ontology, new OWLXMLOntologyFormat(), buffOutputStream);
			}
			buffOutputStream.close();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	
	public Set<OWLNamedIndividual> getIndividuals() {
		return ontology.getIndividualsInSignature();
	}
	
	public static Set<OWLNamedIndividual> getIndividuals(Set<OWLAxiom> axioms) {
		Set<OWLNamedIndividual> inds = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			inds.addAll(ax.getIndividualsInSignature());
		}
		return inds;
	}

	public boolean containsAxiom(OWLAxiom axiom) {
		return ontology.containsAxiom(axiom);
	}
			
	public int getOntologySize() {
		return ontology.getLogicalAxiomCount();
	}
		
	public OWLDataFactory getDataFactory() {
		return factory;
	}
	
	public static int getConceptLength(OWLClassExpression expr) {
		return expr.getSignature().size();
	}
	
	public static int getConceptLength(Set<OWLClassExpression> exprSet) {
		int len = 0;
		for (OWLClassExpression expr : exprSet) {
			if (len < getConceptLength(expr)) {
				len = getConceptLength(expr);
			}
		}
		return len;
	}	
	
	public Set<OWLAxiom> copyAxioms() {
		Set<OWLAxiom> axioms = new HashSet<>();
		axioms.addAll(getTBoxAxioms());
		axioms.addAll(getABoxAxioms());
		axioms.addAll(getRBoxAxioms());
		return axioms;
	}
		
	public Set<OWLAxiom> getTBoxAxioms() {		
		return ontology.getTBoxAxioms(true);
	}	
	
	public Set<OWLAxiom> getRBoxAxioms() {		
		return ontology.getRBoxAxioms(true);
	}
	
	public Set<OWLAxiom> getABoxAxioms() {
		return ontology.getABoxAxioms(true);
	}	
	
	public static List<OWLClassExpression> getLHSandRHS(OWLAxiom ax) {
		// always 2 expressions
		Set<OWLClassExpression> exprs = ax.getNestedClassExpressions();
		// determine which one is LHS
		int lhInd = Integer.MAX_VALUE;
		OWLClassExpression lh = null;
		for (OWLClassExpression expr : exprs) {
			int ind = ax.toString().indexOf(expr.toString());
			if (lhInd > ind) {
				lhInd = ind;
				lh = expr;
			}			
		}		
		// add LHS, then RHS
		List<OWLClassExpression> sides = new ArrayList<>(2);
		sides.add(lh);
		for (OWLClassExpression expr : exprs) {
			if (!expr.equals(lh)) {
				sides.add(expr);
				break;
			}
		}
		return sides;
	}
	
	public static OWLClassExpression getSubClass(OWLAxiom axiom) {
		return ((OWLSubClassOfAxiom)axiom).getSubClass();
	}
	
	public static OWLClassExpression getSuperClass(OWLAxiom axiom) {
		return ((OWLSubClassOfAxiom)axiom).getSuperClass();
	}
	
	public static OWLObjectPropertyExpression getSubProperty(OWLAxiom axiom) {
		return ((OWLSubObjectPropertyOfAxiom)axiom).getSubProperty();
	}
	
	public static OWLObjectPropertyExpression getSuperProperty(OWLAxiom axiom) {
		return ((OWLSubObjectPropertyOfAxiom)axiom).getSuperProperty();
	}
	
	
	public void initAxiomVisitor() {
		visitor = new AxiomVisitor();
		for (OWLAxiom ax : ontology.getLogicalAxioms()) {
			ax.accept(visitor);
		}
	}
	
	public boolean hasAxiomVisitor() {
		return visitor != null;
	}
	
	
	public Set<OWLClass> getClassesInABox() {
		Set<OWLAxiom> abox = getABoxAxioms();
		return getClassesInSignature(abox);
	}

	public Set<OWLObjectProperty> getObjectPropertiesInABox() {
		Set<OWLAxiom> abox = getABoxAxioms();
		return getObjectPropertiesInSignature(abox);
	}

    public Set<OWLDataProperty> getDataPropertiesInABox() {
        Set<OWLAxiom> abox = getABoxAxioms();
        return getDataPropertiesInSignature(abox);
    }


	public Set<OWLEntity> getABoxSignature() {
		Set<OWLAxiom> abox = getABoxAxioms();
		return getSignature(abox);
	}

	
	// only classes and object properties
	public Set<OWLEntity> getTBoxSignature() {
		Set<OWLAxiom> tbox = getTBoxAxioms();
		return getSignature(tbox);
	}
	
		
	
	public Set<OWLClassAssertionAxiom> getClassAssertions() {
		if (!hasAxiomVisitor()) {
			initAxiomVisitor();
		}
		return visitor.ClassAssertionAxioms;
	}
	
	
	public Set<OWLClassAssertionAxiom> getClassAssertions(Set<OWLEntity> sig) {
		Set<OWLClassAssertionAxiom> cas = getClassAssertions();
		Set<OWLClassAssertionAxiom> scas = new HashSet<>();
		for (OWLClassAssertionAxiom ax : cas) {
			if (sig.containsAll(ax.getClassesInSignature())) {
				scas.add(ax);
			}
		}
		return scas;
	}
	
	
	public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertions() {
		if (!hasAxiomVisitor()) {
			initAxiomVisitor();
		}
		return visitor.ObjectPropertyAssertionAxioms;
	}
	
	public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertions(Set<OWLEntity> sig) {
		Set<OWLObjectPropertyAssertionAxiom> ras = getObjectPropertyAssertions();
		Set<OWLObjectPropertyAssertionAxiom> sras = new HashSet<>();
		for (OWLObjectPropertyAssertionAxiom ax : ras) {
			if (sig.containsAll(ax.getObjectPropertiesInSignature())) {
				sras.add(ax);
			}
		}
		return sras;
	}

	public IRI getIRI() {
		return ontology.getOntologyID().getOntologyIRI();
	}
	
	
	private Set<OWLClassAssertionAxiom> getClassAssertionsOfInd(OWLNamedIndividual ind) {
		if (!hasAxiomVisitor()) {
			initAxiomVisitor();
		}
		Set<OWLClassAssertionAxiom> cas = new HashSet<>();
		for (OWLClassAssertionAxiom ax : visitor.ClassAssertionAxioms) {
			if (ax.getIndividualsInSignature().contains(ind)
					&& !isTautology(ax)) {
				cas.add(ax);
			}
		}		
		return cas;
	}
	
	
	
	public Set<OWLClassAssertionAxiom> getClassAssertionsOfInds(Set<OWLNamedIndividual> con) {
		if (!hasAxiomVisitor()) {
			initAxiomVisitor();
		}
		Set<OWLClassAssertionAxiom> cas = new HashSet<>();
		for (OWLClassAssertionAxiom ax : visitor.ClassAssertionAxioms) {
			if (con.containsAll(ax.getIndividualsInSignature())
					&& !isTautology(ax)) {
				cas.add(ax);
			}
		}		
		return cas;
	}
	
	private static boolean isTautology(OWLAxiom ax) {		
		Set<OWLClassExpression> cls = ax.getNestedClassExpressions();
		if (cls.size() != 1) {
			return false;
		}
		boolean hasThing = false;
		for (OWLClassExpression cl : cls) {
			if (cl.isOWLThing()) {
				hasThing = true;
				break;
			}
		}
		return hasThing;
	}
	
	public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionsOfInds(Set<OWLNamedIndividual> con) {
		if (!hasAxiomVisitor()) {
			initAxiomVisitor();
		}
		Set<OWLObjectPropertyAssertionAxiom> ras = new HashSet<>();
		for (OWLObjectPropertyAssertionAxiom ax : visitor.ObjectPropertyAssertionAxioms) {
			if (con.containsAll(ax.getIndividualsInSignature())) {
				ras.add(ax);
			}
		}		
		return ras;
	}
	
	
	
	

	public static Set<OWLEntity> getSignature(Set<? extends OWLAxiom> axioms) {
		Set<OWLEntity> sig = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			sig.addAll(ax.getClassesInSignature());
			sig.addAll(ax.getObjectPropertiesInSignature());
            sig.addAll(ax.getDataPropertiesInSignature());
		}
		return sig;
	}



	public Set<OWLClass> getClassesInSignature() {
		return ontology.getClassesInSignature();
	}
	
	public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
		return ontology.getObjectPropertiesInSignature();
	}

    public Set<OWLDataProperty> getDataPropertiesInSignature() {
        return ontology.getDataPropertiesInSignature();
    }


	public static Set<OWLClass> getClassesInSignature(Set<OWLAxiom> axioms) {
		Set<OWLClass> cls = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			cls.addAll(ax.getClassesInSignature());
		}
		return cls;
	}
	
	public static Set<OWLObjectProperty> getObjectPropertiesInSignature(Set<OWLAxiom> axioms) {
		Set<OWLObjectProperty> sig = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			sig.addAll(ax.getObjectPropertiesInSignature());			
		}
		return sig;
	}

    public static Set<OWLDataProperty> getDataPropertiesInSignature(Set<OWLAxiom> axioms) {
        Set<OWLDataProperty> sig = new HashSet<>();
        for (OWLAxiom ax : axioms) {
            sig.addAll(ax.getDataPropertiesInSignature());
        }
        return sig;
    }


	public static Set<OWLAxiom> getClassAssertionsOfInds(
			Set<OWLAxiom> axioms, Set<OWLNamedIndividual> con) {
		Set<OWLAxiom> cas = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			if (con.containsAll(ax.getIndividualsInSignature())
					&& !isTautology(ax)) {
				cas.add(ax);
			}
		}		
		return cas;
	}
	
	
	public Set<Set<OWLNamedIndividual>> getConnectedIndividuals() {
		Set<OWLNamedIndividual> inds = getIndividuals();
		return getConnectedIndividuals(inds);
	}
	
	
	public Set<Set<OWLNamedIndividual>> getConnectedIndividuals(Set<OWLNamedIndividual> inds) {
		return getConnectedIndividuals(inds, getObjectPropertyAssertions());
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
	
	
	
	/*public void removeABoxInconsistency() {
		Out.p("Repairing inconsistency");
		// create the explanation generator
		OWLReasonerFactory rf = new Reasoner.ReasonerFactory();
		ExplanationGenerator<OWLAxiom> eg = 
				new InconsistentOntologyExplanationGeneratorFactory(rf, 1000L)
				.createExplanationGenerator(ontology);	
		// ask for an explanation of "Thing SubClassOf Nothing"
		OWLAxiom ent = factory.getOWLSubClassOfAxiom(factory.getOWLThing(), 
				factory.getOWLNothing());
		Set<Explanation<OWLAxiom>> expls = eg.getExplanations(ent, 10);
		// remove all assertions occurring in justifications
		for (Explanation<OWLAxiom> expl : expls) {
			for (OWLAxiom ax : expl.getAxioms()) {
				if (ax.isOfType(AxiomType.ABoxAxiomTypes)) {
					removeAxiom(ax);
				}
			}
		}
	}*/
	
	
		
	
	public Set<OWLAxiom> removeInconsistency(OWLReasoner reasoner) {
		Out.p("Repairing inconsistency");
		// create the explanation generator
		OWLReasonerFactory rf = new Reasoner.ReasonerFactory();
		long entCheckTimeout = 1000000;
		ExplanationGenerator<OWLAxiom> eg = 
				new InconsistentOntologyExplanationGeneratorFactory(rf, entCheckTimeout)
				.createExplanationGenerator(ontology);	
		// ask for an explanation of "Thing SubClassOf Nothing"
		OWLAxiom ent = factory.getOWLSubClassOfAxiom(factory.getOWLThing(), 
				factory.getOWLNothing());
		Set<Explanation<OWLAxiom>> expls = eg.getExplanations(ent, 30);
		// find most frequent axioms and remove them one by one
		Set<OWLAxiom> unionSet = new HashSet<>();
		for (Explanation<OWLAxiom> expl : expls) {
			unionSet.addAll(expl.getAxioms());			
		}
		Out.p("All axioms in justifications: " + unionSet.size());
		List<OWLAxiom> unionList = new ArrayList<>(unionSet);
		Set<OWLAxiom> removedAxioms = new HashSet<>();		
		int[] axiomCounts = new int[unionList.size()];
		for (int i=0; i<unionList.size(); i++) {
			OWLAxiom ax = unionList.get(i);
			int count = 0;
			for (Explanation<OWLAxiom> expl : expls) {
				if (expl.getAxioms().contains(ax)) {
					count++;
				}
			}
			axiomCounts[i] = count;
		}
		ArrayIndexComparator comp = new ArrayIndexComparator(axiomCounts, SortingOrder.DESC);
		Integer[] inds = comp.createIndexArray();
		Arrays.sort(inds, comp);
		Out.p("Removing most frequent axioms and testing");
		for (int i=0; i<inds.length; i++) {
			Out.p((i+1) + " / " + unionSet.size() + " removed");
			OWLAxiom ax = unionList.get(inds[i]);
			removeAxiom(ax);
			removedAxioms.add(ax);
			reasoner.flush();
			if (reasoner.isConsistent()) {
				break;
			}
		}
		return removedAxioms;
	}
	
	
	
	
	public void removeUnsatClasses(OWLReasoner reasoner) {
		Set<OWLClass> unsats = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		Set<OWLAxiom> removes = new HashSet<>();
		for (OWLAxiom axiom : ontology.getLogicalAxioms()) {			
			for (OWLClass cl : axiom.getClassesInSignature()) {
				if (unsats.contains(cl)) {
					removes.add(axiom);
					break;
				}
			}
		}
		removeAxioms(removes);
	}
	
	public void removeTautologies(OWLReasoner reasoner) {
		Set<OWLClass> tauts = reasoner.getTopClassNode().getEntitiesMinusTop();
		Set<OWLAxiom> removes = new HashSet<>();
		for (OWLAxiom axiom : ontology.getLogicalAxioms()) {			
			for (OWLClass cl : axiom.getClassesInSignature()) {
				if (tauts.contains(cl)) {
					removes.add(axiom);
					break;
				}
			}
		}
		removeAxioms(removes);
	}
	
	public void removeEmptyClasses(OWLReasoner reasoner) {
		// find empty ones
		Set<OWLClass> empties = new HashSet<>();
		for (OWLClass cl : getClassesInSignature()) {
			Set<OWLNamedIndividual> pos = reasoner.getInstances(cl, false).getFlattened();
			if (pos == null || pos.isEmpty()) {
				empties.add(cl);
			}
		}		
		// find axioms for deletion
		Set<OWLAxiom> removes = new HashSet<>();
		for (OWLAxiom axiom : ontology.getAxioms()) {			
			for (OWLClass cl : axiom.getClassesInSignature()) {
				if (empties.contains(cl)) {
					removes.add(axiom);
					break;
				}
			}
		}
		removeAxioms(removes);
	}
	
		
	
	
	public void removeEmptyRoles(OWLReasoner reasoner) {
		// find empty ones
		Set<OWLObjectProperty> empties = new HashSet<>();
		Set<OWLNamedIndividual> inds = getIndividuals();		
		for (OWLObjectProperty prop : getObjectPropertiesInSignature()) {			
			boolean empty = true;			
			// simple and inverse roles
			// chain definitions are not added!
			loop:
				for (OWLNamedIndividual ind : inds) {
					if (ind != null) {
						Set<OWLNamedIndividual> pos = 
								reasoner.getObjectPropertyValues(ind, prop).getFlattened();
						if (pos != null && !pos.isEmpty()) {
							empty = false;
							break loop;
						}		
					}
				}				
			if (empty) {
				empties.add(prop);
			}
		}		
		// find axioms for deletion
		Set<OWLAxiom> removes = new HashSet<>();
		for (OWLAxiom axiom : ontology.getAxioms()) {			
			for (OWLObjectProperty prop : axiom.getObjectPropertiesInSignature()) {
				if (empties.contains(prop)) {
					removes.add(axiom);
					break;
				}
			}
		}
		removeAxioms(removes);
	}
	
	
		
	
	public static boolean hasOverlap(Set<OWLEntity> sig1, Set<OWLEntity> sig2) {
		for (OWLEntity en : sig1) {
			if (sig2.contains(en)) {
				return true;
			}
		}		
		return false;
	}
	
	
	
		
	public static Set<OWLAxiom> extractModule(OWLOntology ontology, 
			Set<OWLEntity> signature, ModuleType moduleType) {
		SyntacticLocalityModuleExtractor extractor = 
				new SyntacticLocalityModuleExtractor(ontology.getOWLOntologyManager(), ontology, moduleType);
		return extractor.extract(signature);		
	}
	
	
		

	public static Set<OWLAxiom> hyposToAxioms(List<Hypothesis> hypos) {
		Set<OWLAxiom> axioms = new HashSet<>();
		for (Hypothesis h : hypos) {
			axioms.addAll(h.axioms);			
		}		
		return axioms;
	}
	
	public static Set<OWLAxiom> hyposToCodedAxioms(List<Hypothesis> hypos) {
		Set<OWLAxiom> axioms = new HashSet<>();
		for (Hypothesis h : hypos) {
			axioms.addAll(h.codedAxioms);			
		}		
		return axioms;
	}
	
	
	public Map<OWLAxiom, OWLAxiom> mapAxiomsToCodedAxioms(Set<OWLAxiom> axioms) {
		// collect all LHS and RHS
		Set<OWLClassExpression> classExprs = new HashSet<>();
		Set<OWLObjectPropertyExpression> propExprs = new HashSet<>();
		for (OWLAxiom axiom : axioms) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				OWLClassExpression subExpr = getSubClass(axiom);
				OWLClassExpression superExpr = getSuperClass(axiom);
				classExprs.add(subExpr);
				classExprs.add(superExpr);
			}
			if (axiom instanceof OWLSubObjectPropertyOfAxiom) {
				OWLObjectPropertyExpression subProp = getSubProperty(axiom);
				OWLObjectPropertyExpression superProp = getSuperProperty(axiom);
				propExprs.add(subProp);
				propExprs.add(superProp);
			}
		}
		// map coded classes to classes and coded properties to properties
		Map<OWLClassExpression, OWLClass> classMap = 
				new HashMap<>();
		for (OWLClassExpression expr : classExprs) {
			classMap.put(expr, generateClass());
		}
		Map<OWLObjectPropertyExpression, OWLObjectProperty> propMap = 
				new HashMap<>();
		for (OWLObjectPropertyExpression expr : propExprs) {
			propMap.put(expr, generateProperty());
		}
		// map coded axioms to axioms
		Map<OWLAxiom, OWLAxiom> axiomsMap = new HashMap<>();
		for (OWLAxiom axiom : axioms) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				OWLClassExpression subExpr = getSubClass(axiom);
				OWLClassExpression superExpr = getSuperClass(axiom);
				OWLSubClassOfAxiom codedAxiom = 
						factory.getOWLSubClassOfAxiom(classMap.get(subExpr), 
								classMap.get(superExpr));
				axiomsMap.put(codedAxiom, axiom);
			}
			if (axiom instanceof OWLSubObjectPropertyOfAxiom) {
				OWLObjectPropertyExpression subProp = getSubProperty(axiom);
				OWLObjectPropertyExpression superProp = getSuperProperty(axiom);
				OWLSubObjectPropertyOfAxiom codedAxiom = 
						factory.getOWLSubObjectPropertyOfAxiom(propMap.get(subProp), 
								propMap.get(superProp));
				axiomsMap.put(codedAxiom, axiom);
			}
		}		
		return axiomsMap;
	}
	
	
	
	public OWLClass generateClass() {
		IRI iri = getIRI();
		String C = UUID.randomUUID().toString();
		OWLClass cl = factory.getOWLClass(IRI.create(iri + Out.IRI_SEPARATOR + C));
		return cl;
	}
	
	public OWLObjectProperty generateProperty() {
		IRI iri = getIRI();
		String R = UUID.randomUUID().toString();
		OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(iri + Out.IRI_SEPARATOR + R));
		return prop;
	}


	public static OWLClass getAssertionClass(OWLAxiom axiom) {
		for (OWLClass c : axiom.getClassesInSignature()) {
			return c;
		}
		return null;
	}
	
	
	public void addDeclaration(OWLEntity ent) {
		addAxiom(factory.getOWLDeclarationAxiom(ent));
	}
	
	
	
	public void addDeclarations(Set<? extends OWLEntity> ents) {		
		for (OWLEntity ent : ents) {
			addDeclaration(ent);
		}
	}
	
	public Set<OWLEntity> getSignature() {
		return ontology.getSignature();
	}
	
	public Set<OWLEntity> getEntities(Set<String> names) {
		Set<OWLEntity> sig = this.getSignature();		
		Set<OWLEntity> res = new HashSet<>();
		String iriStr = this.getIRI().toString();
		for (OWLEntity en : sig) {
			String enName = en.getIRI().toString().replaceAll(iriStr, "");
			for (String name : names) {
				if (enName.equals(name)) {
					res.add(en);
					break;
				}
			}
		}
		return res;
	}

	public Set<OWLLogicalAxiom> getLogicalAxioms() {
		return ontology.getLogicalAxioms();
	}

	public void removeIrrelevantAxioms() {
		Set<OWLAxiom> removals = new HashSet<>();
		for (OWLAxiom ax : ontology.getLogicalAxioms()) {
			if (ax.isOfType(
//			        AxiomType.DATA_PROPERTY_ASSERTION,
//					AxiomType.DATA_PROPERTY_DOMAIN,
//					AxiomType.DATA_PROPERTY_RANGE,
//					AxiomType.DISJOINT_DATA_PROPERTIES,
//					AxiomType.EQUIVALENT_DATA_PROPERTIES,
//					AxiomType.FUNCTIONAL_DATA_PROPERTY,
//					AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION,
//					AxiomType.SUB_DATA_PROPERTY,
                    AxiomType.DATATYPE_DEFINITION,
					AxiomType.SWRL_RULE
					)) {
				removals.add(ax);
			}
		}
		removeAxioms(removals);
	}
	
	
	public Set<OWLObjectCardinalityRestriction> getNumberRestrictions() {
		return getNumberRestrictions(getTBoxAxioms());
	}
	
	
	
	public static Set<OWLObjectCardinalityRestriction> getNumberRestrictions(
			Set<? extends OWLAxiom> axioms) {
		Set<OWLObjectCardinalityRestriction> numberRestrictions = 
				new HashSet<>();
		for (OWLAxiom ax : axioms) {
			Set<OWLClassExpression> subExprs = ax.getNestedClassExpressions();
			for (OWLClassExpression expr : subExprs) {
				if (expr instanceof OWLObjectCardinalityRestriction) {
					numberRestrictions.add((OWLObjectCardinalityRestriction)expr);
				}
			}
		}
		return numberRestrictions;
	}
	
	

	public Set<OWLObjectAllValuesFrom> getUniversals() {		
		return getUniversals(getTBoxAxioms());
	}
	
	
	
	public static Set<OWLObjectAllValuesFrom> getUniversals(
			Set<? extends OWLAxiom> axioms) {
		Set<OWLObjectAllValuesFrom> universals = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			Set<OWLClassExpression> subExprs = ax.getNestedClassExpressions();
			for (OWLClassExpression expr : subExprs) {
				if (expr instanceof OWLObjectAllValuesFrom) {
					universals.add((OWLObjectAllValuesFrom)expr);
				}
			}
		}
		return universals;
	}
	
	
	private OWLObjectProperty getObjectPropertyByIRI(String id) {
		IRI iri = IRI.create(id);		
		return getObjectPropertyByIRI(iri);
	}
	
	
	private OWLObjectProperty getObjectPropertyByIRI(IRI iri) {
		if (!ontology.containsObjectPropertyInSignature(iri)) {
			return null;
		}
		Set<OWLEntity> ents = ontology.getEntitiesInSignature(iri);
		for (OWLEntity ent : ents) {
			if (ent.isOWLObjectProperty()) {
				return (OWLObjectProperty) ent;
			}
		}
		return null;
	}

	
	public OWLClass getClassByIRI(String id) {
		IRI iri = IRI.create(id);		
		return getClassByIRI(iri);
	}
	

	public OWLClass getClassByIRI(IRI iri) {		
		if (!ontology.containsClassInSignature(iri)) {
			return null;
		}
		Set<OWLEntity> ents = ontology.getEntitiesInSignature(iri);
		for (OWLEntity ent : ents) {
			if (ent.isOWLClass()) {
				return (OWLClass) ent;
			}
		}
		return null;
	}
	

	public Set<OWLAxiom> getNonLogicalAxioms() {	
		Set<OWLAxiom> annots = new HashSet<>(ontology.getAxioms());
		annots.removeAll(ontology.getLogicalAxioms());		
		return annots;
	}

	
	public Map<OWLNamedIndividual, Set<OWLClassAssertionAxiom>> 
		createIndClassAssertionMap() {
		Set<OWLAxiom> axioms = getABoxAxioms();
		Map<OWLNamedIndividual, Set<OWLClassAssertionAxiom>> indCAssMap = new HashMap<>();
		for (OWLAxiom axiom : axioms) {			
			if (axiom.isOfType(AxiomType.CLASS_ASSERTION)) {
				OWLClassAssertionAxiom fact = (OWLClassAssertionAxiom) axiom;
				OWLIndividual anonym = fact.getIndividual();
				if (anonym.isNamed()) {
					OWLNamedIndividual ind = anonym.asOWLNamedIndividual();
					Set<OWLClassAssertionAxiom> facts = indCAssMap.get(ind);
					if (facts == null) {
						facts = new HashSet<>();
						indCAssMap.put(ind, facts);
					}
					facts.add(fact);
				}
			}
		}
		return indCAssMap;
	}

	public Map<OWLNamedIndividual, Set<OWLObjectPropertyAssertionAxiom>> 
		createIndPropertyAssertionMap() {	
		Set<OWLAxiom> axioms = getABoxAxioms();
		Map<OWLNamedIndividual, Set<OWLObjectPropertyAssertionAxiom>> indRAssMap = new HashMap<>();
		for (OWLAxiom axiom : axioms) {			
			if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
				OWLObjectPropertyAssertionAxiom fact = (OWLObjectPropertyAssertionAxiom) axiom;
				OWLIndividual subj = fact.getSubject();
				OWLIndividual obj = fact.getObject();
				if (subj.isNamed() && obj.isNamed()) {
					OWLNamedIndividual ind = subj.asOWLNamedIndividual();
					Set<OWLObjectPropertyAssertionAxiom> facts = indRAssMap.get(ind);
					if (facts == null) {
						facts = new HashSet<>();
						indRAssMap.put(ind, facts);
					}
					facts.add(fact);
				}				
			}
		}
		return indRAssMap;
	}
	
	
	
	public Map<OWLNamedIndividual, Set<OWLDataPropertyAssertionAxiom>>
		createIndDataPropertyAssertionMap() {
		Set<OWLAxiom> axioms = getABoxAxioms();
		Map<OWLNamedIndividual, Set<OWLDataPropertyAssertionAxiom>> indDAssMap = new HashMap<>();
		for (OWLAxiom axiom : axioms) {			
			if (axiom.isOfType(AxiomType.DATA_PROPERTY_ASSERTION)) {
				OWLDataPropertyAssertionAxiom fact = (OWLDataPropertyAssertionAxiom) axiom;
				OWLIndividual subj = fact.getSubject();
				if (subj.isNamed()) {
					OWLNamedIndividual ind = subj.asOWLNamedIndividual();
					Set<OWLDataPropertyAssertionAxiom> facts = indDAssMap.get(ind);
					if (facts == null) {
						facts = new HashSet<>();
						indDAssMap.put(ind, facts);
					}
					facts.add(fact);
				}				
			}
		}
		return indDAssMap;
	}
	
	


	public Set<OWLClassExpression> getExpressionsFromAssertions(
			Set<OWLClassAssertionAxiom> facts) {
		Set<OWLClassExpression> exprs = new HashSet<>(2);
		if (facts == null) {
			return exprs;
		}
		for (OWLClassAssertionAxiom fact : facts) {
			exprs.add(fact.getClassExpression());
		}		
		return exprs;
	}

	
	/**
	 * @return mapping of individuals to their concepts
	 */
	public Map<OWLNamedIndividual, Set<OWLClassExpression>> 
		createIndClassExprMapInferred(Collection<? extends OWLClass> startClassExpressions,
				OWLReasoner reasoner) {
		Map<OWLNamedIndividual, Set<OWLClassExpression>> map = new HashMap<>();		
		for (OWLClassExpression expr : startClassExpressions) {			
			Set<OWLNamedIndividual> insts = reasoner.getInstances(expr, false).getFlattened();
			for (OWLNamedIndividual inst : insts) {
				Set<OWLClassExpression> exprs = map.get(inst);
				if (exprs == null) {
					exprs = new HashSet<>();
					map.put(inst, exprs);
				}
				exprs.add(expr);
			}			
		}	
		return map;
	}

	
	public Map<OWLNamedIndividual, Set<OWLObjectPropertyAssertionAxiom>> 
		createIndPropertyAssertionMapInferred(OWLReasoner reasoner) {
		Map<OWLNamedIndividual, Set<OWLObjectPropertyAssertionAxiom>> indRAssMap = new HashMap<>();
		Set<OWLNamedIndividual> inds = getIndividuals();
		for (OWLNamedIndividual ind : inds) {
			for (OWLObjectProperty prop : getObjectPropertiesInSignature()) {
				Set<OWLNamedIndividual> objs = 
						reasoner.getObjectPropertyValues(ind, prop).getFlattened();
				for (OWLNamedIndividual obj : objs) {
					OWLObjectPropertyAssertionAxiom fact = 
							factory.getOWLObjectPropertyAssertionAxiom(prop, ind, obj);
					Set<OWLObjectPropertyAssertionAxiom> facts = indRAssMap.get(ind);
					if (facts == null) {
						facts = new HashSet<>();
						indRAssMap.put(ind, facts);
					}
					facts.add(fact);
				}
			}
		}		
		return indRAssMap;
	}



	public static Map<OWLNamedIndividual, Map<OWLClass, OWLAxiom>> 
		mapIndividualsToClassesAndAxioms(Set<OWLAxiom> projection) {
		Map<OWLNamedIndividual, Map<OWLClass, OWLAxiom>> indAxiomMap = new HashMap<>();
		for (OWLAxiom ax : projection) {
			// always one individual and one class
			OWLClassAssertionAxiom ca = (OWLClassAssertionAxiom)ax;
			if (ca.getIndividual().isAnonymous()) {
				continue;
			}
			OWLNamedIndividual ind = ca.getIndividual().asOWLNamedIndividual();
			OWLClass cl = ca.getClassExpression().asOWLClass();
			Map<OWLClass, OWLAxiom> classAxiomMap = indAxiomMap.get(ind);
			if (classAxiomMap == null) {
				classAxiomMap = new HashMap<>();
				indAxiomMap.put(ind, classAxiomMap);
			}
			classAxiomMap.put(cl, ca);			
		}
		return indAxiomMap;
	}

	public static Map<List<OWLNamedIndividual>, Map<OWLObjectProperty, OWLAxiom>> 
		mapIndividualsToRolesAndAxioms(Set<OWLAxiom> projection) {		
		Map<List<OWLNamedIndividual>, Map<OWLObjectProperty, OWLAxiom>> indAxiomMap = new HashMap<>();
		for (OWLAxiom ax : projection) {
			OWLObjectPropertyAssertionAxiom ra = (OWLObjectPropertyAssertionAxiom)ax;
			List<OWLNamedIndividual> key = new ArrayList<>(2);
			key.add(ra.getSubject().asOWLNamedIndividual());
			key.add(ra.getObject().asOWLNamedIndividual());
			OWLObjectProperty prop = ra.getProperty().asOWLObjectProperty();
			Map<OWLObjectProperty, OWLAxiom> roleAxiomMap = indAxiomMap.get(key);
			if (roleAxiomMap == null) {
				roleAxiomMap = new HashMap<>();
				indAxiomMap.put(key, roleAxiomMap);
			}
			roleAxiomMap.put(prop, ra);		
		}	
		return indAxiomMap;
	}

	public static Map<OWLClass, Set<OWLNamedIndividual>> 
		mapClassesToIndividuals(Set<OWLAxiom> projection) {
		Map<OWLClass, Set<OWLNamedIndividual>> classIndMap = new HashMap<>();
		for (OWLAxiom ax : projection) {
			// always one individual and one class		
			OWLClassAssertionAxiom ca = (OWLClassAssertionAxiom)ax;
			if (ca.getIndividual().isAnonymous()) {
				continue;
			}
			OWLNamedIndividual ind = ca.getIndividual().asOWLNamedIndividual();
			OWLClass cl = ca.getClassExpression().asOWLClass();
			Set<OWLNamedIndividual> inds = classIndMap.get(cl);
			if (inds == null) {
				inds = new HashSet<>();
				classIndMap.put(cl, inds);
			}
			inds.add(ind);			
		}
		return classIndMap;
	}


	public static Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> 
		mapRolesToIndividuals(Set<OWLAxiom> projection) {
		Map<OWLObjectProperty, Set<List<OWLNamedIndividual>>> roleIndMap = new HashMap<>();
		for (OWLAxiom ax : projection) {
			OWLObjectPropertyAssertionAxiom ra = (OWLObjectPropertyAssertionAxiom)ax;
			List<OWLNamedIndividual> inds = new ArrayList<>(2);
			inds.add(ra.getSubject().asOWLNamedIndividual());
			inds.add(ra.getObject().asOWLNamedIndividual());
			OWLObjectProperty prop = ra.getProperty().asOWLObjectProperty();
			Set<List<OWLNamedIndividual>> insts = roleIndMap.get(prop);
			if (insts == null) {
				insts = new HashSet<>();
				roleIndMap.put(prop, insts);
			}
			insts.add(inds);
		}		
		return roleIndMap;
	}

	public Set<OWLAxiom> getAxioms() {
		return ontology.getAxioms();
	}

	public void addAnnotations(OWLOntology input) {
		Set<OWLEntity> ents = input.getSignature();
		for (OWLEntity ent : ents) {
			addAxioms(input.getAnnotationAssertionAxioms(ent.getIRI()));
		}
	}

	public void cleanDuplicateAnnotations() {
		Set<OWLEntity> ents = ontology.getSignature();
		for (OWLEntity ent : ents) {
			Set<OWLAnnotationAssertionAxiom> annots = ontology.getAnnotationAssertionAxioms(ent.getIRI());
			int count = 0;
			for (OWLAnnotationAssertionAxiom annot : annots) {
				if (count > 0) {
					removeAxiom(annot);
				}
				count++;
			}
		}
	}

	public String getLabel(OWLEntity ent) {		
		Set<OWLAnnotation> annots = ent.getAnnotations(ontology);
		if (annots == null) {
			return null;
		}		
		for (OWLAnnotation annot : annots) {
			if (annot.getProperty().isLabel()) {
				return annot.getValue().toString().replaceAll("@en", "");
			}
		}
		return null;
	}
	
	
	
	public OntologyHandler extractTopDataModule() {			
		return extractDataModule(ModuleType.TOP);
	}
	
	
	public OntologyHandler extractBotDataModule() {			
		return extractDataModule(ModuleType.BOT);
	}
	
	
	public OntologyHandler extractDataModule(ModuleType moduleType) {
		// extract the relevant module
		Out.p("\nExtracting the relevant module");		
		// remove irrelevant axioms
		removeIrrelevantAxioms();	
		Set<OWLEntity> aboxSig = getABoxSignature();
		Out.p("\nABox signature size: " + aboxSig.size() + 
				" including " + getClassesInABox().size() + " classes, " + 
				getObjectPropertiesInABox().size() + " object properties, " +
                getDataPropertiesInABox().size() + " data properties"
        );
		OntologyHandler moduleHandler = null;		
		if (aboxSig.size() > 0) {
			Set<OWLAxiom> module = OntologyHandler.extractModule(ontology, aboxSig, moduleType);
			moduleHandler = new OntologyHandler(module, getIRI());
			Out.p(moduleHandler.getOntologySize() + " out of " 
					+ getOntologySize() + " axioms are relevant");
		}		
		return moduleHandler;
	}



    public static OntologyHandler extractBotDataModule(InputStream inputStream) {
		return extractDataModule(inputStream, ModuleType.BOT);
	}
	
	
	public static OntologyHandler extractTopDataModule(InputStream inputStream) {
		return extractDataModule(inputStream, ModuleType.TOP);
	}
	
	
	public static OntologyHandler extractDataModule(InputStream inputStream,
			ModuleType moduleType) {
		Out.p("\nLoading the ontology");
		OntologyHandler handler = new OntologyHandler(inputStream);        
		Out.p("\nOntology IRI = " + handler.getIRI());				
		return handler.extractDataModule(moduleType);
	}
	
	
	public static OntologyHandler extractBotDataModule(String ontFilePath) {
		FileInputStream ontoFile = null;
		try {
			ontoFile = new FileInputStream(ontFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return OntologyHandler.extractBotDataModule(ontoFile);		
	}
	
	
	
	public static OntologyHandler extractTopDataModule(String ontFilePath) {
		FileInputStream ontoFile = null;
		try {
			ontoFile = new FileInputStream(ontFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return OntologyHandler.extractTopDataModule(ontoFile);		
	}
	
	
	

	public boolean containsNegations() {	
		return containsNegations(getLogicalAxioms());
	}
	
	
	public static boolean containsNegations(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom ax : axioms) {
			if (ax.isOfType(AxiomType.DISJOINT_CLASSES)) {
				return true;
			}
			if (AxiomMetric.containsNegations(ax)) {
			    return true;
            }
		}		
		return false;
	}
	
	
	public boolean containsUniversals() {	
		return containsUniversals(getLogicalAxioms());
	}
	
	
	public static boolean containsUniversals(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom ax : axioms) {
			if (ax.isOfType(AxiomType.OBJECT_PROPERTY_RANGE)) {
				return true;
			}
			if (AxiomMetric.containsUniversals(ax)) {
			    return true;
            }
		}		
		return false;
	}

	
	
	public boolean containsMaxRestrictions() {
		return containsMaxRestrictions(getLogicalAxioms());
	}
	
	
	
	public static boolean containsMaxRestrictions(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom ax : axioms) {			
			if (AxiomMetric.containsMaxRestrictions(ax)) {
			    return true;
            }
		}		
		return false;
	}
	
	
	
	
	public boolean containsDisjunctions() {
		return containsDisjunctions(getLogicalAxioms());
	}
	
	
	
	public static boolean containsDisjunctions(Set<? extends OWLAxiom> axioms) {
		for (OWLAxiom ax : axioms) {			
			if (AxiomMetric.containsDisjunctions(ax)) {
			    return true;
            }
		}		
		return false;
	}
	
	

	
	
	public boolean containsAxioms(Set<OWLAxiom> axioms) {		
		return getAxioms().containsAll(axioms);
	}

	
	
	public Set<String> getClassIRIs() {
		Set<String> names = new HashSet<>();
		for (OWLClass cl : getClassesInSignature()) {
			names.add(cl.getIRI().toString());
		}		
		return names;
	}

	
	
	public Set<OWLAxiom> extractModule(Set<OWLEntity> signature, 
			ModuleType moduleType) {
		return extractModule(ontology, signature, moduleType);
	}
	
	
	
	public static Set<OWLAxiom> selectAxiomsBySignature(Set<OWLEntity> signature, 
			Set<OWLAxiom> axioms) {
		Set<OWLAxiom> extrAxioms = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			if (signature.containsAll(ax.getSignature())) {
				extrAxioms.add(ax);
			}
		}		
		return extrAxioms;
	}
	

	
	public Set<OWLClass> getClassesByIDs(Collection<String> ids) {		
		Set<OWLClass> classes = new HashSet<>();
		for (String id : ids) {
			OWLClass cl = getClassByIRI(id);
			if (cl != null) {
				classes.add(cl);
			} else {
				Out.p(id + " is not found");
			}
		}
		return classes;
	}
	
	
	
	
	
	
	public void declareClassesForInstancesOf(Set<String> classIds) {		
		declareClassesForInstancesOf(getClassesByIDs(classIds));
	}

	
	public void declareClassesForInstancesOf(Collection<OWLClass> classes) {
		Set<OWLEntity> signature = new HashSet<>();
		signature.addAll(classes);
		Set<OWLAxiom> module = extractModule(signature, ModuleType.TOP);
		OntologyHandler modHandler = new OntologyHandler(module);
		Set<OWLNamedIndividual> modInds = modHandler.getIndividuals();
		Out.p("\nDeclaring classes for " + modInds.size() 
			+ " out of " + getIndividuals().size() + " individuals");
		int count = 0;
		for (OWLNamedIndividual ind : modInds) {
			if (ind.getIRI().toString().endsWith(Out.IND_SUFFIX)) {
				continue;
			}
			Set<OWLClassAssertionAxiom> cas = 
					modHandler.getClassAssertionsOfInd(ind);
			if (!containsClass(cas, classes)) {
				continue;
			}
			count++;
			IRI iri = IRI.create(ind.getIRI() + Out.CLASS_SUFFIX);
			OWLClass cl = factory.getOWLClass(iri);
			// add the class declaration and annotation
			addDeclaration(cl);
			addAnnotationsOfIndividualToClass(ind, cl);
			// add the class assertion
			addAxiom(factory.getOWLClassAssertionAxiom(cl, ind));
			// add subclass relations			
			for (OWLClassAssertionAxiom ca : cas) {
				OWLClassExpression type = ca.getClassExpression();
				if (type.isAnonymous()) {
					continue;
				}
				addAxiom(factory.getOWLSubClassOfAxiom(cl, type));
			}
		}
		Out.p(count + " additional classes are declared");
	}
	
	

	private boolean containsClass(
			Set<OWLClassAssertionAxiom> cas, Collection<OWLClass> classes) {
		for (OWLClassAssertionAxiom ca : cas) {
			if (classes.contains(ca.getClassExpression())) {
				return true;
			}
		}		
		return false;
	}
	
	

	private void addAnnotationsOfIndividualToClass(
			OWLNamedIndividual ind, OWLClass cl) {
		Set<OWLAnnotationAssertionAxiom> annots = ind.getAnnotationAssertionAxioms(ontology);
		for (OWLAnnotationAssertionAxiom annot : annots) {
			addAxiom(factory.getOWLAnnotationAssertionAxiom(
					cl.getIRI(), annot.getAnnotation()));
		}
	}

	
	
	public void labelInstancesOfClasses() {
		Set<OWLNamedIndividual> inds = getIndividuals();
		int count = 0;
		for (OWLNamedIndividual ind : inds) {
			String iri = ind.getIRI().toString();
			if (iri.endsWith(Out.IND_SUFFIX)) {
				OWLClass cl = getClassByIRI(iri.replaceAll(Out.IND_SUFFIX, ""));
				if (cl != null) {
					count++;
					Set<OWLAnnotationAssertionAxiom> annots = cl.getAnnotationAssertionAxioms(ontology);
					for (OWLAnnotationAssertionAxiom annot : annots) {
						addAxiom(factory.getOWLAnnotationAssertionAxiom(
								ind.getIRI(), annot.getAnnotation()));
					}					
				}
			}
		}
		Out.p("\nClasses are found and linked for " + count + " / " + inds.size() + " individuals");
	}

	
	
	public void setPropertyDomain(String propIRI, String domainIRI) {
		setPropertyDomain(getObjectPropertyByIRI(propIRI), getClassByIRI(domainIRI));
	}
	
	public void setPropertyRange(String propIRI, String rangeIRI) {		
		setPropertyRange(getObjectPropertyByIRI(propIRI), getClassByIRI(rangeIRI));
	}

	
	private void setPropertyDomain(OWLObjectProperty prop, OWLClass cl) {
		addAxiom(factory.getOWLObjectPropertyDomainAxiom(prop, cl));
	}

	private void setPropertyRange(OWLObjectProperty prop, OWLClass cl) {
		addAxiom(factory.getOWLObjectPropertyRangeAxiom(prop, cl));
	}

	
	
	// potentially memory costly!
	public void materialiseOptimised(
			OWLReasoner reasoner, Map<OWLClass, Set<OWLClass>> disjClassMap) {		
		Set<OWLAxiom> assertions = new HashSet<>();
		// create individual-fact maps
		Map<OWLNamedIndividual, Set<OWLClassAssertionAxiom>> indCAssMap = 
				createIndClassAssertionMap();
		Map<OWLNamedIndividual, Set<OWLObjectPropertyAssertionAxiom>> indRAssMap = 
				createIndPropertyAssertionMap();
		Map<OWLNamedIndividual, Set<OWLDataPropertyAssertionAxiom>> indDAssMap = 
				createIndDataPropertyAssertionMap();
		// create nodes
		Set<OWLNamedIndividual> inds = getIndividuals();
		for (OWLNamedIndividual ind : inds) {
			Set<OWLClassAssertionAxiom> cfacts = indCAssMap.get(ind);
			Set<OWLClassExpression> label = null;
			if (cfacts != null) {
				label = getExpressionsFromAssertions(cfacts);
				// add super classes
				label = addSuperClasses(label, reasoner);			
				// create assertions
				for (OWLClassExpression cl : label) {
					OWLClassAssertionAxiom cfactnew = 
							factory.getOWLClassAssertionAxiom(cl, ind);
					if (!cfacts.contains(cfactnew)) {
						assertions.add(cfactnew);
					}
				}
				// get disjoint classes
				if (disjClassMap != null && !disjClassMap.isEmpty()) {
					Set<OWLClass> disjCls = getDisjointClasses(label, disjClassMap);
					// create assertions
					for (OWLClass cl : disjCls) {
						OWLClassExpression negCl = factory.getOWLObjectComplementOf(cl);
						OWLClassAssertionAxiom cfactnew = 
								factory.getOWLClassAssertionAxiom(negCl, ind);
						if (!cfacts.contains(cfactnew)) {
							assertions.add(cfactnew);
						}
					}
				}
				
			}			
		}
		// create relations
		for (OWLNamedIndividual subj : indRAssMap.keySet()) {
			Set<OWLObjectPropertyAssertionAxiom> rfacts = indRAssMap.get(subj);			
			for (OWLObjectPropertyAssertionAxiom rfact : rfacts) {
				OWLIndividual anonymObj = rfact.getObject();
				if (anonymObj.isNamed()) {
					OWLNamedIndividual obj = anonymObj.asOWLNamedIndividual();
					// add super properties
					Set<OWLObjectPropertyExpression> superProps = 
							addSuperProperties(rfact.getProperty(), reasoner);
					// create assertions
					for (OWLObjectPropertyExpression prop : superProps) {						
						OWLObjectPropertyAssertionAxiom rfactnew = 
								factory.getOWLObjectPropertyAssertionAxiom(prop, subj, obj);
						if (!rfacts.contains(rfactnew)) {
							assertions.add(rfactnew);
						}
					}
					// add inverse properties
					Set<OWLObjectPropertyExpression> inverseProps = 
							addInverseProperties(rfact.getProperty(), reasoner);
					for (OWLObjectPropertyExpression prop : inverseProps) {						
						OWLObjectPropertyAssertionAxiom rfactnew = 
								factory.getOWLObjectPropertyAssertionAxiom(prop, obj, subj);
						if (!rfacts.contains(rfactnew)) {
							assertions.add(rfactnew);
						}
					}
					// add domains for the subject
					Set<OWLClass> domains = getPropertyDomains(
							rfact.getProperty(), reasoner);
					// create assertions
					for (OWLClass cl : domains) {
						if (cl.isOWLThing()) {
							continue;
						}
						OWLClassAssertionAxiom cfactnew = 
								factory.getOWLClassAssertionAxiom(cl, subj);
						Set<OWLClassAssertionAxiom> cfacts = indCAssMap.get(subj);
						if (cfacts == null || !cfacts.contains(cfactnew)) {
							assertions.add(cfactnew);
						}
					}
					// add ranges for the object
					Set<OWLClass> ranges = getPropertyRanges(
							rfact.getProperty(), reasoner);
					// create assertions
					for (OWLClass cl : ranges) {
						if (cl.isOWLThing()) {
							continue;
						}
						OWLClassAssertionAxiom cfactnew = 
								factory.getOWLClassAssertionAxiom(cl, obj);
						Set<OWLClassAssertionAxiom> cfacts = indCAssMap.get(obj);
						if (cfacts == null || !cfacts.contains(cfactnew)) {
							assertions.add(cfactnew);
						}
					}
				}
			}
		}
		// add data property domains
		for (OWLNamedIndividual subj : indDAssMap.keySet()) {
			Set<OWLDataPropertyAssertionAxiom> rfacts = indDAssMap.get(subj);			
			for (OWLDataPropertyAssertionAxiom rfact : rfacts) {				
				// add domains for the subject
				OWLDataPropertyExpression propExpr = rfact.getProperty();
				if (propExpr.isAnonymous()) {
					continue;
				}
				OWLDataProperty prop = propExpr.asOWLDataProperty();
				Set<OWLClass> domains = getDataPropertyDomains(
						prop, reasoner);
				// create assertions
				for (OWLClass cl : domains) {
					if (cl.isOWLThing()) {
						continue;
					}
					OWLClassAssertionAxiom cfactnew = 
							factory.getOWLClassAssertionAxiom(cl, subj);
					Set<OWLClassAssertionAxiom> cfacts = indCAssMap.get(subj);
					if (cfacts == null || !cfacts.contains(cfactnew)) {
						assertions.add(cfactnew);
					}
				}
			}
		}		
		Out.p("ABox axioms = " + getOntologySize());
		addAxioms(assertions);
		Out.p("ABox axioms after materialisation = " + getOntologySize());
	}
	
	
		
	public void materialise(ALCOperator operator) {		
		Set<OWLAxiom> assertions = new HashSet<>();				
		// create atomic class assertions
		Map<OWLClassExpression, Set<OWLNamedIndividual>> clInstMap = operator.getClassInstanceMap();	
		for (OWLClassExpression cl : clInstMap.keySet()) {
			Set<OWLNamedIndividual> insts = clInstMap.get(cl);
			if (insts == null) {
				continue;
			}			
			for (OWLNamedIndividual inst : insts) {				
				OWLClassAssertionAxiom cfact = factory.getOWLClassAssertionAxiom(cl, inst);
				assertions.add(cfact);
			}
		}		
		// create property assertions
		Map<OWLNamedIndividual, Set<OWLObjectPropertyAssertionAxiom>> indRAssMap = 
				createIndPropertyAssertionMap();		
		for (OWLNamedIndividual subj : indRAssMap.keySet()) {
			Set<OWLObjectPropertyAssertionAxiom> rfacts = indRAssMap.get(subj);			
			for (OWLObjectPropertyAssertionAxiom rfact : rfacts) {
				OWLIndividual anonymObj = rfact.getObject();
				if (anonymObj.isNamed()) {
					OWLNamedIndividual obj = anonymObj.asOWLNamedIndividual();
					// add super properties
					Set<OWLObjectPropertyExpression> superProps = 
							addSuperProperties(rfact.getProperty(), operator);
					// create assertions
					for (OWLObjectPropertyExpression prop : superProps) {						
						OWLObjectPropertyAssertionAxiom rfactnew = 
								factory.getOWLObjectPropertyAssertionAxiom(prop, subj, obj);
						if (!rfacts.contains(rfactnew)) {
							assertions.add(rfactnew);
						}
					}
					// add inverse properties
					Set<OWLObjectPropertyExpression> inverseProps = 
							operator.getInverseObjectProperties(rfact.getProperty());
					if (inverseProps != null) {
						for (OWLObjectPropertyExpression prop : inverseProps) {						
							OWLObjectPropertyAssertionAxiom rfactnew = 
									factory.getOWLObjectPropertyAssertionAxiom(prop, obj, subj);
							if (!rfacts.contains(rfactnew)) {
								assertions.add(rfactnew);
							}
						}
					}
				}
			}
		}
		// create data property assertions?
		Out.p("ABox axioms = " + getOntologySize());
		addAxioms(assertions);
		Out.p("ABox axioms after materialisation = " + getOntologySize());
	}




    private Set<OWLClass> getDisjointClasses(Set<OWLClassExpression> cls, 
			Map<OWLClass, Set<OWLClass>> disjClassMap) {
		Set<OWLClass> disjCls = new HashSet<>();
		for (OWLClassExpression cl : cls) {
			disjCls.addAll(disjClassMap.get(cl));
		}		
		disjCls.remove(factory.getOWLThing());
		disjCls.remove(factory.getOWLNothing());
		return disjCls;
	}
	
	

	private Set<OWLClassExpression> addSuperClasses(Set<OWLClassExpression> cls, 
			OWLReasoner reasoner) {
		Set<OWLClassExpression> extCls = new HashSet<>();
		for (OWLClassExpression cl : cls) {
			extCls.addAll(reasoner.getEquivalentClasses(cl).getEntities());
			extCls.addAll(reasoner.getSuperClasses(cl, false).getFlattened());
		}
		extCls.remove(factory.getOWLThing());
		extCls.remove(factory.getOWLNothing());
		return extCls;
	}
	
	
	
	
	private Set<OWLObjectPropertyExpression> addSuperProperties(
			OWLObjectPropertyExpression property, ALCOperator operator) {
		Set<OWLObjectPropertyExpression> extProps = new HashSet<>();
		Set<OWLObjectPropertyExpression> eqProps = operator.getEquivalentObjectProperties(property);
		if (eqProps != null) {
			extProps.addAll(eqProps);
		}
		Set<OWLObjectPropertyExpression> superProps = operator.getSuperObjectProperties(property);
		if (superProps != null) {
			extProps.addAll(superProps);
		}
		return extProps;
	}
	
	
	
	private Set<OWLObjectPropertyExpression> addSuperProperties(
			OWLObjectPropertyExpression property, OWLReasoner reasoner) {
		Set<OWLObjectPropertyExpression> extProps = new HashSet<>();
		extProps.addAll(reasoner.getEquivalentObjectProperties(property).getEntities());
		extProps.addAll(reasoner.getSuperObjectProperties(property, false).getFlattened());
		extProps.remove(factory.getOWLTopObjectProperty());
		extProps.remove(factory.getOWLBottomObjectProperty());
		return extProps;
	}


    private Set<OWLDataProperty> addSuperProperties(
            OWLDataProperty property, OWLReasoner reasoner) {
        Set<OWLDataProperty> extProps = new HashSet<>();
        extProps.addAll(reasoner.getEquivalentDataProperties(property).getEntities());
        extProps.addAll(reasoner.getSuperDataProperties(property, false).getFlattened());
        extProps.remove(factory.getOWLTopDataProperty());
        extProps.remove(factory.getOWLBottomDataProperty());
        return extProps;
    }
	
	
	private Set<OWLObjectPropertyExpression> addInverseProperties(
			OWLObjectPropertyExpression property, OWLReasoner reasoner) {
		Set<OWLObjectPropertyExpression> extProps = new HashSet<>();
		extProps.addAll(reasoner.getInverseObjectProperties(property).getEntities());		
		extProps.remove(factory.getOWLTopObjectProperty());
		extProps.remove(factory.getOWLBottomObjectProperty());
		return extProps;
	}
	
	
		
	private Set<OWLClass> getDataPropertyDomains(
			OWLDataProperty property, OWLReasoner reasoner) {
		return reasoner.getDataPropertyDomains(property, false).getFlattened();
	}
	
	
	
	private Set<OWLClass> getPropertyDomains(
			OWLObjectPropertyExpression property, OWLReasoner reasoner) {
		return reasoner.getObjectPropertyDomains(property, false).getFlattened();
	}
	
	
	private Set<OWLClass> getPropertyRanges(
			OWLObjectPropertyExpression property, OWLReasoner reasoner) {
		return reasoner.getObjectPropertyRanges(property, false).getFlattened();
	}

	
	
	public void removePropertyDomains() {
		Set<OWLAxiom> domainAxioms = new HashSet<>();
		for (OWLAxiom ax : getTBoxAxioms()) {
			if (ax.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
				domainAxioms.add(ax);
			}
		}
		removeAxioms(domainAxioms);
	}
	
	
	public void removePropertyRanges() {
		Set<OWLAxiom> rangeAxioms = new HashSet<>();
		for (OWLAxiom ax : getTBoxAxioms()) {
			if (ax.isOfType(AxiomType.OBJECT_PROPERTY_RANGE)) {
				rangeAxioms.add(ax);
			}
		}
		removeAxioms(rangeAxioms);
	}

	
	public Set<OWLAxiom> removeClassAssertionsOfInds(Collection<OWLNamedIndividual> removals) {
		Set<OWLAxiom> removedAxioms = new HashSet<>();
		for (OWLAxiom ax : getABoxAxioms()) {
			if (ax instanceof OWLClassAssertionAxiom) {
				for (OWLNamedIndividual rem : removals) {
					if (ax.getIndividualsInSignature().contains(rem)) {
						removedAxioms.add(ax);
						break;
					}
				}
			}
		}
		removeAxioms(removedAxioms);
		return removedAxioms;
	}
	
	
	

	public Set<OWLAxiom> removePropertyAssertionsOfInds(Collection<OWLNamedIndividual> removals) {
		Set<OWLAxiom> removedAxioms = new HashSet<>();
		for (OWLAxiom ax : getABoxAxioms()) {
			if (ax instanceof OWLObjectPropertyAssertionAxiom) {
				for (OWLNamedIndividual rem : removals) {
					if (ax.getIndividualsInSignature().contains(rem)) {
						removedAxioms.add(ax);
						break;
					}
				}
			}
		}
		removeAxioms(removedAxioms);
		return removedAxioms;
	}


	
	
	public void addHypotheses(Collection<Hypothesis> hypotheses) {
		for (Hypothesis h : hypotheses) {
			addAxioms(h.axioms);
		}
	}

	
	public Set<OWLAxiom> removeClassAssertionsOfClass(OWLClassExpression expr) {
		Set<OWLAxiom> removedAxioms = new HashSet<>();
		for (OWLAxiom ax : getABoxAxioms()) {			
			if (ax instanceof OWLClassAssertionAxiom) {
				OWLClassAssertionAxiom axiom = (OWLClassAssertionAxiom) ax;
				if (axiom.getClassExpression().equals(expr)) {
					removedAxioms.add(axiom);
				}
			}
		}
		removeAxioms(removedAxioms);
		return removedAxioms;
	}
	
	

	public Set<OWLAxiom> removeClassAssertionsOfClasses(
			Set<? extends OWLClassExpression> cls) {
		Set<OWLAxiom> removedAxioms = new HashSet<>();
		for (OWLClassExpression cl : cls) {
			removedAxioms.addAll(removeClassAssertionsOfClass(cl));
		}
		return removedAxioms;
	}
	
	
	

	public static void saveAxioms(Set<OWLAxiom> axioms, File file) {
		OntologyHandler handler = new OntologyHandler();
		handler.addAxioms(axioms);
		handler.saveOntology(file);
	}

	
	
	
	public Set<OWLAxiom> getDisjointnessAxioms() {
		Set<OWLAxiom> disjAxioms = new HashSet<>();
		for (OWLAxiom ax : getTBoxAxioms()) {
			if (ax instanceof OWLDisjointClassesAxiom) {
				disjAxioms.add(ax);
			}
		}	
		return disjAxioms;
	}

	
	public static Set<OWLAxiom> getEntailedAxioms(
			Set<OWLAxiom> axioms1, Set<OWLAxiom> axioms2, ReasonerName reasonerName) {
		Out.p("\nChecking entailed axioms: " + axioms1.size() + " axioms entail ? out of " + axioms2.size() + " axioms");
		Set<OWLAxiom> entAxioms = new HashSet<>();
		OntologyHandler handler = new OntologyHandler(axioms1);
		handler = new OntologyHandler(handler.extractModule(
				OntologyHandler.getSignature(axioms2), ModuleType.BOT));
		try {
			Out.p("\nInitialising the reasoner");
			OWLReasoner reasoner = ReasonerLoader.initReasoner(reasonerName, handler.getOntology());
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			Out.p("\nChecking entailments");
			int count = 0;
			for (OWLAxiom ax : axioms2) {
				if (reasoner.isEntailed(ax)) {
					entAxioms.add(ax);
				}
				if (++count % 10 == 0) {
					Out.p(count + " axioms checked");
				}
			}
			reasoner.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entAxioms;
	}
	
	
	private static Map<Set<OWLAxiom>, Set<OWLAxiom>> mapAxiomsToModules(
			Set<OWLAxiom> axioms1, Set<OWLAxiom> axioms2, boolean useOptimised) {
		OntologyHandler handler = new OntologyHandler(axioms1);
		Out.p("\nExtracting modules");
		Map<OWLAxiom, Set<OWLAxiom>> axiomModuleMap = new HashMap<>();
		for (OWLAxiom ax : axioms2) {
			Set<OWLAxiom> axModule = null;
			if (useOptimised) {
				axModule = selectAxiomsBySignature(ax.getSignature(), axioms1);
			} else {
				axModule = handler.extractModule(ax.getSignature(), ModuleType.BOT);
			}
			axiomModuleMap.put(ax, axModule);
		}
		Map<Set<OWLAxiom>, Set<OWLAxiom>> moduleAxiomsMap = new HashMap<>();
		for (OWLAxiom ax1 : axiomModuleMap.keySet()) {
			Set<OWLAxiom> axModule1 = axiomModuleMap.get(ax1);
			boolean isSubsumed = false;
			for (OWLAxiom ax2 : axiomModuleMap.keySet()) {
				if (ax1.equals(ax2)) {
					continue;
				}
				Set<OWLAxiom> axModule2 = axiomModuleMap.get(ax2);
				if (!axModule2.equals(axModule1) && axModule2.containsAll(axModule1)) {
					isSubsumed = true;
				}				
			}
			if (!isSubsumed) {
				Set<OWLAxiom> modAxioms = moduleAxiomsMap.get(axModule1);
				if (modAxioms == null) {
					modAxioms = new HashSet<>();
					moduleAxiomsMap.put(axModule1, modAxioms);
				}
				modAxioms.add(ax1);				
			}
		}
		// add axioms with subsumed modules
		for (OWLAxiom ax1 : axiomModuleMap.keySet()) {
			Set<OWLAxiom> axModule1 = axiomModuleMap.get(ax1);
			for (Set<OWLAxiom> axModule2 : moduleAxiomsMap.keySet()) {
				if (axModule2.containsAll(axModule1)) {
					Set<OWLAxiom> modAxioms2 = moduleAxiomsMap.get(axModule2);
					modAxioms2.add(ax1);
				}
			}
		}
		Out.p(moduleAxiomsMap.size() + " modules for " + axioms2.size() + " axioms");
		return moduleAxiomsMap;
	}
	
	
		
	public static List<Set<OWLAxiom>> getEntailedAxiomsUsingModules(
			Set<OWLAxiom> axioms1, Set<OWLAxiom> axioms2, 
			Set<OWLAxiom> tboxAxioms, boolean useOptimised, 
			ReasonerName reasonerName) {
		Out.p("\nChecking entailed axioms: " + axioms1.size() + " axioms entail ? out of " + axioms2.size() + " axioms");
		Set<OWLAxiom> entailedAxioms = new HashSet<>();
		Set<OWLAxiom> entailedTBoxAxioms = new HashSet<>();
		Set<OWLAxiom> entailingAxioms = new HashSet<>();		
		OntologyHandler modHandler = new OntologyHandler(tboxAxioms);
		Out.p("\nInitialising the reasoner");
		OWLReasoner modReasoner = null;
		try {
			modReasoner = ReasonerLoader.initReasoner(reasonerName, modHandler.getOntology());
			modReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.OBJECT_PROPERTY_HIERARCHY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Out.p("\nChecking entailments");
		for (OWLAxiom ax2 : axioms2) {
			if (modReasoner.isEntailed(ax2)) {
				entailedAxioms.add(ax2);
				entailedTBoxAxioms.add(ax2);
			}
		}		
		Out.p("\nChecking hits");
		Set<OWLAxiom> unentailedAxioms = new HashSet<>(axioms2);
		unentailedAxioms.removeAll(entailedAxioms);
		Map<Set<OWLAxiom>, Set<OWLAxiom>> moduleAxiomsMap = mapAxiomsToModules(axioms1, unentailedAxioms, useOptimised);
		// check entailments using modules
		int count = 0;
		for (Set<OWLAxiom> axModule : moduleAxiomsMap.keySet()) {
			Out.p(++count + " / " + moduleAxiomsMap.size() + " modules checked");
			Set<OWLAxiom> modAxioms = moduleAxiomsMap.get(axModule);
			// first do easy checks
			boolean allEntailed = true;
			for (OWLAxiom ax2 : modAxioms) {					
				if (!entailedAxioms.contains(ax2)) {
					if (axModule.contains(ax2)) {				
						entailedAxioms.add(ax2);
						entailingAxioms.add(ax2);
					} else {
						allEntailed = false;
					}
				}					
			}
			if (allEntailed) {
				continue;
			}
			// check remaining axioms using a reasoner
			try {				
				modHandler.addAxioms(axModule);
				modReasoner.flush();
				if (!modReasoner.isConsistent()) {
					Out.p("Inconsistent: repairing");
					modHandler.removeAxioms(axModule);
					modReasoner.flush();
					List<OWLAxiom> sortedAxioms = new LinkedList<>(axModule);
					Collections.sort(sortedAxioms, new AxiomLengthComparator(SortingOrder.ASC));
					for (OWLAxiom ax : sortedAxioms) {
						modHandler.addAxiom(ax);
						modReasoner.flush();
						if (!modReasoner.isConsistent()) {
							modHandler.removeAxiom(ax);
						}
					}						
				}
				modReasoner.flush();
				for (OWLAxiom ax2 : modAxioms) {
					if (entailedAxioms.contains(ax2)) {
						continue;
					}					
					if (modReasoner.isEntailed(ax2)) {
						entailedAxioms.add(ax2);
						// try to minimise the entailing set
						Set<OWLAxiom> minEntailingAxioms = new HashSet<>();
						List<OWLAxiom> sortedEntailingAxioms = new LinkedList<>(axModule);
						sortedEntailingAxioms.retainAll(modHandler.getAxioms());
						Collections.sort(sortedEntailingAxioms, new AxiomLengthComparator(SortingOrder.DESC));
						for (OWLAxiom ax1 : sortedEntailingAxioms) {
							modHandler.removeAxiom(ax1);
							modReasoner.flush();
							if (!modReasoner.isEntailed(ax2)) {
								modHandler.addAxiom(ax1);
								minEntailingAxioms.add(ax1);
							}
						}						
						entailingAxioms.addAll(minEntailingAxioms);
					}
				}
				// check removed axioms if inconsistent
				Set<OWLAxiom> removedAxioms = new HashSet<>(axModule);
				removedAxioms.removeAll(modHandler.getAxioms());
				if (!removedAxioms.isEmpty()) {
					modHandler.removeAxioms(axModule);					
					for (OWLAxiom ax : removedAxioms) {
						modHandler.addAxiom(ax);
						modReasoner.flush();
						if (!modReasoner.isConsistent()) {
							modHandler.removeAxiom(ax);
						}
					}					
					modReasoner.flush();
					// make a stronger set
					List<OWLAxiom> sortedAxioms = new LinkedList<>(axModule);
					sortedAxioms.removeAll(removedAxioms);
					Collections.sort(sortedAxioms, new AxiomLengthComparator(SortingOrder.ASC));
					for (OWLAxiom ax : sortedAxioms) {
						modHandler.addAxiom(ax);
						modReasoner.flush();
						if (!modReasoner.isConsistent()) {
							modHandler.removeAxiom(ax);
						}
					}
					modReasoner.flush();
					// check axioms					
					for (OWLAxiom ax2 : modAxioms) {
						if (entailedAxioms.contains(ax2)) {
							continue;
						}					
						if (modReasoner.isEntailed(ax2)) {
							entailedAxioms.add(ax2);
							// try to minimise the entailing set
							Set<OWLAxiom> minEntailingAxioms = new HashSet<>();
							List<OWLAxiom> sortedEntailingAxioms = new LinkedList<>(axModule);
							sortedEntailingAxioms.retainAll(modHandler.getAxioms());
							Collections.sort(sortedEntailingAxioms, new AxiomLengthComparator(SortingOrder.DESC));
							for (OWLAxiom ax1 : sortedEntailingAxioms) {
								modHandler.removeAxiom(ax1);
								modReasoner.flush();
								if (!modReasoner.isEntailed(ax2)) {
									modHandler.addAxiom(ax1);
									minEntailingAxioms.add(ax1);
								}
							}						
							entailingAxioms.addAll(minEntailingAxioms);
						}
					}
				}									
				modHandler.removeAxioms(axModule);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// try to minimise the entailing set
		Set<OWLAxiom> minEntailingAxioms = new HashSet<>();
		modHandler.addAxioms(entailingAxioms);
		modReasoner.flush();
		if (modReasoner.isConsistent()) {
			Out.p("\nMinimising the entailing set");
			// find the minimal set
			List<OWLAxiom> sortedEntailingAxioms = new LinkedList<>(entailingAxioms);
			Collections.sort(sortedEntailingAxioms, new AxiomLengthComparator(SortingOrder.DESC));
			count = 0;			
			for (OWLAxiom ax1 : sortedEntailingAxioms) {
				modHandler.removeAxiom(ax1);
				modReasoner.flush();
				if (!modReasoner.isEntailed(entailedAxioms)) {
					modHandler.addAxiom(ax1);
					minEntailingAxioms.add(ax1);
				}	
				Out.p(++count + " / " + sortedEntailingAxioms.size() + " checked");
			}
		} else {
			minEntailingAxioms = entailingAxioms;
		}
		modReasoner.dispose();
		// discard TBox axioms
		minEntailingAxioms.retainAll(axioms1);
		List<Set<OWLAxiom>> resList = new ArrayList<>();
		resList.add(minEntailingAxioms);
		resList.add(entailedAxioms);
		resList.add(entailedTBoxAxioms);
		return resList;
	}

	
	
	/*public static Set<OWLAxiom> getEntailingAxiomsUsingModules(
			Set<OWLAxiom> axioms1, Set<OWLAxiom> axioms2, boolean useOptimised) {
		Out.p("\nFinding the minimal entailing set of axioms");
		OntologyHandler handler = new OntologyHandler(axioms1);
		Set<OWLEntity> sig2 = getSignature(axioms2);
		Set<OWLAxiom> entailingAxioms = null;
		if (useOptimised) {
			entailingAxioms = selectAxiomsBySignature(sig2, axioms1);
		} else {
			entailingAxioms = handler.extractModule(sig2, ModuleType.BOT);
		}
		handler = new OntologyHandler(entailingAxioms);
		Out.p("\nInitialising the reasoner");
		OWLReasoner reasoner = null;
		try {
			reasoner = ReasonerLoader.initReasoner(handler.getOntology());
			if (!reasoner.isConsistent()) {
				handler.removeInconsistency(reasoner);
			}
			reasoner.flush();
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);			
			if (!reasoner.isEntailed(axioms2)) {
				handler = new OntologyHandler(entailingAxioms);
				handler.removeInconsistencyByLength(reasoner);
			}
			reasoner.flush();
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			if (!reasoner.isEntailed(axioms2)) {
				return getEntailingInconsistentAxiomsUsingModules(entailingAxioms, axioms2);
			}
			List<OWLAxiom> sortedEntailingAxioms = new LinkedList<>(entailingAxioms);
			Collections.sort(sortedEntailingAxioms, new AxiomLengthComparator(SortingOrder.DESC));
			int count = 0;
			for (OWLAxiom ax : sortedEntailingAxioms) {
				handler.removeAxiom(ax);
				reasoner.flush();
				if (!reasoner.isEntailed(axioms2)) {
					handler.addAxiom(ax);
				}
				Out.p(++count + " / " + sortedEntailingAxioms.size() + " axioms checked");
			}
			reasoner.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return handler.getAxioms();
	}*/

	
	
	
	
	/*private void removeInconsistencyByLength(OWLReasoner reasoner) {
		removeInconsistency(reasoner, true);
	}*/
	
	
	

	/*private static Set<OWLAxiom> getEntailingInconsistentAxiomsUsingModules(
			Set<OWLAxiom> axioms1, Set<OWLAxiom> axioms2) {
		Set<OWLAxiom> entAxioms = new HashSet<>();
		Map<Set<OWLAxiom>, Set<OWLAxiom>> moduleAxiomsMap = mapAxiomsToModules(axioms1, axioms2, true);
		// check entailments using modules
		OntologyHandler modHandler = new OntologyHandler();		
		Out.p("\nInitialising the reasoner");
		OWLReasoner reasoner = null;
		try {
			reasoner = ReasonerLoader.initReasoner(modHandler.getOntology());
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Out.p("\nChecking entailments");
		int count = 0;
		for (Set<OWLAxiom> axModule : moduleAxiomsMap.keySet()) {	
			try {
				Out.p(++count + " / " + moduleAxiomsMap.size() + " modules checked");
				modHandler.addAxioms(axModule);
				reasoner.flush();
				if (!reasoner.isConsistent()) {
					modHandler.removeInconsistency(reasoner);
				}
				reasoner.flush();
				Set<OWLAxiom> modAxioms = moduleAxiomsMap.get(axModule);
				if (reasoner.isEntailed(modAxioms)) {
					// minimise
					List<OWLAxiom> sortedEntailingAxioms = new LinkedList<>(axModule);
					Collections.sort(sortedEntailingAxioms, new AxiomLengthComparator(SortingOrder.DESC));
					for (OWLAxiom ax : sortedEntailingAxioms) {
						modHandler.removeAxiom(ax);
						reasoner.flush();
						if (!reasoner.isEntailed(axioms2)) {
							modHandler.addAxiom(ax);
						}						
					}					
					entAxioms.addAll(modHandler.getAxioms());
				} else {
					// check removed axioms
					Set<OWLAxiom> incAxioms = new HashSet<>(axModule);
					incAxioms.removeAll(modHandler.getAxioms());
					modHandler.removeAxioms();
					if (incAxioms.isEmpty()) {
						continue;
					}
					modHandler.addAxioms(incAxioms);					
					reasoner.flush();
					if (reasoner.isEntailed(modAxioms)) {
						// minimise
						List<OWLAxiom> sortedEntailingAxioms = new LinkedList<>(incAxioms);
						Collections.sort(sortedEntailingAxioms, new AxiomLengthComparator(SortingOrder.DESC));
						for (OWLAxiom ax : sortedEntailingAxioms) {
							modHandler.removeAxiom(ax);
							reasoner.flush();
							if (!reasoner.isEntailed(axioms2)) {
								modHandler.addAxiom(ax);
							}						
						}					
						entAxioms.addAll(modHandler.getAxioms());
					}
				}				
				modHandler.removeAxioms();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}			
		reasoner.dispose();
		return entAxioms;
	}*/
	
	

	public int countDisjointnessAxioms() {
		int count = 0;
		for (OWLAxiom ax : getTBoxAxioms()) {
			if (ax.isOfType(AxiomType.DISJOINT_CLASSES)) {
				count++;
			}
		}		
		return count;
	}

	
	
	public Set<OWLClassExpression> getSubExpressions() {
		Set<OWLClassExpression> subExprs = new HashSet<>();
		for (OWLAxiom ax : getLogicalAxioms()) {
			subExprs.addAll(ax.getNestedClassExpressions());
		}		
		return subExprs;
	}

	
	
	public Set<OWLClassExpression> generateRandomClassExpressions(int number) {
		Set<OWLClassExpression> exprs = new HashSet<>();
		List<OWLClass> clList = new ArrayList<>(getClassesInSignature());
		List<OWLObjectProperty> propList = new ArrayList<>(getObjectPropertiesInSignature());
		for (int i=0; i<number; i++) {
			// pick two classes
			int ind1 = (int)(Math.random() * clList.size());
			OWLClass cl1 = clList.get(ind1);
			int ind2 = (int)(Math.random() * clList.size());
			OWLClass cl2 = clList.get(ind2);
			int ind3 = (int)(Math.random() * propList.size());
			OWLObjectProperty prop = propList.get(ind3);
			boolean isRelational = false;
			if (Math.random() < 0.5) {
				isRelational = true;
			}
			if (!isRelational) {
				// generate propositional
				boolean isConj = false;
				if (Math.random() < 0.5) {
					// conjunction
					isConj = true;
				}
				// negations
				int pos = 0;
				double ran = Math.random();
				if (ran >= 0.25 && ran < 0.5) {
					pos = 1;
				}
				if (ran >= 0.5 && ran < 0.75) {
					pos = 2;
				}
				if (ran >= 0.75 && ran < 1) {
					pos = 3;
				}
				OWLClassExpression expr1 = null;
				OWLClassExpression expr2 = null;
				switch (pos) {
				case 0:  expr1 = cl1;
					expr2 = cl2;
				break;
				case 1:  expr1 = factory.getOWLObjectComplementOf(cl1);
					expr2 = cl2;
				break;
				case 2:  expr1 = cl1;
					expr2 = factory.getOWLObjectComplementOf(cl2);
				break;
				case 3: expr1 = factory.getOWLObjectComplementOf(cl1);
					expr2 = factory.getOWLObjectComplementOf(cl2);
				break;
		        }
				if (isConj) {
					exprs.add(factory.getOWLObjectIntersectionOf(expr1, expr2));
				} else {
					exprs.add(factory.getOWLObjectUnionOf(expr1, expr2));
				}
			} else {
				OWLClassExpression negExpr = cl1;
				if (Math.random() < 0.5) {
					negExpr = factory.getOWLObjectComplementOf(cl1);
				}
				if (Math.random() < 0.5) {
					exprs.add(factory.getOWLObjectSomeValuesFrom(prop, negExpr));
				} else {
					exprs.add(factory.getOWLObjectAllValuesFrom(prop, negExpr));
				}
			}
		}
		return exprs;
	}

	
	
	
	public void removeUselessAnnotations() {
		Set<OWLLogicalAxiom> logAxioms = getLogicalAxioms();
		Set<OWLEntity> logEnts = getSignature(logAxioms);		
		Set<OWLAnnotationAssertionAxiom> logAnnots = new HashSet<>();
		for (OWLEntity ent : logEnts) {
			logAnnots.addAll(ontology.getAnnotationAssertionAxioms(ent.getIRI()));			
		}
		Set<OWLAxiom> axioms = new HashSet<>(getAxioms());
		axioms.removeAll(logAxioms);
		axioms.removeAll(logAnnots);
		removeAxioms(axioms);
	}

	
	
	
	public void applyCWA(OWLReasoner reasoner) {
		Out.p("\nApplying CWA");
		Set<OWLAxiom> assertions = new HashSet<>();
		Set<OWLClass> cls = getClassesInSignature();
		Map<OWLClass, Set<OWLNamedIndividual>> classInstanceMap = new HashMap<>();
		int count = 0;
		for (OWLClass cl : cls) {
			Set<OWLNamedIndividual> insts = reasoner.getInstances(cl, false).getFlattened();			
			if (!insts.isEmpty()) {
				classInstanceMap.put(cl, insts);				
			}
			Out.p(++count + " / " + cls.size() + " classes are checked for instances");
		}
		for (OWLClass cl1 : classInstanceMap.keySet()) {
			Set<OWLNamedIndividual> insts1 = classInstanceMap.get(cl1);
			if (insts1 == null) {
				continue;
			}
			for (OWLClass cl2 : classInstanceMap.keySet()) {
				if (cl1.equals(cl2)) {
					continue;
				}
				Set<OWLNamedIndividual> insts2 = classInstanceMap.get(cl2);
				if (insts2 == null) {
					continue;
				}
				if (HypothesisEvaluator.countIntersection(insts1, insts2) == 0) {
					for (OWLNamedIndividual inst1 : insts1) {
						OWLAxiom negAsser = factory.getOWLClassAssertionAxiom(
								factory.getOWLObjectComplementOf(cl2), inst1);
						assertions.add(negAsser);
					}
					for (OWLNamedIndividual inst2 : insts2) {
						OWLAxiom negAsser = factory.getOWLClassAssertionAxiom(
								factory.getOWLObjectComplementOf(cl1), inst2);
						assertions.add(negAsser);
					}
				}				
			}
		}
		addAxioms(assertions);
	}



    public boolean containsDataProperties() {
        return containsDataProperties(getLogicalAxioms());
    }


    public static boolean containsDataProperties(Set<? extends OWLAxiom> axioms) {
        for (OWLAxiom ax : axioms) {
            if (ax.isOfType(AxiomType.DATA_PROPERTY_ASSERTION)) {
                return true;
            }
        }
        return false;
    }


}
