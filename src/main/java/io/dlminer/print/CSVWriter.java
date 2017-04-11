package io.dlminer.print;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.dlminer.exp.CDLOutput;
import io.dlminer.exp.GSOutput;
import org.semanticweb.owlapi.model.OWLOntology;
import io.dlminer.learn.Hypothesis;
import io.dlminer.main.DLMinerStats;
import io.dlminer.ont.AxiomMetric;
import io.dlminer.ont.OntologyAnalyser;
import io.dlminer.ont.OntologyHandler;

public class CSVWriter {	
	
	private FileWriter fileWriter;
	
	
	public CSVWriter(File file, boolean append) {		
		// If the file doesn't exist then create it
		try {
			File parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
			if (!file.exists()) {				
				file.createNewFile();				
			}
			fileWriter = new FileWriter(file, append);			
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
		
			
	public static void write(String[][] arr, String fileName, String fileHeader) {
		String workingDir = System.getProperty("user.dir");
		File file = new File(workingDir + "/reports/"+fileName+".csv");
		Out.p("\nWriting to " + file);
		FileWriter fileWriter = null;
		try {
			// If the file doesn't exist then create it
			if (!file.exists()) {
				file.createNewFile();
			}			
			fileWriter = new FileWriter(file);			
			// Write the CSV file header
			fileWriter.append(fileHeader);			
			// Add a new line separator after the header
			fileWriter.append(CSV.NEW_LINE_SEPARATOR);			
			for (int i=0; i<arr.length; i++) {				
				for (int j=0; j<arr[0].length; j++) {
					fileWriter.append(arr[i][j]);
					fileWriter.append(CSV.COMMA_DELIMITER);
				}
				fileWriter.append(CSV.NEW_LINE_SEPARATOR);
			}			
		} catch (Exception e) {			
			e.printStackTrace();
		} finally {			
			close(fileWriter);			             
		}
	}
	
	
	
	public static String combineHeader(int nMetrics) {
		String header = "\\rotatebox{90}{Ontology}" + CSV.COMMA_DELIMITER
				+ "\\rotatebox{90}{DL expressivity}" + CSV.COMMA_DELIMITER;
		for (int i=0; i<nMetrics; i++) {
			header += (OntologyAnalyser.METRIC_NAMES[i] + CSV.COMMA_DELIMITER);
		}
		header += "\\rotatebox{90}{Time (ms)}";		
		return header;
	}
	
	
	
	public void append(String[] entry) {		
		try {											
			for (int j=0; j<entry.length; j++) {
				fileWriter.append(entry[j]);
				fileWriter.append(CSV.COMMA_DELIMITER);
			}
			fileWriter.append(CSV.NEW_LINE_SEPARATOR);			
		} catch (Exception e) {			
			e.printStackTrace();
			close();	
		}
	}
	
	
	
	public void append(List<String[]> entries) {		
		try {					
			for (String[] entry : entries) {				
				for (int j=0; j<entry.length; j++) {
					fileWriter.append(entry[j]);
					fileWriter.append(CSV.COMMA_DELIMITER);
				}
				fileWriter.append(CSV.NEW_LINE_SEPARATOR);
			}
			
		} catch (Exception e) {			
			e.printStackTrace();
			close();	
		}
	}
	
	
	public static void close(FileWriter fileWriter) {
		try {
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {				
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {				
			e.printStackTrace();
		}
	}
	
	
	
	public static void writeWithRowNames(String[][] arr, String fileName,
			String fileHeader, String[] rowNames) {
		String workingDir = System.getProperty("user.dir");
		File file = new File(workingDir + "/reports/"+fileName+".csv");
		FileWriter fileWriter = null;
		try {
			// If the file doesn't exist then create it
			if (!file.exists()) {
				file.createNewFile();
			}			
			fileWriter = new FileWriter(file);			
			// Write the CSV file header
			fileWriter.append(fileHeader);			
			// Add a new line separator after the header
			fileWriter.append(CSV.NEW_LINE_SEPARATOR);			
			for (int i=0; i<arr.length; i++) {
				fileWriter.append(rowNames[i]);
				fileWriter.append(CSV.COMMA_DELIMITER);
				for (int j=0; j<arr[0].length; j++) {
					fileWriter.append(arr[i][j]);
					fileWriter.append(CSV.COMMA_DELIMITER);
				}
				fileWriter.append(CSV.NEW_LINE_SEPARATOR);
			}			
		} catch (Exception e) {			
			e.printStackTrace();
		} finally {			
			close(fileWriter);			             
		}
	}
	
	
		
	public void saveHypothesesToCSV(Collection<Hypothesis> hypotheses, 
			String ontName) {				
		List<String[]> entries = new ArrayList<>();
		// save to csv all hypotheses
		for (Hypothesis h : hypotheses) {
			
			String[] entry = new String[61];
			
			entry[0] = ontName;			
			entry[1] = h.id;			
			
			// cheap measures
			entry[2] = Double.toString(h.support);
			entry[3] = Double.toString(h.assumption);
			entry[4] = Double.toString(h.precision);
			entry[5] = Double.toString(h.recall);
			entry[6] = Double.toString(h.lift);
			entry[7] = Double.toString(h.leverage);
			entry[8] = Double.toString(h.addedValue);
			entry[9] = Double.toString(h.jaccard);
			entry[10] = Double.toString(h.certaintyFactor);
			entry[11] = Double.toString(h.klosgen);
			entry[12] = Double.toString(h.conviction);			
			entry[13] = Double.toString(h.shapiro);
			entry[14] = Double.toString(h.cosine);
			entry[15] = Double.toString(h.informGain);
			entry[16] = Double.toString(h.sebag);
			entry[17] = Double.toString(h.contradiction);
			entry[18] = Double.toString(h.oddMultiplier);
			entry[19] = Double.toString(h.linearCorrelation);
			entry[20] = Double.toString(h.jmeasure);
			entry[21] = h.noveltyApprox == null ? CSV.NULL_VALUE : Double.toString(h.noveltyApprox);					
			entry[22] = h.dissimilarityApprox == null ? CSV.NULL_VALUE : Double.toString(h.dissimilarityApprox);
			
			// costly measures
			entry[23] = h.fitness == null ? CSV.NULL_VALUE : Double.toString(h.fitness);
			entry[24] = h.braveness == null ? CSV.NULL_VALUE : Double.toString(h.braveness);
			entry[25] = h.novelty == null ? CSV.NULL_VALUE : Double.toString(h.novelty);
			entry[26] = h.dissimilarity == null ? CSV.NULL_VALUE : Double.toString(h.dissimilarity);
			entry[27] = h.strength == null ? CSV.NULL_VALUE : Double.toString(h.strength);
			entry[28] = h.rank == null ? CSV.NULL_VALUE : Double.toString(h.rank);
			
			// comparison
			entry[29] = h.fitnessSum == null ? CSV.NULL_VALUE : Double.toString(h.fitnessSum);
			entry[30] = h.bravenessSum == null ? CSV.NULL_VALUE : Double.toString(h.bravenessSum);
			entry[31] = h.noveltySum == null ? CSV.NULL_VALUE : Double.toString(h.noveltySum);					
		
			// contrapositive measures
			entry[32] = h.mainSupport == null ? CSV.NULL_VALUE : Double.toString(h.mainSupport);
			entry[33] = h.mainAssumption == null ? CSV.NULL_VALUE : Double.toString(h.mainAssumption);
			entry[34] = h.mainContradiction == null ? CSV.NULL_VALUE : Double.toString(h.mainContradiction);
			entry[35] = h.mainPrecision == null ? CSV.NULL_VALUE : Double.toString(h.mainPrecision);
			entry[36] = h.mainLift == null ? CSV.NULL_VALUE : Double.toString(h.mainLift);
			entry[37] = h.mainConvictionNeg == null ? CSV.NULL_VALUE : Double.toString(h.mainConvictionNeg);
			entry[38] = h.mainConvictionQue == null ? CSV.NULL_VALUE : Double.toString(h.mainConvictionQue);
			entry[39] = h.convictionNeg == null ? CSV.NULL_VALUE : Double.toString(h.convictionNeg);
			entry[40] = h.convictionQue == null ? CSV.NULL_VALUE : Double.toString(h.convictionQue);
			
			// performance
			entry[41] = h.fitnessTime == null ? CSV.NULL_VALUE : Double.toString(h.fitnessTime);
			entry[42] = h.bravenessTime == null ? CSV.NULL_VALUE : Double.toString(h.bravenessTime);
			entry[43] = h.noveltyTime == null ? CSV.NULL_VALUE : Double.toString(h.noveltyTime);
			entry[44] = h.dissimTime == null ? CSV.NULL_VALUE : Double.toString(h.dissimTime);
			entry[45] = h.strengthTime == null ? CSV.NULL_VALUE : Double.toString(h.strengthTime);
			entry[46] = h.basicTime == null ? CSV.NULL_VALUE : Double.toString(h.basicTime);
			entry[47] = h.mainTime == null ? CSV.NULL_VALUE : Double.toString(h.mainTime);
			entry[48] = h.consistTime == null ? CSV.NULL_VALUE : Double.toString(h.consistTime);
			entry[49] = h.informTime == null ? CSV.NULL_VALUE : Double.toString(h.informTime);
			entry[50] = h.cleanTime == null ? CSV.NULL_VALUE : Double.toString(h.cleanTime);
			
			// metrics
			entry[51] = Integer.toString(h.length);
			entry[52] = Integer.toString(h.depth);
			entry[53] = Integer.toString(h.axioms.size());
			entry[54] = Integer.toString(h.countRoleAxioms());
			entry[55] = Integer.toString(h.signature.size());			
			entry[56] = Integer.toString(AxiomMetric.countConjunctions(h.axioms));
			entry[57] = Integer.toString(AxiomMetric.countDisjunctions(h.axioms));
			entry[58] = Integer.toString(AxiomMetric.countNegations(h.axioms));
			entry[59] = Integer.toString(AxiomMetric.countExistentials(h.axioms));
			entry[60] = Integer.toString(AxiomMetric.countUniversals(h.axioms));
			
			entries.add(entry);
		}
		append(entries);
	}


	public void createHypothesisHeader() {
		// default file header
		String fileHeader = 			
				"ontology" + CSV.COMMA_DELIMITER + 				
				"id" + CSV.COMMA_DELIMITER + 				
				// cheap measures				
				"support"  + CSV.COMMA_DELIMITER + 
				"assumption"  + CSV.COMMA_DELIMITER + 
				"precision"  + CSV.COMMA_DELIMITER + 
				"recall"  + CSV.COMMA_DELIMITER + 
				"lift"  + CSV.COMMA_DELIMITER + 
				"leverage"  + CSV.COMMA_DELIMITER + 
				"added_value"  + CSV.COMMA_DELIMITER + 
				"jaccard"  + CSV.COMMA_DELIMITER + 
				"certainty_factor"  + CSV.COMMA_DELIMITER + 
				"klosgen"  + CSV.COMMA_DELIMITER + 
				"conviction"  + CSV.COMMA_DELIMITER + 				
				"piatetsky-shapiro"  + CSV.COMMA_DELIMITER + 				
				"cosine"  + CSV.COMMA_DELIMITER + 				
				"information_gain"  + CSV.COMMA_DELIMITER + 
				"sebag-schoenauer"  + CSV.COMMA_DELIMITER + 
				"least_contradiction"  + CSV.COMMA_DELIMITER + 
				"odd_multiplier"  + CSV.COMMA_DELIMITER + 
				"linear_correlation"  + CSV.COMMA_DELIMITER + 
				"j-measure"  + CSV.COMMA_DELIMITER +
				"novelty_approx" + CSV.COMMA_DELIMITER + 
				"dissimilarity_approx" + CSV.COMMA_DELIMITER + 
				// costly measures
				"fitness"  + CSV.COMMA_DELIMITER + 
				"braveness" + CSV.COMMA_DELIMITER +
				"novelty" + CSV.COMMA_DELIMITER +
				"dissimilarity" + CSV.COMMA_DELIMITER +
				"strength_rank" + CSV.COMMA_DELIMITER +
				"dom_rank" + CSV.COMMA_DELIMITER +
				// comparison				
				"fitness_sum" + CSV.COMMA_DELIMITER + 
				"braveness_sum" + CSV.COMMA_DELIMITER + 
				"novelty_sum" + CSV.COMMA_DELIMITER + 
				// contrapositive measures
				"main_support" + CSV.COMMA_DELIMITER + 
				"main_assumption" + CSV.COMMA_DELIMITER + 
				"main_contradiction" + CSV.COMMA_DELIMITER + 
				"main_precision" + CSV.COMMA_DELIMITER + 
				"main_lift" + CSV.COMMA_DELIMITER + 
				"main_conviction_neg" + CSV.COMMA_DELIMITER + 
				"main_conviction_que" + CSV.COMMA_DELIMITER + 
				"conviction_neg"  + CSV.COMMA_DELIMITER + 
				"conviction_que"  + CSV.COMMA_DELIMITER + 
				// performance
				"fitness_time" + CSV.COMMA_DELIMITER + 
				"braveness_time" + CSV.COMMA_DELIMITER + 
				"novelty_time" + CSV.COMMA_DELIMITER + 
				"dissim_time" + CSV.COMMA_DELIMITER + 
				"strength_time" + CSV.COMMA_DELIMITER + 
				"basic_time" + CSV.COMMA_DELIMITER + 
				"main_time" + CSV.COMMA_DELIMITER + 
				"consist_time" + CSV.COMMA_DELIMITER + 
				"inform_time" + CSV.COMMA_DELIMITER + 
				"clean_time" + CSV.COMMA_DELIMITER + 
				// axiom metrics				
				"length" + CSV.COMMA_DELIMITER + 
				"depth" + CSV.COMMA_DELIMITER + 
				"axiom_num" + CSV.COMMA_DELIMITER +
				"role_axiom_num" + CSV.COMMA_DELIMITER + 
				"signature" + CSV.COMMA_DELIMITER +				
				"conj_num" + CSV.COMMA_DELIMITER + 
				"disj_num" + CSV.COMMA_DELIMITER + 
				"neg_num" + CSV.COMMA_DELIMITER + 
				"exist_num" + CSV.COMMA_DELIMITER + 
				"univ_num"
				;		
		try {
			// Write the CSV file header
			fileWriter.append(fileHeader);
			// Add a new line separator after the header
			fileWriter.append(CSV.NEW_LINE_SEPARATOR);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}


	public void createStatsHeader() {
		// default file header
		String fileHeader = 				
				"ontology" + CSV.COMMA_DELIMITER + 
				// ontology metrics
				OntologyAnalyser.METRIC_NAMES[0] + CSV.COMMA_DELIMITER + 
				OntologyAnalyser.METRIC_NAMES[1] + CSV.COMMA_DELIMITER + 
				OntologyAnalyser.METRIC_NAMES[2] + CSV.COMMA_DELIMITER + 
				OntologyAnalyser.METRIC_NAMES[3] + CSV.COMMA_DELIMITER +  
				OntologyAnalyser.METRIC_NAMES[4] + CSV.COMMA_DELIMITER + 
				OntologyAnalyser.METRIC_NAMES[5] + CSV.COMMA_DELIMITER + 
				OntologyAnalyser.METRIC_NAMES[6] + CSV.COMMA_DELIMITER + 
				OntologyAnalyser.METRIC_NAMES[7] + CSV.COMMA_DELIMITER + 
				OntologyAnalyser.METRIC_NAMES[8] + CSV.COMMA_DELIMITER + 
				// stats
				"ont_parse_time" + CSV.COMMA_DELIMITER + 
				"ont_reason_time" + CSV.COMMA_DELIMITER + 
				"conc_build_time" + CSV.COMMA_DELIMITER + 
				"role_build_time" + CSV.COMMA_DELIMITER + 
				"hypo_build_time" + CSV.COMMA_DELIMITER + 
				"hypo_clean_time" + CSV.COMMA_DELIMITER + 
				"basic_meas_time" + CSV.COMMA_DELIMITER + 
				"contr_meas_time" + CSV.COMMA_DELIMITER + 
				"compl_meas_time" + CSV.COMMA_DELIMITER + 
				"compl_meas_prec_time" + CSV.COMMA_DELIMITER + 
				"strength_time" + CSV.COMMA_DELIMITER + 
				"dominance_time" + CSV.COMMA_DELIMITER + 
				"consist_time" + CSV.COMMA_DELIMITER + 
				"conc_num" + CSV.COMMA_DELIMITER + 
				"role_num" + CSV.COMMA_DELIMITER + 
				"hypo_num"
				;		
		try {
			// Write the CSV file header
			fileWriter.append(fileHeader);
			// Add a new line separator after the header
			fileWriter.append(CSV.NEW_LINE_SEPARATOR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void saveStatsToCSV(String ontName, DLMinerStats stats, OWLOntology ontology) {
		// extract ontology metrics
		OntologyHandler handler = new OntologyHandler(ontology);
		OntologyAnalyser analyser = new OntologyAnalyser(handler);
		String[] metrics = analyser.extractMetrics();		
		int mnum = metrics.length;
		String[] entry = new String[mnum+17];		
		entry[0] = ontName;
		// save metrics
		for (int i=0; i<mnum; i++) {
			entry[i+1] = metrics[i];
		}
		// save stats
		entry[mnum+1] = stats.getOntologyParsingTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getOntologyParsingTime());
		entry[mnum+2] = stats.getOntologyReasoningTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getOntologyReasoningTime());
		entry[mnum+3] = stats.getConceptBuildingTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getConceptBuildingTime());
		entry[mnum+4] = stats.getRoleBuildingTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getRoleBuildingTime());
		entry[mnum+5] = stats.getHypothesesBuildingTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getHypothesesBuildingTime());
		entry[mnum+6] = stats.getHypothesesCleaningTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getHypothesesCleaningTime());
		entry[mnum+7] = stats.getBasicMeasuresTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getBasicMeasuresTime());
		entry[mnum+8] = stats.getContraMeasuresTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getContraMeasuresTime());
		entry[mnum+9] = stats.getComplexMeasuresTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getComplexMeasuresTime());
		entry[mnum+10] = stats.getComplexMeasuresPrecompTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getComplexMeasuresPrecompTime());
		entry[mnum+11] = stats.getStrengthTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getStrengthTime());
		entry[mnum+12] = stats.getDominanceTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getDominanceTime());
		entry[mnum+13] = stats.getConsistencyTime() == null ? CSV.NULL_VALUE : Double.toString(stats.getConsistencyTime());
		entry[mnum+14] = Double.toString(stats.getConceptsNumber());
		entry[mnum+15] = Double.toString(stats.getRolesNumber());
		entry[mnum+16] = Double.toString(stats.getHypothesesNumber());
		append(entry);
	}



	public void createPredictionHeader() {
		// default file header
		String fileHeader = 
				// accuracy measures
				"min_confidence" + CSV.COMMA_DELIMITER + 
				"min_support" + CSV.COMMA_DELIMITER + 
				"number" + CSV.COMMA_DELIMITER + 
				"train_number" + CSV.COMMA_DELIMITER + 				
				"train_FP" + CSV.COMMA_DELIMITER + 
				"train_FN" + CSV.COMMA_DELIMITER + 
				"train_TP" + CSV.COMMA_DELIMITER + 
				"train_TN" + CSV.COMMA_DELIMITER + 
				"train_error" + CSV.COMMA_DELIMITER + 				
				"test_number" + CSV.COMMA_DELIMITER + 				
				"test_FP" + CSV.COMMA_DELIMITER + 
				"test_FN" + CSV.COMMA_DELIMITER + 
				"test_TP" + CSV.COMMA_DELIMITER + 
				"test_TN" + CSV.COMMA_DELIMITER + 
				"test_error" + CSV.COMMA_DELIMITER + 
				"clashes" + CSV.COMMA_DELIMITER + 
				"train_clashes" + CSV.COMMA_DELIMITER + 
				"test_clashes" + CSV.COMMA_DELIMITER + 
				"train_recall" + CSV.COMMA_DELIMITER + 
				"train_precision" + CSV.COMMA_DELIMITER + 
				"train_F1" + CSV.COMMA_DELIMITER + 
				"test_recall" + CSV.COMMA_DELIMITER + 
				"test_precision" + CSV.COMMA_DELIMITER + 
				"test_F1"  
				/*+ CSV.COMMA_DELIMITER + 
				"gtrain_FP" + CSV.COMMA_DELIMITER + 
				"gtrain_FN" + CSV.COMMA_DELIMITER + 
				"gtrain_TP" + CSV.COMMA_DELIMITER + 
				"gtrain_TN" + CSV.COMMA_DELIMITER + 
				"gtrain_error" + CSV.COMMA_DELIMITER + 
				"gtest_FP" + CSV.COMMA_DELIMITER + 
				"gtest_FN" + CSV.COMMA_DELIMITER + 
				"gtest_TP" + CSV.COMMA_DELIMITER + 
				"gtest_TN" + CSV.COMMA_DELIMITER + 
				"gtest_error" + CSV.COMMA_DELIMITER + 
				"guesses" + CSV.COMMA_DELIMITER + 
				"train_guesses" + CSV.COMMA_DELIMITER + 
				"test_guesses" + CSV.COMMA_DELIMITER + 
				"gtrain_recall" + CSV.COMMA_DELIMITER + 
				"gtrain_precision" + CSV.COMMA_DELIMITER + 
				"gtrain_F1" + CSV.COMMA_DELIMITER + 
				"gtest_recall" + CSV.COMMA_DELIMITER + 
				"gtest_precision" + CSV.COMMA_DELIMITER + 
				"gtest_F1"*/
				;		
		try {
			// Write the CSV file header
			fileWriter.append(fileHeader);
			// Add a new line separator after the header
			fileWriter.append(CSV.NEW_LINE_SEPARATOR);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}



	public void savePredictionsToCSV(double[][] result) {
		List<String[]> entries = new ArrayList<>();
		for (int i=0; i<result.length; i++) {
			String[] entry = new String[result[0].length];			
			for (int j=0; j<result[0].length; j++) {
				entry[j] = Double.toString(result[i][j]);
			}
			entries.add(entry);
		}
		append(entries);
	}



	public void createMetricsHeader() {
		// default file header
		String fileHeader = "ontology" + CSV.COMMA_DELIMITER;		
		for (String metricName : OntologyAnalyser.METRIC_NAMES) {
			fileHeader += metricName + CSV.COMMA_DELIMITER;
		}
		fileHeader = fileHeader + 
				"ch_time" + CSV.COMMA_DELIMITER + 
				"ir_time" + CSV.COMMA_DELIMITER + 
				"exception";
		try {
			// Write the CSV file header
			fileWriter.append(fileHeader);
			// Add a new line separator after the header
			fileWriter.append(CSV.NEW_LINE_SEPARATOR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public void saveMetricsToCSV(String ontName, String[] metrics, 
			double chTime, double irTime, String exception) {		
		String[] entry = new String[metrics.length + 4];
		entry[0] = ontName;
		for (int i=0; i<metrics.length; i++) {
			entry[i+1] = metrics[i];
		}
		entry[entry.length-3] = Double.toString(chTime);
		entry[entry.length-2] = Double.toString(irTime);
		entry[entry.length-1] = exception;
		append(entry);
	}



	
	public void createCDLStatsHeader() {
		// default file header
		String fileHeader = 				
				"ontology" + CSV.COMMA_DELIMITER + 								
				"hypo_num" + CSV.COMMA_DELIMITER + 
				"def_num" + CSV.COMMA_DELIMITER + 
				"at_def_num" + CSV.COMMA_DELIMITER + 
				"com_def_num" + CSV.COMMA_DELIMITER + 				
				"hypo_time" + CSV.COMMA_DELIMITER + 
				"def_time"  + CSV.COMMA_DELIMITER + 
				"def_ent_num" + CSV.COMMA_DELIMITER + 
				"def_ent_recall"  + CSV.COMMA_DELIMITER + 
				"def_ent_length"  + CSV.COMMA_DELIMITER + 
				"at_def_ent_num" + CSV.COMMA_DELIMITER + 
				"com_def_ent_num" + CSV.COMMA_DELIMITER + 
				"tbox_def_ent_num" + CSV.COMMA_DELIMITER + 
				"tbox_def_ent_length" + CSV.COMMA_DELIMITER + 
				"at_tbox_def_ent_num" + CSV.COMMA_DELIMITER + 
				"com_tbox_def_ent_num" + CSV.COMMA_DELIMITER + 
				"sub_super_class_def_num"  + CSV.COMMA_DELIMITER + 
				"only_super_class_def_num"  + CSV.COMMA_DELIMITER + 
				"only_sub_class_def_num"  + CSV.COMMA_DELIMITER + 
				"undef_class_num"  + CSV.COMMA_DELIMITER + 
				"ent_hypos_num" + CSV.COMMA_DELIMITER + 
				"ent_hypos_length"
				;		
		try {
			// Write the CSV file header
			fileWriter.append(fileHeader);
			// Add a new line separator after the header
			fileWriter.append(CSV.NEW_LINE_SEPARATOR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public void createGSStatsHeader() {
		// default file header
		String fileHeader = 				
				"ontology" + CSV.COMMA_DELIMITER + 	
				"hypo_num" + CSV.COMMA_DELIMITER + 						
				"gs_axiom_num" + CSV.COMMA_DELIMITER + 	
				"gs_axiom_ent_num" + CSV.COMMA_DELIMITER + 
				"gs_axiom_ent_tbox_num" + CSV.COMMA_DELIMITER +
				"recall"
				;		
		try {
			// Write the CSV file header
			fileWriter.append(fileHeader);
			// Add a new line separator after the header
			fileWriter.append(CSV.NEW_LINE_SEPARATOR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


		
	public static void saveHypothesesToCSV(Collection<Hypothesis> hypotheses, 
			File csvFile, String ontName) {
		// create a new file		
		CSVWriter csvWriter = null;
		if (!csvFile.exists()) {			
			try {
				// create a new file
				csvFile.createNewFile();	
				csvWriter = new CSVWriter(csvFile, false);
				csvWriter.createHypothesisHeader();
				csvWriter.saveHypothesesToCSV(hypotheses, ontName);
				csvWriter.close();
			} catch (Exception e) {			
				e.printStackTrace();
			}
		} else {
			// add lines without erasing contents
			csvWriter = new CSVWriter(csvFile, true);		
			csvWriter.saveHypothesesToCSV(hypotheses, ontName);
			csvWriter.close();
		}
		
	}
	
	
	
	public static void saveStatsToCSV(File csvStatsFile, String ontName,
			DLMinerStats stats, OWLOntology ontology) {
		CSVWriter csvWriter = null;
		if (!csvStatsFile.exists()) {			
			try {
				// create a new file
				csvStatsFile.createNewFile();	
				csvWriter = new CSVWriter(csvStatsFile, false);
				csvWriter.createStatsHeader();
				csvWriter.saveStatsToCSV(ontName, stats, ontology);
				csvWriter.close();
			} catch (Exception e) {			
				e.printStackTrace();
			}
		} else {
			// add lines without erasing contents
			csvWriter = new CSVWriter(csvStatsFile, true);		
			csvWriter.saveStatsToCSV(ontName, stats, ontology);
			csvWriter.close();
		}
	}



    public void saveGSStatsToCSV(String ontName, GSOutput gsOutput) {
        String[] entry = new String[6];
        entry[0] = ontName;
        entry[1] = Integer.toString(gsOutput.hypoNum);
        entry[2] = Integer.toString(gsOutput.gsAxiomNum);
        entry[3] = Integer.toString(gsOutput.gsAxiomEntNum);
        entry[4] = Double.toString(gsOutput.gsAxiomEntTBoxNum);
        entry[5] = Double.toString(gsOutput.recall);
        append(entry);
    }



    public void saveCDLStatsToCSV(String ontName, CDLOutput cdlOutput) {
        String[] entry = new String[22];
        entry[0] = ontName;
        entry[1] = Integer.toString(cdlOutput.hypoNumber);
        entry[2] = Integer.toString(cdlOutput.defNumber);
        entry[3] = Double.toString(cdlOutput.atomDefNumer);
        entry[4] = Double.toString(cdlOutput.complDefNumer);
        entry[5] = Double.toString(cdlOutput.hypoTime);
        entry[6] = Double.toString(cdlOutput.defTime);
        entry[7] = Double.toString(cdlOutput.defEntailedNumber);
        entry[8] = Double.toString(cdlOutput.defEntailedRecall);
        entry[9] = Double.toString(cdlOutput.defEntailedLength);
        entry[10] = Double.toString(cdlOutput.atomDefEntailedNumber);
        entry[11] = Double.toString(cdlOutput.complDefEntailedNumber);
        entry[12] = Double.toString(cdlOutput.defEntailedTBoxNumber);
        entry[13] = Double.toString(cdlOutput.defEntailedTBoxLength);
        entry[14] = Double.toString(cdlOutput.atomDefEntailedTBoxNumber);
        entry[15] = Double.toString(cdlOutput.complDefEntailedTBoxNumber);
        entry[16] = Double.toString(cdlOutput.subAndSuperClassDefinedNumber);
        entry[17] = Double.toString(cdlOutput.onlySuperClassDefinedNumber);
        entry[18] = Double.toString(cdlOutput.onlySubClassDefinedNumber);
        entry[19] = Double.toString(cdlOutput.classUndefinedNumber);
        entry[20] = Double.toString(cdlOutput.entHyposNumber);
        entry[21] = Double.toString(cdlOutput.entHyposLength);
        append(entry);
    }





	
}
