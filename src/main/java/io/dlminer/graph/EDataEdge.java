package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Created by slava on 13/03/17.
 */
public class EDataEdge extends DataEdge {

    public EDataEdge(CNode subject, OWLDataPropertyExpression label, OWLLiteral object) {
        super(subject, label, object);
    }

}
