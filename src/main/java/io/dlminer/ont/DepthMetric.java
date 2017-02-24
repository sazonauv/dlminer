package io.dlminer.ont;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;


public class DepthMetric {	
	
	
	public static int depth(OWLClassExpression expr) {		
		int maxDepth = 0;		
		Set<OWLClassExpression> exprs = expr.getNestedClassExpressions();				
		for (OWLClassExpression nExpr: exprs) {			
			OWLClassExpression filler = null;
			if (nExpr instanceof OWLObjectSomeValuesFrom) {
				filler = ((OWLObjectSomeValuesFrom)nExpr).getFiller();
			} else if (nExpr instanceof OWLObjectAllValuesFrom) {
				filler = ((OWLObjectAllValuesFrom)nExpr).getFiller();
			} else if (nExpr instanceof OWLObjectMaxCardinality) {
				filler = ((OWLObjectMaxCardinality)nExpr).getFiller();
			} else if (nExpr instanceof OWLObjectMinCardinality) {
				filler = ((OWLObjectMinCardinality)nExpr).getFiller();
			} else if (nExpr instanceof OWLObjectExactCardinality) {
				filler = ((OWLObjectExactCardinality)nExpr).getFiller();
			}
			if (filler != null) {
				int depth = 1;
				depth += depth(filler);
				if (maxDepth < depth) {
					maxDepth = depth;
				}
			}
		}		
		return maxDepth;
	}
	
	
	public static int depth(OWLAxiom axiom) {
		Set<OWLClassExpression> exprs = axiom.getNestedClassExpressions();
		int maxDepth = 0;						
		for (OWLClassExpression expr: exprs) {
			int depth = depth(expr);
			if (maxDepth < depth) {
				maxDepth = depth;
			}
		}		
		return maxDepth;
	}
	
	

	public static int depth(Set<OWLAxiom> axioms) {
		int maxDepth = 0;
		for (OWLAxiom ax : axioms) {
			int depth = depth(ax);
			if (maxDepth < depth) {
				maxDepth = depth;
			}
		}
		return maxDepth;
	}
	
	

}
