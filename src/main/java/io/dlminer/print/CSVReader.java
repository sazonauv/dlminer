package io.dlminer.print;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CSVReader {
	
	
	
	
	public static List<String[]> read(File file, boolean hasHeader) {
		BufferedReader reader = null;
		List<String[]> result = new ArrayList<>();
		try {
			if (file.exists()) {			
				reader = new BufferedReader(new FileReader(file));
                String line;
				if (hasHeader) {
                    line = reader.readLine();
                }
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
	
	


}
