package io.dlminer.graph;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Created by slava on 20/03/17.
 */
public class LiteralNode extends CNode {

    public OWLLiteral literal;

    public LiteralNode(OWLLiteral literal) {
        this.literal = literal;
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
        if (!(obj instanceof LiteralNode)) {
            return false;
        }
        LiteralNode node = (LiteralNode) obj;
        return literal.equals(node.literal);
    }

    @Override
    protected int hash(int hash) {
        return literal.hashCode();
    }

    @Override
    public CNode clone() {
        return new LiteralNode(literal);
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
