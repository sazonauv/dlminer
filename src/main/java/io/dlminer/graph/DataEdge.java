package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * Created by slava on 13/03/17.
 */
public abstract class DataEdge extends CEdge {

    public OWLDataPropertyExpression label;
    public OWLLiteral object;



    protected void init(CNode subject, OWLDataPropertyExpression label, OWLLiteral object) {
        this.subject = subject;
        this.label = label;
        this.object = object;
    }



    protected static boolean isMoreSpecificThan(CEdge e1, CEdge e2) {
        if (e1 instanceof DataEdge && e2 instanceof DataEdge) {
            OWLLiteral lit1 = (OWLLiteral) e1.object;
            OWLLiteral lit2 = (OWLLiteral) e2.object;
            // parse literals
            Double val1 = parseNumber(lit1);
            Double val2 = parseNumber(lit2);
            if (val1 == null || val2 == null) {
                return false;
            }
            // compare the values
            if (e1 instanceof EDataEdge) {
                if (e2 instanceof EDataEdge) {
                    return val1 == val2;
                } else if (e2 instanceof GDataEdge) {
                    return val1 >= val2;
                } else if (e2 instanceof LDataEdge) {
                    return val1 <= val2;
                }
            }
            if (e1 instanceof GDataEdge
                    && e2 instanceof GDataEdge) {
                return val1 >= val2;
            }
            if (e1 instanceof LDataEdge
                    && e2 instanceof LDataEdge) {
                return val1 <= val2;
            }
        }
        return false;
    }


    protected static Double parseNumber(OWLLiteral literal) {
        if (literal.isInteger()) {
            return (double) literal.parseInteger();
        } else if (literal.isFloat()) {
            return (double) literal.parseFloat();
        } else if (literal.isDouble()) {
            return literal.parseDouble();
        } else {
            return null;
        }
    }



}
