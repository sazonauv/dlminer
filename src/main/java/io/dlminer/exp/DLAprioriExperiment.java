package io.dlminer.exp;


import io.dlminer.graph.ALCNode;
import io.dlminer.learn.ConceptBuilder;
import io.dlminer.main.DLMiner;
import io.dlminer.main.DLMinerInput;
import io.dlminer.ont.Logic;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.CSVWriter;
import io.dlminer.print.Out;
import io.dlminer.refine.OperatorConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by slava on 18/04/17.
 */
public class DLAprioriExperiment {


    public static void main(String[] args) throws Exception {

        // check parameters number
        if (args.length != 8) {
            throw new RuntimeException(
                    "You need exactly 8 parameters: ontology path, csv path," +
                            "\nmax role depth, max concept length, " +
                            "\nlogic, reasoner name, redundancy flag, max number of concepts.");
        }


        // process parameters
        File ontFile = new File(args[0]);
        File csvFile = new File(args[1]);
        Integer roleDepth = Integer.parseInt(args[2]);
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
        boolean checkRedundancy;
        if (args[6].equals("t")) {
            checkRedundancy = true;
        } else {
            checkRedundancy = false;
        }
        Integer maxConceptNumber = Integer.parseInt(args[7]);

        // set parameters
        DLMinerInput input = new DLMinerInput(ontFile);
        input.setReasonerName(reasonerName);

        // language bias
        OperatorConfig operatorConfig = input.getOperatorConfig();
        operatorConfig.maxDepth = roleDepth;
        operatorConfig.maxLength = maxConceptLength;
        operatorConfig.minSupport = 0;
        // optimisations
        operatorConfig.checkDisjointness = true;
        operatorConfig.useReasonerForAtomicClassInstances = true;
        operatorConfig.useReasonerForClassInstances = false;
        operatorConfig.storeInstances = false;
        operatorConfig.useDataProperties = false;
        operatorConfig.useNegation = true;
        operatorConfig.useDisjunction = true;
        operatorConfig.useUniversalRestriction = true;
        operatorConfig.logic = logic;
        if (logic.equals(Logic.EL)) {
            operatorConfig.useNegation = false;
            operatorConfig.useDisjunction = false;
            operatorConfig.useUniversalRestriction = false;
        }


        // first ignore redundancy
        operatorConfig.checkRedundancy = checkRedundancy;
        DLMiner miner = new DLMiner(input);
        try {
            miner.init();
        } catch (Exception e) {
            e.printStackTrace();
        }


        ConceptBuilder conceptBuilder = miner.getOutput().getConceptBuilder();

        // generate concepts
        conceptBuilder.buildConcepts(maxConceptNumber);

        Set<ALCNode> nodes = conceptBuilder.getNodes();
        Out.p("\n" + nodes.size() + " concepts are built");

        double totalTime = 0;
        for (ALCNode node : nodes) {
            Double time = conceptBuilder.getTimeByExpression(node.getConcept());
            totalTime += (time == null) ? 0 : time;
        }
        Out.p("\nTotal time of checking instances = " + Out.fn(totalTime) + " seconds");


        Map<Integer, Set<ALCNode>> instNumNodesMap = new HashMap<>();
        for (ALCNode node : nodes) {
            Integer instNum = (node.coverage == null) ? 0 : node.coverage;
            Set<ALCNode> instNodes = instNumNodesMap.get(instNum);
            if (instNodes == null) {
                instNodes = new HashSet<>();
                instNumNodesMap.put(instNum, instNodes);
            }
            instNodes.add(node);
        }

//        Out.p("\nAmong them at least X instances have Y concepts:");
        List<Integer> instanceNumbers = new ArrayList<>(instNumNodesMap.keySet());
        Collections.sort(instanceNumbers);
        Map<Integer, Integer> instNodeNumMap = new LinkedHashMap<>();
        for (Integer minInstanceNumber : instanceNumbers) {
            int conceptNumber = 0;
            for (Integer instanceNumber : instNumNodesMap.keySet()) {
                if (instanceNumber >= minInstanceNumber) {
                    conceptNumber += instNumNodesMap.get(instanceNumber).size();
                }
            }
            instNodeNumMap.put(minInstanceNumber, conceptNumber);
//            Out.p(minInstanceNumber + " instances : " + conceptNumber + " concepts");
        }


        Out.p("\nSaving results to CSV");
        CSVWriter csvWriter = new CSVWriter(csvFile, true);
        csvWriter.saveConceptNumbersToCSV(ontFile.getName().replace(".owl", ""), instNodeNumMap);
        csvWriter.close();

        Out.p("\nAll is done.\n");

    }


}
