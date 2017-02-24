package io.dlminer.print;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CSVReader {
	
	
	
	
	public static List<String[]> read(File file) {		
		BufferedReader reader = null;
		List<String[]> result = new ArrayList<>();
		try {
			if (file.exists()) {			
				reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				 // read the file line by line
	            while ((line = reader.readLine()) != null) {
	                // get all tokens available in line
	                String[] tokens = line.split(CSV.COMMA_DELIMITER);
	                result.add(tokens);
	            }
			}
		} catch (Exception e) {			
			e.printStackTrace();
		} finally {
            try {
            	reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	private static double[][] getTimeHeatMap(File dir) {
//		int len = DEF_LENGTH;	
		int[][] counts = new int[CSV.LANGUAGES.length][CSV.SIZES.length];
		double[][] averTimes = new double[counts.length][counts[0].length];		
		for (File csv : dir.listFiles()) {
			String[][] times = readTime(csv);
			double[][] values = getNormalisedValues(times);
			for (int i=0; i<values.length; i++) {
				for (int j=0; j<values[0].length; j++) {
					if (values[i][j] >= 0) {
						averTimes[i][j] += values[i][j];
						counts[i][j]++;
					}
				}
			}
		}
		for (int i=0; i<averTimes.length; i++) {
			for (int j=0; j<averTimes[0].length; j++) {
				averTimes[i][j] /= counts[i][j];
				averTimes[i][j] = 1 - averTimes[i][j];
			}
		}		
		return averTimes;
	}

	private static double[][] getBravenessHeatMap(File dir) {
//		int len = DEF_LENGTH;		
		int[][] counts = new int[CSV.LANGUAGES.length][CSV.SIZES.length];
		double[][] averBravs = new double[counts.length][counts[0].length];		
		for (File csv : dir.listFiles()) {
			String[][] bravs = readBraveness(csv);
			double[][] values = getNormalisedValues(bravs);
			for (int i=0; i<values.length; i++) {
				for (int j=0; j<values[0].length; j++) {
					if (values[i][j] >= 0) {
						averBravs[i][j] += values[i][j];
						counts[i][j]++;
					}
				}
			}
		}
		for (int i=0; i<averBravs.length; i++) {
			for (int j=0; j<averBravs[0].length; j++) {
				averBravs[i][j] /= counts[i][j];
				averBravs[i][j] = 1 - averBravs[i][j];
			}
		}		
		return averBravs;
	}

	private static double[][] getFitnessHeatMap(File dir) {
//		int len = DEF_LENGTH;				
		int[][] counts = new int[CSV.LANGUAGES.length][CSV.SIZES.length];
		double[][] averFits = new double[counts.length][counts[0].length];	
		for (File csv : dir.listFiles()) {
			String[][] fits = readFitness(csv);
			double[][] values = getNormalisedValues(fits);
			for (int i=0; i<values.length; i++) {
				for (int j=0; j<values[0].length; j++) {
					if (values[i][j] >= 0) {
						averFits[i][j] += values[i][j];
						counts[i][j]++;
					}
				}
			}
		}
		for (int i=0; i<averFits.length; i++) {
			for (int j=0; j<averFits[0].length; j++) {
				averFits[i][j] /= counts[i][j];
				averFits[i][j] = 1 - averFits[i][j];
			}
		}		
		return averFits;
	}

	private static double[][] getNormalisedValues(String[][] arr) {		
		double[][] res = new double[arr.length][arr[0].length];
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				if (arr[i][j] != null) {
					res[i][j] = Double.valueOf(arr[i][j]);
				} else {
					res[i][j] = -1;
				}
			}
		}
		// find maximum
		double max = 0;
		for (int i=0; i<res.length; i++) {
			for (int j=0; j<res[0].length; j++) {
				if (max < res[i][j]) {
					max = res[i][j];
				}
			}
		}
		// normalise
		if (max > 0) {
			for (int i=0; i<res.length; i++) {
				for (int j=0; j<res[0].length; j++) {
					res[i][j] /= max;
				}
			}
		}
		return res;
	}

	
	
	public static String[][] readFitness(File csv) {
		List<String[]> entries = read(csv);
//		int len = DEF_LENGTH;		
		String[][] bestFits = new String[CSV.LANGUAGES.length][CSV.SIZES.length];			
		String[] langStrings = CSV.LANGUAGES;
		List<String> languages = Arrays.asList(langStrings);
		String[] sizeStrings = CSV.SIZES;
		List<String> sizes = Arrays.asList(sizeStrings);
		String lang = "";
		String size = "";		
		for (String[] entry : entries) {							
			if (!entry[0].equals("") && !entry[1].equals("")) {
				lang = entry[0];
				size = entry[1];
				int i = languages.indexOf(lang);//getLanguage(lang, languages);
				int j = sizes.indexOf(size);				
				bestFits[i][j] = entry[5];				
			}						
		}
		return bestFits;
	}
	
	public static String[][] readBraveness(File csv) {
		List<String[]> entries = read(csv);
//		int len = DEF_LENGTH;		
		String[][] bestBravs = new String[CSV.LANGUAGES.length][CSV.SIZES.length];			
		String[] langStrings = CSV.LANGUAGES;
		List<String> languages = Arrays.asList(langStrings);
		String[] sizeStrings = CSV.SIZES;
		List<String> sizes = Arrays.asList(sizeStrings);
		String lang = "";
		String size = "";		
		for (String[] entry : entries) {							
			if (!entry[0].equals("") && !entry[1].equals("")) {
				lang = entry[0];
				size = entry[1];
				int i = languages.indexOf(lang);//getLanguage(lang, languages);
				int j = sizes.indexOf(size);				
				bestBravs[i][j] = entry[8];				
			}						
		}
		return bestBravs;
	}

	public static String[][] readTime(File csv) {
		List<String[]> entries = read(csv);
//		int len = DEF_LENGTH;		
		String[][] times = new String[CSV.LANGUAGES.length][CSV.SIZES.length];			
		String[] langStrings = CSV.LANGUAGES;
		List<String> languages = Arrays.asList(langStrings);
		String[] sizeStrings = CSV.SIZES;
		List<String> sizes = Arrays.asList(sizeStrings);
		String lang = "";
		String size = "";		
		for (String[] entry : entries) {							
			if (!entry[0].equals("") && !entry[1].equals("")) {
				lang = entry[0];
				size = entry[1];
				int i = languages.indexOf(lang);//getLanguage(lang, languages);
				int j = sizes.indexOf(size);				
				times[i][j] = entry[2];			
			}						
		}
		return times;
	}
	
	

	private static double min(String[][] arr) {
		double min = Double.MAX_VALUE;
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				if (arr[i][j] != null) {
					Double val = Double.valueOf(arr[i][j]);
					if (val < min) {
						min = val;
					}
				}
			}
		}
		return min;
	}
	
	private static double max(String[][] arr) {
		double max = -1;
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				if (arr[i][j] != null) {
					Double val = Double.valueOf(arr[i][j]);
					if (val > max) {
						max = val;
					}
				}
			}
		}
		return max;
	}
	
		
	private static void replaceNulls(String[][] arr, String str) {
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				if (arr[i][j] == null) {
					arr[i][j] = str;
				}
			}
		}
	}
	
	
	public static void printHeatMaps(File dir) {
		double[][] fitMap = getFitnessHeatMap(dir);
		double[][] braMap = getBravenessHeatMap(dir);
		double[][] timMap = getTimeHeatMap(dir);
		Out.p(CSV.SPLIT_LINE + "fitness heat map" + CSV.SPLIT_LINE + " \n" + Out.printArray(fitMap));
		Out.p(CSV.SPLIT_LINE + "braveness heat map" + CSV.SPLIT_LINE + " \n" + Out.printArray(braMap));
		Out.p(CSV.SPLIT_LINE + "time heat map" + CSV.SPLIT_LINE + " \n" + Out.printArray(timMap));
		String header = "Language " + CSV.COMMA_DELIMITER + 
				"2 " + CSV.COMMA_DELIMITER + 
				"4 " + CSV.COMMA_DELIMITER + 
				"6 " + CSV.COMMA_DELIMITER + 
				"8 ";
		String[] langs = new String[] {
			"G1 ", "G2 ", "G3 ", "G4 ", "G5 ", "G6", "G7"	
		};		
		CSVWriter.writeWithRowNames(toStringArray(fitMap), "fit_map", header, langs);
		CSVWriter.writeWithRowNames(toStringArray(braMap), "bra_map", header, langs);
		CSVWriter.writeWithRowNames(toStringArray(timMap), "tim_map", header, langs);
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			new IllegalArgumentException("Provide a folder path");
		}
		printCSVs(new File(args[0]));
		printHeatMaps(new File(args[0]));
	}
	
	
	
	public static void printCSVs(File dir) {
		for (File csv : dir.listFiles()) {
			readAndPrintCSV(csv);
		}
	}
	
	
	public static void readAndPrintCSV(File csv) {		
		String[][] bestFits = readFitness(csv);
		String[][] bestBravs = readBraveness(csv);
		String[][] times = readTime(csv);
		double minTime = min(times);
		double maxTime = max(times);
		String empty = CSV.EMPTY_VALUE;
		replaceNulls(bestFits, empty);
		replaceNulls(bestBravs, empty);
		replaceNulls(times, empty);		
		Out.p("\n" + CSV.SPLIT_LINE + csv.getName() + CSV.SPLIT_LINE);
		Out.p("fitness \n" + Out.printArray(bestFits));
		Out.p("braveness \n" + Out.printArray(bestBravs));
		Out.p("times \n" + Out.printArray(times));
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		if (minTime < maxTime) {
			Out.p("time = [" + df.format(minTime) + ", " + df.format(maxTime) + "]");
		}
	}
	
	
	public static String[][] toStringArray(double[][] arr) {
		String[][] strArr = new String[arr.length][arr[0].length];
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[0].length; j++) {
				strArr[i][j] = "" + arr[i][j];
			}
		}		
		return strArr;
	}
	

}
