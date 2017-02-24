package io.dlminer.ont;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLSubPropertyAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyExpressionImpl;

public class OWLObjectPropertyChain extends OWLObjectPropertyExpressionImpl {
	
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	
	
	private List<OWLObjectPropertyExpression> properties;
	
	private Set<OWLEntity> signature;
	

	public OWLObjectPropertyChain(OWLObjectPropertyExpression... properties) {
		super();
		this.properties = new ArrayList<>(Arrays.asList(properties));
		initSignature();
	}
	
	
	private void initSignature() {
		signature = new HashSet<>();
		for (OWLObjectPropertyExpression property :properties) {
			signature.addAll(property.getSignature());
		}
	}


	public List<OWLObjectPropertyExpression> getPropertyExpressions() {
		return properties;
	}
	
	
	// override Object methods
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof OWLObjectPropertyChain))
			return false;				
		OWLObjectPropertyChain chain = (OWLObjectPropertyChain) obj;		
		return properties.equals(chain.properties);
	}


	@Override
	public int hashCode() {
		return super.hashCode() + properties.hashCode();
	}


	@Override
	public String toString() {
		return super.toString().concat(properties.toString());
	}
	
	
	
	@Override
	public Set<OWLEntity> getSignature() {
		return signature;
	}

	
	
	
	// override Property methods
	@Override
	public OWLObjectProperty asOWLObjectProperty() {
		return null;
	}

	@Override
	public boolean isAnonymous() {
		return true;
	}

	@Override
	public void accept(OWLPropertyExpressionVisitor visitor) {
		// TODO Auto-generated method stub		
	}

	@Override
	public <O> O accept(OWLPropertyExpressionVisitorEx<O> visitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOWLTopObjectProperty() {
		return false;
	}

	@Override
	public boolean isOWLBottomObjectProperty() {
		return false;
	}

	@Override
	public boolean isOWLTopDataProperty() {
		return false;
	}

	@Override
	public boolean isOWLBottomDataProperty() {
		return false;
	}
	
	
	

	@Override
	public void accept(OWLObjectVisitor visitor) {
		// TODO Auto-generated method stub		
	}

	@Override
	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Set<? extends OWLSubPropertyAxiom<OWLObjectPropertyExpression>> getSubPropertyAxiomsForRHS(
			OWLOntology ont) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int compareObjectOfSameType(OWLObject object) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public OWLObjectPropertyExpression getInverseProperty() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public OWLObjectProperty getNamedProperty() {
		// TODO Auto-generated method stub
		return null;
	}


	public void addSignatureEntitiesToSet(Set<OWLEntity> entities) {
		// TODO Auto-generated method stub
		
	}


	public void addAnonymousIndividualsToSet(Set<OWLAnonymousIndividual> anons) {
		// TODO Auto-generated method stub
		
	}


	protected int index() {
		// TODO Auto-generated method stub
		return 0;
	}
		

}
