package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Created by slava on 20/03/17.
 */
public class NumericNode extends CNode {

    public double value;

    public NumericNode(double value) {
        this.value = value;
    }

    @Override
    public boolean isMoreSpecificThan(CNode node) {
        return this.equals(node);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NumericNode)) {
            return false;
        }
        NumericNode node = (NumericNode) obj;
        return value == node.value;
    }


    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }


    @Override
    public CNode clone() {
        return new NumericNode(value);
    }

    @Override
    protected int countDepth() {
        return 0;
    }

    @Override
    protected int countLength(int count) {
        return ++count;
    }

    @Override
    protected void buildTopLevelConcept(OWLDataFactory factory) {
        // ignore
    }

    @Override
    protected boolean isOWLThing() {
        return false;
    }
}
