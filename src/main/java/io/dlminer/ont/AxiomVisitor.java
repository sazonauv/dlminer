package io.dlminer.ont;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

public class AxiomVisitor extends OWLAxiomVisitorAdapter {
	
	public Set<OWLAxiom> SubClassOfAxioms;
	public Set<OWLAxiom> DisjointClassesAxioms;
	public Set<OWLAxiom> EquivalentClassesAxioms;
	public Set<OWLAxiom> AsymmetricObjectPropertyAxioms;
	public Set<OWLAxiom> SymmetricObjectPropertyAxioms;
	public Set<OWLAxiom> InverseObjectPropertiesAxioms;
	public Set<OWLAxiom> InverseFunctionalObjectPropertyAxioms;
	public Set<OWLAxiom> FunctionalObjectPropertyAxioms;
	public Set<OWLAxiom> TransitiveObjectPropertyAxioms;
	public Set<OWLAxiom> FunctionalDataPropertyAxioms;
	public Set<OWLAxiom> DataPropertyRangeAxioms;
	public Set<OWLAxiom> DataPropertyDomainAxioms;
	public Set<OWLAxiom> ObjectPropertyDomainAxioms;
	public Set<OWLAxiom> ObjectPropertyRangeAxioms;
	public Set<OWLAxiom> SubObjectPropertyOfAxioms; 
	public Set<OWLAxiom> SubDataPropertyOfAxioms;
	public Set<OWLAxiom> ReflexiveObjectPropertyAxioms;
	public Set<OWLAxiom> EquivalentObjectPropertiesAxioms;
	public Set<OWLAxiom> EquivalentDataPropertiesAxioms;
	public Set<OWLAxiom> DisjointObjectPropertiesAxioms;
	public Set<OWLAxiom> DisjointDataPropertiesAxioms;
	
	public Set<OWLAxiom> DifferentIndividualsAxioms;
	public Set<OWLAxiom> SameIndividualAxioms;
	
	public Set<OWLClassAssertionAxiom> ClassAssertionAxioms;
	public Set<OWLObjectPropertyAssertionAxiom> ObjectPropertyAssertionAxioms;
	public Set<OWLAxiom> DataPropertyAssertionAxioms;
	
	public Set<OWLAxiom> DisjointUnionAxioms;
	public Set<OWLAxiom> HasKeyAxioms;
	public Set<OWLAxiom> IrreflexiveObjectPropertyAxioms;
	public Set<OWLAxiom> NegativeDataPropertyAssertionAxioms;
	public Set<OWLAxiom> NegativeObjectPropertyAssertionAxioms;
	public Set<OWLAxiom> SubPropertyChainOfAxioms;
	public Set<OWLAxiom> SWRLRules;
	
		
	public AxiomVisitor() {
		init();
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {		
		SubClassOfAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		DisjointClassesAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		EquivalentClassesAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		AsymmetricObjectPropertyAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		SymmetricObjectPropertyAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		InverseObjectPropertiesAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		InverseFunctionalObjectPropertyAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		FunctionalObjectPropertyAxioms.add(axiom);
	}
		
	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		TransitiveObjectPropertyAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		FunctionalDataPropertyAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		DataPropertyRangeAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		DataPropertyDomainAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		ObjectPropertyDomainAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		ObjectPropertyRangeAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		SubObjectPropertyOfAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		SubDataPropertyOfAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		ReflexiveObjectPropertyAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		EquivalentObjectPropertiesAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		EquivalentDataPropertiesAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		DisjointObjectPropertiesAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		DisjointDataPropertiesAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		DifferentIndividualsAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLSameIndividualAxiom axiom) {		
		SameIndividualAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		ClassAssertionAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		ObjectPropertyAssertionAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		DataPropertyAssertionAxioms.add(axiom);
	}
	
	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		DisjointUnionAxioms.add(axiom);
	}
			
	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		HasKeyAxioms.add(axiom);
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		IrreflexiveObjectPropertyAxioms.add(axiom);
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		NegativeDataPropertyAssertionAxioms.add(axiom);
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		NegativeObjectPropertyAssertionAxioms.add(axiom);
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		SubPropertyChainOfAxioms.add(axiom);
	}

	@Override
	public void visit(SWRLRule axiom) {
		SWRLRules.add(axiom);
	}

	public void init() {
		SubClassOfAxioms = new HashSet<OWLAxiom>();
		DisjointClassesAxioms = new HashSet<OWLAxiom>();
		EquivalentClassesAxioms = new HashSet<OWLAxiom>();
		AsymmetricObjectPropertyAxioms = new HashSet<OWLAxiom>();
		SymmetricObjectPropertyAxioms = new HashSet<OWLAxiom>();
		InverseObjectPropertiesAxioms = new HashSet<OWLAxiom>();
		InverseFunctionalObjectPropertyAxioms = new HashSet<OWLAxiom>();
		FunctionalObjectPropertyAxioms = new HashSet<OWLAxiom>();
		TransitiveObjectPropertyAxioms = new HashSet<OWLAxiom>();
		FunctionalDataPropertyAxioms = new HashSet<OWLAxiom>();
		DataPropertyRangeAxioms = new HashSet<OWLAxiom>();
		DataPropertyDomainAxioms = new HashSet<OWLAxiom>();
		ObjectPropertyDomainAxioms = new HashSet<OWLAxiom>();
		ObjectPropertyRangeAxioms = new HashSet<OWLAxiom>();
		SubObjectPropertyOfAxioms = new HashSet<OWLAxiom>();
		SubDataPropertyOfAxioms = new HashSet<OWLAxiom>();
		ReflexiveObjectPropertyAxioms = new HashSet<OWLAxiom>();
		EquivalentObjectPropertiesAxioms = new HashSet<OWLAxiom>();
		EquivalentDataPropertiesAxioms = new HashSet<OWLAxiom>();
		DisjointObjectPropertiesAxioms = new HashSet<OWLAxiom>();
		DisjointDataPropertiesAxioms = new HashSet<OWLAxiom>();
		DifferentIndividualsAxioms = new HashSet<OWLAxiom>();
		SameIndividualAxioms = new HashSet<OWLAxiom>();
		ClassAssertionAxioms = new HashSet<OWLClassAssertionAxiom>();
		ObjectPropertyAssertionAxioms = new HashSet<OWLObjectPropertyAssertionAxiom>();
		DataPropertyAssertionAxioms = new HashSet<OWLAxiom>();		
		DisjointUnionAxioms = new HashSet<OWLAxiom>();
		HasKeyAxioms = new HashSet<OWLAxiom>();
		IrreflexiveObjectPropertyAxioms = new HashSet<OWLAxiom>();
		NegativeDataPropertyAssertionAxioms = new HashSet<OWLAxiom>();
		NegativeObjectPropertyAssertionAxioms = new HashSet<OWLAxiom>();
		SubPropertyChainOfAxioms = new HashSet<OWLAxiom>();
		SWRLRules = new HashSet<OWLAxiom>();
	}

}
