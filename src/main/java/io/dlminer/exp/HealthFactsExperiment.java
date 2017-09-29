package io.dlminer.exp;

import io.dlminer.learn.AxiomConfig;
import io.dlminer.learn.AxiomPattern;
import io.dlminer.main.DLMiner;
import io.dlminer.main.DLMinerInput;
import io.dlminer.print.Out;
import io.dlminer.refine.OperatorConfig;

import java.io.File;

/**
 * Created by slava on 01/09/17.
 */
public class HealthFactsExperiment {


    public static void main(String[] args) {
        DLMinerInput input = new DLMinerInput(args[0]);
        input.setMaxHypothesesNumber(50000);

        OperatorConfig operatorConfig = input.getOperatorConfig();
        operatorConfig.maxLength = 2;
        operatorConfig.minSupport = 50;

        AxiomConfig axiomConfig = input.getAxiomConfig();
        axiomConfig.minPrecision = 0.9;
        axiomConfig.seedClassName = args[2];
        axiomConfig.ignoredStrings = new String[] {"Lab", "Drug", "Encounter", "Thing"};
        axiomConfig.axiomPattern = AxiomPattern.SEEDS_LHS;
        axiomConfig.useCleaning = true;

        DLMiner miner = new DLMiner(input);
        try {
            miner.init();
            miner.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Out.p("\nSaving hypotheses");
        File hypothesesFile = new File(args[1]);
        miner.saveHypotheses(hypothesesFile);
        Out.p("\nStatistics:");
        Out.p(miner.getStats());
    }

}
