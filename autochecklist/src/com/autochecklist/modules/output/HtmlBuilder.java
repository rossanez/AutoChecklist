package com.autochecklist.modules.output;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.autochecklist.utils.Utils;

public class HtmlBuilder {

	private String mFileName;

	public HtmlBuilder(String fileName) {
		mFileName = fileName;
	}

	public static String generateContent(String title, String body) {
		try {
			return generateContent_internal(title, body);
		} catch (IOException e) {
			throw new RuntimeException("Unable to generate content! - " + e.getMessage());
		}
	}

	private static String generateContent_internal(String title, String body) throws IOException {
		File htmlTemplateFile = new File("res/Output/simple.html");
		String htmlString = FileUtils.readFileToString(htmlTemplateFile);
		htmlString = htmlString.replace("$title", title);
		
		body = body.replace("\n", "<br>");
		
		body = body.replace(" --- ", "<h3>");
		body = body.replace(" /--- ", "</h3>");

		body = body.replace("- Answer: Yes", "<p> &rarr; <i>Answer:</i> <b><font color='green'>YES</font></b>");
		body = body.replace("- Answer: No", "<p> &rarr; <i>Answer:</i> <b><font color='red'>NO</font></b>");
		body = body.replace("- Answer: Warning", "<p> &rarr; <i>Answer:</i> <b><font color='orange'>WARNING</font></b>");

		body = body.replace("- Value: ", "<p> &rarr; <b>Value: </b>");

		body = body.replace(" -- ", " &bull; ");
	
		body = body.replace(" ---- ", "&mdash;");

		body = "<h2>" + title + "</h2>" + body;

		htmlString = htmlString.replace("$body", body);

		return htmlString;
	}

	public String build(String content) {
		if (Utils.isTextEmpty(mFileName)) {
			throw new RuntimeException("No file name passed in!");
		}
		
		try {
			build_internal(content);
			return mFileName;
		} catch (IOException e) {
			throw new RuntimeException("Unable to generate output file! - " + e.getMessage());
		}
	}
	
	private void build_internal(String content) throws IOException {
		File newHtmlFile = new File(mFileName);
		FileUtils.writeStringToFile(newHtmlFile, content);
	}
}
