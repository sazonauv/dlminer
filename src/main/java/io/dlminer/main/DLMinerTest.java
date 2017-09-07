package io.dlminer.main;

import java.io.File;

import io.dlminer.learn.AxiomConfig;
import io.dlminer.print.Out;
import io.dlminer.refine.OperatorConfig;


/**
 * @author Slava Sazonau
 * The University of Manchester
 * Information Management Group
 * 
 * The class is a simple test/debug for DL-Miner.
 */
public class DLMinerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        DLMinerInput input = new DLMinerInput(args[0]);
        input.setMaxHypothesesNumber(1000);

        OperatorConfig operatorConfig = input.getOperatorConfig();
        operatorConfig.maxLength = 4;
        operatorConfig.minSupport = 1;

        AxiomConfig axiomConfig = input.getAxiomConfig();
        axiomConfig.minPrecision = 0.9;
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
