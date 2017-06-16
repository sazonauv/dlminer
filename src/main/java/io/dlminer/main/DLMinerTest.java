package io.dlminer.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
        input.setMaxHypothesesNumber(10000);
        input.setMinPrecision(0.9);
        input.setUseCleaning(true);
//        input.setDlminerMode(DLMinerMode.CDL);
//        input.setSeedClassName(args[2]);

        OperatorConfig config = input.getConfig();
        config.maxLength = 5;
        config.minSupport = 50;

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
