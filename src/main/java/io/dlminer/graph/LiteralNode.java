package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataFactory;

/**
 * Created by slava on 14/03/17.
 */
public class LiteralNode extends CNode {

    @Override
    public boolean isMoreSpecificThan(CNode node) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    protected int hash(int hash) {
        return 0;
    }

    @Override
    public CNode clone() {
        return null;
    }

    @Override
    protected int countLength(int count) {
        return 0;
    }

    @Override
    protected void buildTopLevelConcept(OWLDataFactory factory) {

    }

    @Override
    protected boolean isOWLThing() {
        return false;
    }
}
