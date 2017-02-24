package io.dlminer.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import io.dlminer.print.Out;


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
//          ontologyFile = new FileInputStream("/home/slava/PhD/Ontologies/Bioportal/selected/glyco.glycomics-ontology.1.orig.owl.xml");
//        	ontologyFile = new FileInputStream("/home/slava/Workspaces/Eclipse/ExperimentRunner/hpo/hpo_abox.owl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        DLMinerInput input = new DLMinerInput(ontologyFile);
        DLMiner miner = new DLMiner(input);
		try {
			miner.run();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		Out.p("\nSaving hypotheses");
		File hypothesesFile = new File(args[1]);
		miner.saveHypotheses(hypothesesFile);		
//		Out.p("\nHypothesis sorter test:");
//		HypothesisSorter sorter = new HypothesisSorter(miner.getHypothesesData());
//		sorter.sort();
//		sorter.test();		
		Out.p("\nStatistics:");
		Out.p(miner.getStats());
	}

}
