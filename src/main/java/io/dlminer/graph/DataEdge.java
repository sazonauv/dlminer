package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Created by slava on 13/03/17.
 */
public abstract class DataEdge extends CEdge {


    protected void init(ALCNode subject, OWLDataPropertyExpression label, NumericNode object) {
        this.subject = subject;
        this.label = label;
        this.object = object;
    }



    protected static boolean isMoreSpecificThan(DataEdge e1, DataEdge e2) {
        NumericNode n1 = (NumericNode) e1.object;
        NumericNode n2 = (NumericNode) e2.object;
        if (n1.equals(n2)) {
            return true;
        }
        // parse literals
        double val1 = n1.value;
        double val2 = n2.value;
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


    public static Double parseNumber(OWLLiteral literal) {
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
