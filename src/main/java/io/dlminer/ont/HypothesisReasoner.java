package io.dlminer.ont;

import io.dlminer.graph.ELNode;
import io.dlminer.learn.Hypothesis;
import io.dlminer.learn.HypothesisEvaluator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;


public class HypothesisReasoner {
	
	
	private Map<OWLClassExpression, ELNode> classToTreeMap;
	
	
	
	public HypothesisReasoner() {
		classToTreeMap = new HashMap<>();
	}
	

	public HypothesisReasoner(Set<OWLAxiom> axioms) {
		init(axioms);
	}
	


	public HypothesisReasoner(Collection<Hypothesis> hypotheses) {
		init(hypotheses);
	}


	private void init(Collection<Hypothesis> hypotheses) {
		Set<OWLAxiom> axioms = new HashSet<>();
		for (Hypothesis h : hypotheses) {
			axioms.addAll(h.axioms);
		}
		init(axioms);
	}
	
	
	private void init(Set<OWLAxiom> axioms) {
		classToTreeMap = new HashMap<>();
		for (OWLAxiom axiom : axioms) {
			if (axiom instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom ax = (OWLSubClassOfAxiom) axiom;
				OWLClassExpression sub = ax.getSubClass();
				OWLClassExpression sup = ax.getSuperClass();
				if (!classToTreeMap.containsKey(sub)) {
					ELNode subNode = ELNode.parse(sub);
					classToTreeMap.put(sub, subNode);
				}
				if (!classToTreeMap.containsKey(sup)) {
					ELNode supNode = ELNode.parse(sup);
					classToTreeMap.put(sup, supNode);
				}				
			}
		}
	}
	
	
	public boolean entails(Set<OWLAxiom> axioms1, Set<OWLAxiom> axioms2) {
		for (OWLAxiom ax2 : axioms2) {
			boolean hasStrongerOrEquivalent = false;
			for (OWLAxiom ax1 : axioms1) {
				if (entails(ax1, ax2)) {
					hasStrongerOrEquivalent = true;
					break;
				}
			}
			if (!hasStrongerOrEquivalent) {
				return false;
			}
		}		
		return true;
	}

	
	public boolean entails(OWLAxiom axiom1, OWLAxiom axiom2) {
		if (axiom1.equals(axiom2)) {
			return true;
		}
		if (HypothesisEvaluator.countIntersection(
				axiom1.getSignature(), axiom2.getSignature()) < 2) {
			return false;
		}
		if (axiom1 instanceof OWLSubClassOfAxiom
				&& axiom2 instanceof OWLSubClassOfAxiom) {
			OWLSubClassOfAxiom ax1 = (OWLSubClassOfAxiom) axiom1;
			OWLSubClassOfAxiom ax2 = (OWLSubClassOfAxiom) axiom2;
			return isEquivalentTo(ax1, ax2) || isStrongerThan(ax1, ax2);
		}
		if (axiom1 instanceof OWLSubObjectPropertyOfAxiom
				&& axiom2 instanceof OWLSubObjectPropertyOfAxiom) {
			OWLSubObjectPropertyOfAxiom ax1 = (OWLSubObjectPropertyOfAxiom) axiom1;
			OWLSubObjectPropertyOfAxiom ax2 = (OWLSubObjectPropertyOfAxiom) axiom2;
			return isEquivalentTo(ax1, ax2);
		}
		if (axiom1 instanceof OWLSubPropertyChainOfAxiom
				&& axiom2 instanceof OWLSubPropertyChainOfAxiom) {
			OWLSubPropertyChainOfAxiom ax1 = (OWLSubPropertyChainOfAxiom) axiom1;
			OWLSubPropertyChainOfAxiom ax2 = (OWLSubPropertyChainOfAxiom) axiom2;
			return isEquivalentTo(ax1, ax2);	
		}		
		return false;
	}
	
	
	
