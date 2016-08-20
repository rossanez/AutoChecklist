package com.autochecklist.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

public class HtmlBuilder {

	private static final String HTML_BASE_RESOURCE = "Output/base.html";
	private static final String HTML_SCRIPTS_RESOURCE = "Output/scripts.html";
	
	private String mFileName;

	public HtmlBuilder(String fileName) {
		mFileName = fileName;
	}

	public static String generateContent(String title, String body, boolean scriptSupport) {
		try {
			return generateContent_internal(title, body, scriptSupport);
		} catch (IOException e) {
			Utils.printError("Unable to generate content! - " + e.getMessage());
			throw new RuntimeException("Unable to generate content! - " + e.getMessage());
		}
	}

	private static String generateContent_internal(String title, String body, boolean scriptSupport) throws IOException {
		String htmlString = Utils.getResourceAsString(HTML_BASE_RESOURCE);

		htmlString = htmlString.replace("$scripts", scriptSupport ?
				Utils.getResourceAsString(HTML_SCRIPTS_RESOURCE) : "");

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
			Utils.printError("No content to be built!");
			throw new RuntimeException("No file name passed in!");
		}
		
		try {
			build_internal(content);
			return mFileName;
		} catch (IOException e) {
			Utils.printError("Unable to generate output file! - " + e.getMessage());
			throw new RuntimeException("Unable to generate output file! - " + e.getMessage());
		}
	}
	
	private void build_internal(String content) throws IOException {
		File newHtmlFile = new File(mFileName);
		FileUtils.writeStringToFile(newHtmlFile, content);
	}

	public static String removeScriptsfromContent(String htmlContent) {
    	Document doc = Jsoup.parse(htmlContent);

    	Document.OutputSettings settings = doc.outputSettings();
    	settings.prettyPrint(false);
    	settings.escapeMode(Entities.EscapeMode.extended);
    	settings.charset("ASCII");

    	String title = doc.title();
    	String body = doc.body().html();
    
    	String retContent = Utils.getResourceAsString(HTML_BASE_RESOURCE);
    	retContent = retContent.replace("$scripts", "");
    	retContent = retContent.replace("$title", title);
    	retContent = retContent.replace("$body", body);
    	return retContent;
    }
}
