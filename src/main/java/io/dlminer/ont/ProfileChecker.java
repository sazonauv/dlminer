package io.dlminer.ont;

import io.dlminer.print.Out;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2Profile;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;



public class ProfileChecker {
	private OWLOntology ont;
	
	/**
	 * Constructor
	 * @param ont	OWL ontology
	 */
	public ProfileChecker(OWLOntology ont) {
		this.ont = ont;
		insertDeclarations(ont);
	}
	
	
	/**
	 * Insert entity declarations in case there are any missing
	 * @param ont	OWL ontology
	 * @return true if any declarations had to be inserted, false otherwise
	 */
	private boolean insertDeclarations(OWLOntology ont) {
		boolean insertedSome = false;
		OWLDataFactory df = ont.getOWLOntologyManager().getOWLDataFactory();
		for(OWLEntity e : ont.getSignature()) {
			List<OWLOntologyChange> change = ont.getOWLOntologyManager().applyChange(new AddAxiom(ont, df.getOWLDeclarationAxiom(e)));
			if(change.size() > 0)
				insertedSome = true;
		}
		return insertedSome;
	}
	
	
	/**
	 * Get a string containing a report of which profiles - OWL 2 (DL, EL, RL, QL) and RDFS - the ontology fits in  
	 * @param ontName	Ontology name
	 * @param includeHeaders	Include the corresponding headers
	 * @return String report of profile checks
	 */
	public String getCompleteProfileReport(String ontName, boolean includeHeaders) {
		String output = "";
		if(includeHeaders)
			output += "Ontology,OWL 2,OWL 2 DL,OWL 2 EL, OWL 2 QL, OWL 2 RL, RDFS\n";
		output += ontName + "," + isOWL2(ont) + "," + isOWL2DL(ont) + "," + isOWL2EL(ont) + "," + isOWL2QL(ont)
				+ "," + isOWL2RL(ont) + "," + isRDFS(ont) + "\n";
		return output;
	}
	
	
	/**
	 * Check if ontology uses only RDFS constructs
	 * @param ont	OWL ontology
	 * @return true if ontology uses only RDFS constructs, false otherwise
	 */
	public boolean isRDFS(OWLOntology ont) {
		boolean isRDFS = true;	
		loopAxioms:
			for(OWLAxiom ax : ont.getAxioms()) {
				if(ax.isLogicalAxiom()) {
					if(ax.isOfType(AxiomType.SUBCLASS_OF)) {
						OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) ax;
						if(subAx.getSubClass().isAnonymous() || subAx.getSuperClass().isAnonymous()) {
							isRDFS = false;
							Out.p("SubClassAx: " + ax);
							break loopAxioms;
						}
					}
					else if(ax.isOfType(AxiomType.SUB_OBJECT_PROPERTY)) {
						OWLSubObjectPropertyOfAxiom subProp = (OWLSubObjectPropertyOfAxiom)ax;
						if(subProp.getSubProperty().isAnonymous() || subProp.getSuperProperty().isAnonymous()) {
							isRDFS = false;
							Out.p("SupPropertyAx: " + ax);
							break loopAxioms;
						}
					}
					else if(ax.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN) || ax.isOfType(AxiomType.OBJECT_PROPERTY_RANGE) ||
							ax.isOfType(AxiomType.DATA_PROPERTY_ASSERTION) || ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION) ||
							ax.isOfType(AxiomType.DATA_PROPERTY_DOMAIN) || ax.isOfType(AxiomType.DATA_PROPERTY_RANGE)) {
						// do nothing
					}
					else if(ax.isOfType(AxiomType.CLASS_ASSERTION)) {
						OWLClassAssertionAxiom assAx = (OWLClassAssertionAxiom)ax;
						if(assAx.getClassExpression().isAnonymous()) {
							isRDFS = false;
							Out.p("Class Assertion: " + ax);
							break loopAxioms;
						}
					}
					else {
						Out.p("Some other axiom: " + ax);
						isRDFS = false;
						break loopAxioms;
					}
				}
			}
		return isRDFS;
	}
	
	
	/**
	 * Check if ontology is in OWL2
	 * @param ont	OWLOntology
	 * @return true if in OWL2, false otherwise
	 */
	public boolean isOWL2(OWLOntology ont) {
		return new OWL2Profile().checkOntology(ont).isInProfile();
	}
	
	
	/**
	 * Check if ontology is in OWL2 DL profile
	 * @param ont	OWLOntology
	 * @return true if in OWL2 DL profile, false otherwise
	 */
	public boolean isOWL2DL(OWLOntology ont) {
		return new OWL2DLProfile().checkOntology(ont).isInProfile();
	}
	
	
	/**
	 * Check if ontology is in OWL2 EL profile
	 * @param ont	OWLOntology
	 * @return true if in OWL2 EL profile, false otherwise
	 */
	public boolean isOWL2EL(OWLOntology ont) {
		return new OWL2ELProfile().checkOntology(ont).isInProfile();
	}
	
	
	/**
	 * Check if ontology is in OWL2 RL profile
	 * @param ont	OWLOntology
	 * @return true if in OWL2 RL profile, false otherwise
	 */
	public boolean isOWL2RL(OWLOntology ont) {
		return new OWL2RLProfile().checkOntology(ont).isInProfile();
	}
	
	
	/**
	 * Check if ontology is in OWL2 QL profile
	 * @param ont	OWLOntology
	 * @return true if in OWL2 QL profile, false otherwise
	 */
	public boolean isOWL2QL(OWLOntology ont) {
		return new OWL2QLProfile().checkOntology(ont).isInProfile();
	}
	

}
