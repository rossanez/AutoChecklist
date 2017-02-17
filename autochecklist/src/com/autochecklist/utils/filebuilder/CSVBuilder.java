package com.autochecklist.utils.filebuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.autochecklist.utils.Utils;
import com.opencsv.CSVReader;
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
    	CSVWriter writer;
    	try {
    		writer = new CSVWriter(new FileWriter(fileName));
            writer.writeNext(HEADER);

			for (String[] row : contents) {
                writer.writeNext(row);
			}
			
			writer.close();
		} catch (IOException e) {
			Utils.printError("Unable to save CSV file!");
		}
    }

    public static String[][] loadFile(String fileName) {
    	String[][] contentsArr = null;
    	List<String[]> contents = new ArrayList<String[]>();
    	CSVReader reader;
    	try {
    		reader = new CSVReader(new FileReader(fileName));

    		String[] row = reader.readNext(); // First one is the HEADER.
    		while ((row = reader.readNext()) != null) {
                contents.add(row);
    		}

    		contentsArr = new String[contents.size()][];
    		contentsArr = contents.toArray(contentsArr);
    	} catch (IOException e) {
    		Utils.printError("Unable to read CSV file!");
    	}

    	return contentsArr;
    }
}
