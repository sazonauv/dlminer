package io.dlminer.exp;

import io.dlminer.graph.ALCNode;
import io.dlminer.learn.ConceptBuilder;
import io.dlminer.main.DLMiner;
import io.dlminer.main.DLMinerInput;
import io.dlminer.ont.Logic;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.Out;
import io.dlminer.refine.OperatorConfig;

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
        config.checkRedundancy = false;
        DLMiner miner = new DLMiner(input);
        try {
            miner.init();
        } catch (Exception e) {
            e.printStackTrace();
        }


        ConceptBuilder conceptBuilder = miner.getOutput().getConceptBuilder();

        // generate concepts
        conceptBuilder.buildConcepts();

        Set<ALCNode> nodes = conceptBuilder.getNodes();
        Out.p("\n" + nodes.size() + " concepts are built");

        double totalTime = 0;
        for (ALCNode node : nodes) {
            totalTime += conceptBuilder.getTimeByExpression(node.getConcept());
        }
        Out.p("\nTotal time of checking instances = " + totalTime + " seconds");


        Out.p("\nAmong them at least X instances have Y concepts:");
        Map<Integer, Set<ALCNode>> instanceNodesMap = new HashMap<>();
        for (ALCNode node : nodes) {
            Set<ALCNode> instNodes = instanceNodesMap.get(node.coverage);
            if (instNodes == null) {
                instNodes = new HashSet<>();
                instanceNodesMap.put(node.coverage, instNodes);
            }
            instNodes.add(node);
        }

        List<Integer> instanceNumbers = new ArrayList<>(instanceNodesMap.keySet());
        Collections.sort(instanceNumbers);
        for (Integer minInstanceNumber : instanceNumbers) {
            int conceptNumber = 0;
            for (Integer instanceNumber : instanceNodesMap.keySet()) {
                if (instanceNumber >= minInstanceNumber) {
                    conceptNumber += instanceNodesMap.get(instanceNumber).size();
                }
            }
            Out.p(minInstanceNumber + " instances : " + conceptNumber + " concepts");
        }


        Out.p("\nAll is done.\n");

    }

}
