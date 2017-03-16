package io.dlminer.ont;

import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.owlapi.model.*;

public class LengthMetric {


	
	public static Integer length(OWLClassExpression expr) {
		if (!expr.isAnonymous()) {
			return 1;
		}
		// optimisation: not A, R some A
		OWLClassExpression sub = getDirectSubExpression(expr);
		if (sub != null && !sub.isAnonymous()) {
			return atomLength(sub) + atomLength(expr);
		}
		int length = 0;		
		LinkedList<OWLClassExpression> subExprs = traverse(expr);		
		for (OWLClassExpression subExpr : subExprs) {
			length += atomLength(subExpr);
		}		
		return length;
	}

	
	private static Integer atomLength(OWLClassExpression expr) {
		// a concept name
        if (!expr.isAnonymous()) {
			return 1;
		}
		// conjunctions and disjunctions
		if (expr instanceof OWLObjectIntersectionOf) {
			OWLObjectIntersectionOf intersect = (OWLObjectIntersectionOf)expr;			
			return intersect.asConjunctSet().size() - 1;
		}
		if (expr instanceof OWLObjectUnionOf) {
			OWLObjectUnionOf union = (OWLObjectUnionOf)expr;						
			return union.asDisjunctSet().size() - 1;
		}
		// object property restrictions
		if (expr instanceof OWLObjectSomeValuesFrom) {
			return length(((OWLObjectSomeValuesFrom)expr).getProperty());
		}
		if (expr instanceof OWLObjectAllValuesFrom) {
			return length(((OWLObjectAllValuesFrom)expr).getProperty());
		}
		if (expr instanceof OWLObjectMaxCardinality) {
			return length(((OWLObjectMaxCardinality)expr).getProperty());
		}
		if (expr instanceof OWLObjectMinCardinality) {
			return length(((OWLObjectMinCardinality)expr).getProperty());
		}
		if (expr instanceof OWLObjectExactCardinality) {
			return length(((OWLObjectExactCardinality)expr).getProperty());
		}
        // data property restrictions
        if (expr instanceof OWLDataSomeValuesFrom) {
            return length(((OWLDataSomeValuesFrom)expr).getProperty()) + 1;
        }
        if (expr instanceof OWLDataAllValuesFrom) {
            return length(((OWLDataAllValuesFrom)expr).getProperty()) + 1;
        }
        if (expr instanceof OWLDataMaxCardinality) {
            return length(((OWLDataMaxCardinality)expr).getProperty()) + 1;
        }
        if (expr instanceof OWLDataMinCardinality) {
            return length(((OWLDataMinCardinality)expr).getProperty()) + 1;
        }
        if (expr instanceof OWLDataExactCardinality) {
            return length(((OWLDataExactCardinality)expr).getProperty()) + 1;
        }
        // nominals
		if (expr instanceof OWLObjectComplementOf
                || expr instanceof  OWLDataComplementOf) {
			return 1;
		}
		if (expr instanceof OWLObjectHasSelf) {
			return length(((OWLObjectHasSelf)expr).getProperty());
		}
		if (expr instanceof OWLObjectHasValue) {
			return length(((OWLObjectHasValue)expr).getProperty()) + 1;
		}
        if (expr instanceof OWLDataHasValue) {
            return length(((OWLDataHasValue)expr).getProperty()) + 1;
        }
		if (expr instanceof OWLObjectOneOf) {
			return ((OWLObjectOneOf)expr).getIndividuals().size();
		}
        if (expr instanceof OWLDataOneOf) {
            return ((OWLDataOneOf)expr).getValues().size();
        }
		return 0;
	}


	private static Integer length(OWLSubClassOfAxiom axiom) {
		return length(axiom.getSubClass()) + length(axiom.getSuperClass());		
	}


	
	private static Integer length(OWLSubObjectPropertyOfAxiom axiom) {
		return length(axiom.getSubProperty()) + length(axiom.getSuperProperty());
	}


	private static Integer length(OWLSubPropertyChainOfAxiom axiom) {
		int length = 0;
		for (OWLObjectPropertyExpression prop : axiom.getPropertyChain()) {
			length += length(prop);
		}
		length += length(axiom.getSuperProperty());
		return length;
	}


	// 1 if atomic, 2 if inverse
	public static int length(OWLObjectPropertyExpression prop) {
		if (prop.isAnonymous()) {
			return 2;
		}		
		return 1;
	}


    // 1 if atomic, 2 if inverse
    public static int length(OWLDataPropertyExpression prop) {
        if (prop.isAnonymous()) {
            return 2;
        }
        return 1;
    }


	public static Integer length(OWLAxiom axiom) {		
		int length = 0;
		// C -> D
		if (axiom instanceof OWLSubClassOfAxiom) {
			length += length((OWLSubClassOfAxiom)axiom);			
		}
		// R -> S
		if (axiom instanceof OWLSubObjectPropertyOfAxiom) {
			length += length((OWLSubObjectPropertyOfAxiom)axiom);
		}
		// R1 o R2 -> S
		if (axiom instanceof OWLSubPropertyChainOfAxiom) {
			length += length((OWLSubPropertyChainOfAxiom)axiom);			
		}
		return length;		
	}

	

	public static Integer length(Set<OWLAxiom> axioms) {
		int length = 0;
		for (OWLAxiom ax : axioms) {
			length += length(ax);
		}
		return length;
	}
	
	
	
	
	private static Set<OWLClassExpression> 
	getDirectSubExpressions(OWLClassExpression expr) {			
		if (expr instanceof OWLObjectIntersectionOf) {
			OWLObjectIntersectionOf intersect = (OWLObjectIntersectionOf)expr;
			return intersect.asConjunctSet();			
		}
		if (expr instanceof OWLObjectUnionOf) {
			OWLObjectUnionOf union = (OWLObjectUnionOf)expr;
			return union.asDisjunctSet();			
		}
		return null;
	}


	private static OWLClassExpression 
	getDirectSubExpression(OWLClassExpression expr) {	
		if (expr instanceof OWLObjectSomeValuesFrom) {
			return ((OWLObjectSomeValuesFrom)expr).getFiller();
		}
		if (expr instanceof OWLObjectAllValuesFrom) {
			return ((OWLObjectAllValuesFrom)expr).getFiller();
		}
		if (expr instanceof OWLObjectMaxCardinality) {
			return ((OWLObjectMaxCardinality)expr).getFiller();
		}
		if (expr instanceof OWLObjectMinCardinality) {
			return ((OWLObjectMinCardinality)expr).getFiller();
		}
		if (expr instanceof OWLObjectExactCardinality) {
			return ((OWLObjectExactCardinality)expr).getFiller();
		}
		if (expr instanceof OWLObjectComplementOf) {
			return ((OWLObjectComplementOf)expr).getOperand();
		}
		return null;
	}
	
	
	private static LinkedList<OWLClassExpression> traverse(OWLClassExpression expr) {
		LinkedList<OWLClassExpression> processed = new LinkedList<>();
		LinkedList<OWLClassExpression> que = new LinkedList<>();
		que.add(expr);		
		while (!que.isEmpty()) {
			OWLClassExpression current = que.pollFirst();
			processed.add(current);
			Set<OWLClassExpression> subExprs = getDirectSubExpressions(current);
			if (subExprs != null) {
				que.addAll(subExprs);
			}
			OWLClassExpression subExpr = getDirectSubExpression(current);
			if (subExpr != null) {
				que.add(subExpr);
			}
		}
		return processed;
	}




}
