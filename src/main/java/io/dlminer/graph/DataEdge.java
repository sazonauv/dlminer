package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * Created by slava on 13/03/17.
 */
public abstract class DataEdge extends CEdge {


    protected void init(ALCNode subject, OWLDataPropertyExpression label, LiteralNode object) {
        this.subject = subject;
        this.label = label;
        this.object = object;
    }



    protected static boolean isMoreSpecificThan(DataEdge e1, DataEdge e2) {
        LiteralNode n1 = (LiteralNode) e1.object;
        LiteralNode n2 = (LiteralNode) e2.object;
        OWLLiteral lit1 = n1.literal;
        OWLLiteral lit2 = n2.literal;
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
