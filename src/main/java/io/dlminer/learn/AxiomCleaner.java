package io.dlminer.learn;

import io.dlminer.graph.HSNode;
import io.dlminer.graph.HSTree;
import io.dlminer.ont.OntologyHandler;
import io.dlminer.ont.ReasonerLoader;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.Out;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Slava Sazonau on 14/03/16.
 */
public abstract class AxiomCleaner {


    public static Set<Set<OWLAxiom>> findMinimalSubsetsOptimized(Set<OWLAxiom> axioms, OntologyHandler handler, OWLReasoner reasoner,
                                                          int maxSteps, int minSubsetSize) {
        int size = axioms.size();
        Set<Set<OWLAxiom>> minSubsets = null;
        // calculate the number of chunks
        if (size > minSubsetSize) {
            // place axioms into chunks
            List<Set<OWLAxiom>> chunks = new LinkedList<>();
            int count= 0;
            Set<OWLAxiom> buffer = new HashSet<>();
            for (OWLAxiom ax : axioms) {
                count++;
                buffer.add(ax);
                if (count > minSubsetSize) {
                    Set<OWLAxiom> chunk = new HashSet<>(buffer);
                    chunks.add(chunk);
                    count = 0;
                    buffer.clear();
                }
            }
            // run HST for each chunk
            List<HSTree<OWLAxiom>> treeList = new LinkedList<>();
            for (int i=0; i<chunks.size(); i++) {
                Set<OWLAxiom> chunk = chunks.get(i);
                Set<OWLEntity> signature = OntologyHandler.getSignature(chunk);
                Set<OWLAxiom> module = OntologyHandler.extractModule(handler.getOntology(), signature, ModuleType.BOT);
                // init a handler
                OntologyHandler chunkHandler = new OntologyHandler(module, handler.getIRI());
                // init a reasoner
                OWLReasoner chunkReasoner = null;
                try {
                    chunkReasoner = ReasonerLoader.initReasoner(ReasonerName.HERMIT, chunkHandler.getOntology());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // build a tree
                HSTree<OWLAxiom> tree = findMinimalSubsets(chunk, chunkHandler, chunkReasoner, maxSteps);
                treeList.add(tree);
            }


        } else {
            HSTree<OWLAxiom> hstree = findMinimalSubsets(axioms, handler, reasoner, maxSteps);
            minSubsets = hstree.getMinimalSubsets();
        }
        return minSubsets;
    }


    public static HSTree<OWLAxiom> findMinimalSubsets(Set<OWLAxiom> axioms, OntologyHandler handler, OWLReasoner reasoner,
                                                      int maxSteps) {
        Out.p("Set size=" + axioms.size());
        // root has empty path
        HSNode<Set<OWLAxiom>> root = new HSNode<>(axioms, new HashSet<OWLAxiom>());
        HSTree<OWLAxiom> hstree = new HSTree<>(root);
        LinkedList<HSNode<Set<OWLAxiom>>> hist = new LinkedList<>();
        hist.add(root);
        // until all leaves are null
        int steps = 0;
        while (!hist.isEmpty() && steps <= maxSteps) {
            steps++;
            if (steps % 10 == 0) {
                Out.p("steps=" + steps);
            }
            // depth-first search
            HSNode<Set<OWLAxiom>> next = hist.pollLast();
            // do not explore empty nodes (e.g. tautologies)
            if (next.value.isEmpty()) {
                // mark with null
                HSNode<Set<OWLAxiom>> child = new HSNode<>(null, next.path);
                hstree.addChild(next, child);
            } else {
                // remove the path
                handler.removeAxioms(next.path);
                for (OWLAxiom ax : next.value) {
                    Set<OWLAxiom> path = new HashSet<>(next.path);
                    path.add(ax);
                    // do not explore if the path exists
                    if (!hstree.hasPath(path)) {
                        handler.removeAxiom(ax);
                        reasoner.flush();
                        reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
                        // explore after the axiom removal
                        HSNode<Set<OWLAxiom>> child = null;
                        if (reasoner.isEntailed(ax)) {
                            Set<OWLAxiom> value = new HashSet<>(next.value);
                            value.remove(ax);
                            child = new HSNode<Set<OWLAxiom>>(value, path);
                            // only add satisfactory nodes
                            hist.add(child);
                        } else {
                            // mark with null
                            child = new HSNode<Set<OWLAxiom>>(null, path);
                        }
                        // add a child if its path is new
                        hstree.addChild(next, child);
                        // add the axiom back
                        handler.addAxiom(ax);
                    }
                }
                // add the path
                handler.addAxioms(next.path);
            }
        }
        return hstree;
    }



}
