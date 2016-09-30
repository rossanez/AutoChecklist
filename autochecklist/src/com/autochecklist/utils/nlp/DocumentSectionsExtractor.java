package com.autochecklist.utils.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.autochecklist.utils.Utils;

public class DocumentSectionsExtractor {

	public void extract(String document) {
		BufferedReader bufReader = new BufferedReader(new StringReader(document));
		String line = null;
		try {
			while ((line = bufReader.readLine()) != null) {
				if (!Utils.isTextEmpty(line)) {
					String[] title = line.split(" ");
					if ((title == null) || (title.length < 2)) continue;
					if (!title[0].matches("[0-9]+(\\.[0-9]+)*")) continue;

					Utils.println(line);
				}
			}
		} catch (IOException e) {
			Utils.printError("Error when trying to extract the document sections!");
		}
	}
}