	private boolean isEquivalentTo(OWLSubClassOfAxiom axiom1, OWLSubClassOfAxiom axiom2) {
		// will never generate something equivalent and not equal
		return axiom1.equals(axiom2);
	}
	
	
	
	
	private boolean isStrongerThan(OWLSubClassOfAxiom axiom1, OWLSubClassOfAxiom axiom2) {		
		OWLClassExpression sub1 = axiom1.getSubClass();
		OWLClassExpression super1 = axiom1.getSuperClass();
		OWLClassExpression sub2 = axiom2.getSubClass();
		OWLClassExpression super2 = axiom2.getSuperClass();
		boolean isSubStronger = true;
		if (!sub1.equals(sub2)) {
			ELNode subNode1 = classToTreeMap.get(sub1);
			if (subNode1 == null) {
				subNode1 = ELNode.parse(sub1);
				classToTreeMap.put(sub1, subNode1);
			}
			ELNode subNode2 = classToTreeMap.get(sub2);
			if (subNode2 == null) {
				subNode2 = ELNode.parse(sub2);
				classToTreeMap.put(sub2, subNode2);
			}				
			isSubStronger = subNode2.isMoreSpecificThan(subNode1);
		}
		if (!isSubStronger) {
			return false;
		}
		boolean isSuperStronger = true;
		if (!super1.equals(super2)) {
			ELNode superNode1 = classToTreeMap.get(super1);
			if (superNode1 == null) {
				superNode1 = ELNode.parse(super1);
				classToTreeMap.put(super1, superNode1);
			}
			ELNode superNode2 = classToTreeMap.get(super2);
			if (superNode2 == null) {
				superNode2 = ELNode.parse(super2);
				classToTreeMap.put(super2, superNode2);
			}	
			isSuperStronger = superNode1.isMoreSpecificThan(superNode2);
		}
		return isSuperStronger;		
	}
	
	
	
	private boolean isEquivalentTo(OWLSubObjectPropertyOfAxiom axiom1, OWLSubObjectPropertyOfAxiom axiom2) {
		if (axiom1.equals(axiom2)) {
			return true;
		}
		return isInverseOf(axiom1.getSubProperty(), axiom2.getSubProperty()) 
				&& isInverseOf(axiom1.getSuperProperty(), axiom2.getSuperProperty());
	}
	
	
	private boolean isInverseOf(OWLObjectPropertyExpression expr1, OWLObjectPropertyExpression expr2) {		
		if (expr1 instanceof OWLObjectInverseOf) {
			OWLObjectInverseOf invExpr1 = (OWLObjectInverseOf) expr1;			
			OWLObjectPropertyExpression argInvExpr1 = invExpr1.getInverse();
			return argInvExpr1.equals(expr2);
		}
		if (expr2 instanceof OWLObjectInverseOf) {
			OWLObjectInverseOf invExpr2 = (OWLObjectInverseOf) expr2;			
			OWLObjectPropertyExpression argInvExpr2 = invExpr2.getInverse();
			return argInvExpr2.equals(expr1);
		}
		return false;
	}
	
	
		
	private boolean isEquivalentTo(OWLSubPropertyChainOfAxiom axiom1, OWLSubPropertyChainOfAxiom axiom2) {
		if (axiom1.equals(axiom2)) {
			return true;
		}		
		return isInverseOf(axiom1.getSuperProperty(), axiom2.getSuperProperty())
				&& isInverseOf(axiom1.getPropertyChain(), axiom2.getPropertyChain());
	}
	
	
	private boolean isInverseOf(List<OWLObjectPropertyExpression> exprList1, 
			List<OWLObjectPropertyExpression> exprList2) {
		if (exprList1.size() != exprList2.size()) {
			return false;
		}
		for (int i=0; i<exprList1.size(); i++) {
			OWLObjectPropertyExpression expr1 = exprList1.get(i);
			OWLObjectPropertyExpression expr2 = exprList2.get(i);
			if (!isInverseOf(expr1, expr2)) {
				return false;
			}
		}		
		return true;
	}
	
	
	
		
			

}
