package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class OnlyEdge extends CEdge {

	public OnlyEdge(CNode subject, OWLObjectPropertyExpression label, CNode object) {
		init(subject, label, object);
	}


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OnlyEdge)) {
            return false;
        }
        OnlyEdge edge = (OnlyEdge) obj;
        return label.equals(edge.label);
    }


}
