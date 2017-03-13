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
}
