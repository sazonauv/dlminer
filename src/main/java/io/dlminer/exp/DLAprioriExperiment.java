package io.dlminer.exp;

import io.dlminer.graph.ALCNode;
import io.dlminer.learn.ConceptBuilder;
import io.dlminer.main.DLMiner;
import io.dlminer.main.DLMinerInput;
import io.dlminer.ont.Logic;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.Out;
import io.dlminer.refine.OperatorConfig;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.io.File;
import java.util.*;

/**
 * Created by slava on 18/04/17.
 */
public class DLAprioriExperiment {


    public static void main(String[] args) throws Exception {

        // check parameters number
        if (args.length != 6) {
            throw new RuntimeException(
                    "You need exactly 6 parameters: ontology path, " +
                            "\nmax role depth, " +
                            "\nmin support, max concept length, " +
                            "\nlogic, reasoner name.");
        }


        // process parameters
        File ontFile = new File(args[0]);
        Integer roleDepth = Integer.parseInt(args[1]);
        Integer minSupport = Integer.parseInt(args[2]);
        Integer maxConceptLength = Integer.parseInt(args[3]);
        Logic logic = null;
        for (Logic l : Logic.values()) {
            if (l.toString().equalsIgnoreCase(args[4])) {
                logic = l;
            }
        }
        ReasonerName reasonerName = null;
        for (ReasonerName rname : ReasonerName.values()) {
            if (rname.toString().equalsIgnoreCase(args[5])) {
                reasonerName = rname;
            }
        }

        // set parameters
        DLMinerInput input = new DLMinerInput(ontFile);
        input.setLogic(logic);
        input.setReasonerName(reasonerName);

        // language bias
        OperatorConfig config = input.getConfig();
        config.maxDepth = roleDepth;
        config.maxLength = maxConceptLength;
        config.minSupport = minSupport;
        // optimisations
        config.checkDisjointness = true;
        config.useReasonerForAtomicClassInstances = true;
        config.useReasonerForClassInstances = false;
        config.useDataProperties = false;
        config.useNegation = true;
        config.useDisjunction = true;
        config.useUniversalRestriction = true;
        if (logic.equals(Logic.EL)) {
            config.useNegation = false;
            config.useDisjunction = false;
            config.useUniversalRestriction = false;
        }


        // first ignore redundancy
        config.checkRedundancy = true;
        DLMiner miner = new DLMiner(input);
        try {
            miner.init();
        } catch (Exception e) {
            e.printStackTrace();
        }


        ConceptBuilder conceptBuilder = miner.getOutput().getConceptBuilder();

        // generate concepts
        conceptBuilder.buildConcepts();

        Map<OWLClassExpression, Set<OWLNamedIndividual>> exprInstMap = conceptBuilder.getClassExpressionInstanceMap();
        Out.p("\n" + exprInstMap.size() + " concepts are built");

        double totalTime = 0;
        for (OWLClassExpression expr : exprInstMap.keySet()) {
            totalTime += conceptBuilder.getTimeByExpression(expr);
        }
        Out.p("\nTotal time of checking instances = " + totalTime + " seconds");


        Out.p("\nAmong them at least X instances have Y concepts:");
        Map<Integer, Set<OWLClassExpression>> instNumExprsMap = new HashMap<>();
        for (OWLClassExpression expr : exprInstMap.keySet()) {
            int instNum = exprInstMap.get(expr).size();
            Set<OWLClassExpression> exprs = instNumExprsMap.get(instNum);
            if (exprs == null) {
                exprs = new HashSet<>();
                instNumExprsMap.put(instNum, exprs);
            }
            exprs.add(expr);
        }

        List<Integer> instanceNumbers = new ArrayList<>(instNumExprsMap.keySet());
        Collections.sort(instanceNumbers);
        for (Integer minInstanceNumber : instanceNumbers) {
            int conceptNumber = 0;
            for (Integer instanceNumber : instNumExprsMap.keySet()) {
                if (instanceNumber >= minInstanceNumber) {
                    conceptNumber += instNumExprsMap.get(instanceNumber).size();
                }
            }
            Out.p(minInstanceNumber + " instances : " + conceptNumber + " concepts");
        }


        Out.p("\nAll is done.\n");

    }

}
