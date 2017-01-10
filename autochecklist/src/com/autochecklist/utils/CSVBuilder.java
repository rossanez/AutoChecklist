package com.autochecklist.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVWriter;

/**
 * https://tools.ietf.org/html/rfc4180
 * Using OpenCSV library.
 */
public class CSVBuilder {

	public static final String[] HEADER = {
       "Req. ID", "Req. Text", "Question ID", "Generic Finding", "Specific Finding", "Automatic Answer", "Reviewed Answer", "Reviewer Comments"
	};

	public static void saveFile(String fileName, List<String[]> contents) {
		String[][] contentsArr = new String[contents.size()][];
		contentsArr = contents.toArray(contentsArr);
		saveFile(fileName, contentsArr);
	}

    public static void saveFile(String fileName, String[][] contents) {
    	try {
    		CSVWriter pw = new CSVWriter(new FileWriter(fileName));
            pw.writeNext(HEADER);

			for (String[] row : contents) {
                pw.writeNext(row);
			}
			
			pw.close();
		} catch (IOException e) {
			Utils.printError("Unable to save CSV file!");
		}
    }
}
