package io.dlminer.exp;

import io.dlminer.learn.ConceptBuilder;
import io.dlminer.main.DLMiner;
import io.dlminer.main.DLMinerInput;
import io.dlminer.ont.Logic;
import io.dlminer.ont.ReasonerName;
import io.dlminer.print.Out;
import io.dlminer.refine.OperatorConfig;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.io.File;
import java.util.Map;
import java.util.Set;

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
        config.useDataProperties = false;
        config.useNegation = true;
        config.useDisjunction = true;
        config.useUniversalRestriction = true;
        if (logic.equals(Logic.EL)) {
            config.useNegation = false;
            config.useDisjunction = false;
            config.useUniversalRestriction = false;
        }

        // optimisations
        config.checkDisjointness = true;
        config.useReasonerForAtomicClassInstances = true;
        config.useReasonerForClassInstances = false;

        config.checkClassHierarchy = false;
        config.checkSyntacticRedundancy = false;


        DLMiner miner = new DLMiner(input);
        try {
            miner.init();
        } catch (Exception e) {
            e.printStackTrace();
        }


        ConceptBuilder conceptBuilder = miner.getOutput().getConceptBuilder();

        // generate only supported concepts
        long start = System.currentTimeMillis();
        conceptBuilder.buildConcepts();
        long end = System.currentTimeMillis();
        double time = (double)(end - start) / 1e3;
        Map<OWLClass, Set<OWLNamedIndividual>> classInstMap = conceptBuilder.getClassInstanceMap();
        Out.p("\n" + classInstMap.size() + " concepts are built in " + time + " seconds");


        Out.p("\nAll is done.\n");

    }

}
