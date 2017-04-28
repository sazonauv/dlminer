package io.dlminer.exp;

import io.dlminer.print.CSVReader;
import io.dlminer.print.CSVWriter;
import io.dlminer.print.Out;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by slava on 28/04/17.
 */
public class DLAprioriCSVResultsProcessor {


    public static void main(String[] args) {
        File csvFile = new File(args[0]);
        File newCsvFile = new File(args[1]);

        List<String[]> rows = CSVReader.read(csvFile, false);
        List<String[]> newRows = new ArrayList<>();
        for (String[] row : rows) {
            newRows.add(processDLAprioriCSVRow(row));
        }

        CSVWriter csvWriter = new CSVWriter(newCsvFile, false);
        csvWriter.append(newRows);
        csvWriter.close();

        Out.p("\nAll is done.\n");
    }


    private static String[] processDLAprioriCSVRow(String[] row) {
        int partitions = 20;
        List<String> newRow = new ArrayList<>();
        newRow.add(row[0]);
        Out.p("Ontology: " + row[0]);
        // max number
        String lastCell = row[row.length-1];
        String[] lastSplits = lastCell.split("-");
        int maxInstNum = Integer.parseInt(lastSplits[0]);
        double step = (double)maxInstNum / partitions;
        step = (step == 0) ? 1 : step;

        int count = 0;
        for (int i=1; i<row.length; i++) {
            String cell = row[i];
            String[] splits = cell.split("-");
            int instNum = Integer.parseInt(splits[0]);

            if (instNum == 0 || instNum == 1) {
                newRow.add(splits[1]);
            } else {
                int currentCount = (int)(instNum / step);
                if (currentCount > count) {
                    for (int j=1; j<=currentCount-count-1; j++) {
                        newRow.add("");
                    }
                    newRow.add(splits[1]);
                    count = currentCount;
                }
            }
        }

        String[] newRowArray = new String[newRow.size()];
        return newRow.toArray(newRowArray);
    }


}
