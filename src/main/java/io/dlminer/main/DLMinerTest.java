package io.dlminer.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import io.dlminer.ont.Logic;
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
		InputStream ontologyFile = null;
        try {
        	ontologyFile = new FileInputStream(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        DLMinerInput input = new DLMinerInput(ontologyFile);
        input.setMaxHypothesesNumber(10000);
        input.setMinPrecision(0.95);
        input.setUseCleaning(true);
        input.setDlminerMode(DLMinerMode.CDL);

        OperatorConfig config = input.getConfig();
        config.minSupport = 1;

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
