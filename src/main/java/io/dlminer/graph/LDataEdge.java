package io.dlminer.graph;


import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Created by slava on 13/03/17.
 */
public class LDataEdge extends DataEdge {

    public LDataEdge(ALCNode subject, OWLDataPropertyExpression label, LiteralNode object) {
        init(subject, label, object);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LDataEdge)) {
            return false;
        }
        LDataEdge edge = (LDataEdge) obj;
        return label.equals(edge.label);
    }

}
