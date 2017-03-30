package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;

/**
 * Created by slava on 13/03/17.
 */
public class GDataEdge extends DataEdge {

    public GDataEdge(ALCNode subject, OWLDataPropertyExpression label, NumericNode object) {
        init(subject, label, object);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GDataEdge)) {
            return false;
        }
        GDataEdge edge = (GDataEdge) obj;
        return label.equals(edge.label);
    }


}
