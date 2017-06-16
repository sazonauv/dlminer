package io.dlminer.exp;

import io.dlminer.main.DLMiner;
import io.dlminer.main.DLMinerInput;
import io.dlminer.print.Out;
import io.dlminer.refine.OperatorConfig;

import java.io.File;

/**
 * Created by slava on 16/06/17.
 */
public class DiscernOntologyExperiment {


    public static void main(String[] args) throws Exception {

        // check parameters number
        if (args.length != 6) {
            throw new RuntimeException(
                    "You need exactly 6 parameters: ontology path, hypotheses path, " +
                            "\nmax hypotheses number, min precision, " +
                            "\nmin support, max concept length.");
        }

        // process parameters
        File ontologyFile = new File(args[0]);
        File hypothesesFile = new File(args[1]);
        Integer hypothesesNumber = Integer.parseInt(args[2]);
        Double minPrecision = Double.parseDouble(args[3]);
        Integer minSupport = Integer.parseInt(args[4]);
        Integer maxConceptLength = Integer.parseInt(args[5]);

        // set logging
//        Out.setLog(hypothesesPath);

        // set parameters
        DLMinerInput input = new DLMinerInput(ontologyFile);
        input.setMaxHypothesesNumber(hypothesesNumber);
        input.setMinPrecision(minPrecision);
        input.setUseCleaning(true);

        // language bias
        OperatorConfig config = input.getConfig();
        config.maxLength = maxConceptLength;
        config.minSupport = minSupport;

        // run DL-Miner
        DLMiner miner = new DLMiner(input);
        try {
            miner.init();
            miner.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Out.p("\nSaving hypotheses");
        miner.saveHypotheses(hypothesesFile);
        Out.p("\nStatistics:");
        Out.p(miner.getStats());

        Out.p("\nAll is done.\n");
    }




}
