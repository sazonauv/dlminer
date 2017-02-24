package io.dlminer.ont;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

public class AxiomMetric {
	
	public static int countConjunctions(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countConjunctions(ax);
		}				
		return count;
	}
	
	public static int countConjunctions(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectIntersectionOf) {
				count++;
			}
		}		
		return count;
	}
	
	
	public static int countDisjunctions(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countDisjunctions(ax);
		}				
		return count;
	}
	
	
	public static int countDisjunctions(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectUnionOf) {
				count++;
			}
		}		
		return count;
	}
	
	
	public static int countNegations(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countNegations(ax);
		}				
		return count;
	}
	
	
	public static int countNegations(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectComplementOf) {
				count++;
			}
		}		
		return count;
	}
	
	
	public static int countNegations(OWLClassExpression expr) {
		int count = 0;
		for (OWLClassExpression subExpr : expr.getNestedClassExpressions()) {
			if (subExpr instanceof OWLObjectComplementOf) {
				count++;
			}
		}		
		return count;
	}
	
	
	public static int countExistentials(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countExistentials(ax);
		}				
		return count;
	}
	
	
	public static int countExistentials(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectSomeValuesFrom) {
				count++;
			}
		}		
		return count;
	}
		
	
	public static int countUniversals(Set<OWLAxiom> axioms) {
		int count = 0;
		for (OWLAxiom ax : axioms) {
			count += countUniversals(ax);
		}				
		return count;
	}
	
	
	
	public static int countUniversals(OWLAxiom axiom) {
		int count = 0;
		for (OWLClassExpression expr : axiom.getNestedClassExpressions()) {
			if (expr instanceof OWLObjectAllValuesFrom) {
				count++;
			}
		}		
		return count;
	}
	
	
	
	public static boolean isEL(OWLAxiom axiom) {
		if (countDisjunctions(axiom) == 0
				&& countNegations(axiom) == 0
				&& countUniversals(axiom) == 0) {
			return true;
		}
		return false;
	}
	
	
	public static boolean isEL(Set<OWLAxiom> axioms) {
		for (OWLAxiom axiom : axioms) {
			if (!isEL(axiom)) {
				return false;
			}
		}
		return true;
	}
	
	
	public static int countOWLThing(OWLClassExpression expr) {
		int count = 0;
		for (OWLClassExpression nest : expr.getNestedClassExpressions()) {
			if (nest.isOWLThing()) {
				count++;
			}
		}
		return count;
	}

}
