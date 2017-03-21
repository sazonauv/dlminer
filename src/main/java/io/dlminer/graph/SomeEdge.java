package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class SomeEdge extends CEdge {

	public SomeEdge(ALCNode subject, OWLObjectPropertyExpression label, ALCNode object) {
		init(subject, label, object);
	}


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SomeEdge)) {
            return false;
        }
        SomeEdge edge = (SomeEdge) obj;
        return label.equals(edge.label);
    }


}
