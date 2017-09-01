package io.dlminer.exp;

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
        input.setMinPrecision(0.9);
        input.setUseCleaning(true);
        input.setSeedClassName(args[2]);

        OperatorConfig config = input.getConfig();
        config.maxLength = 2;
        config.minSupport = 30;

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
