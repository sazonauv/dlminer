package io.dlminer.graph;


import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Created by slava on 13/03/17.
 */
public class LDataEdge extends DataEdge {

    public LDataEdge(CNode subject, OWLDataPropertyExpression label, OWLLiteral object) {
        init(subject, label, object);
    }

}
