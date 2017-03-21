package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Created by slava on 13/03/17.
 */
public class EDataEdge extends DataEdge {

    public EDataEdge(ALCNode subject, OWLDataPropertyExpression label, LiteralNode object) {
        init(subject, label, object);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EDataEdge)) {
            return false;
        }
        EDataEdge edge = (EDataEdge) obj;
        return label.equals(edge.label);
    }


}
