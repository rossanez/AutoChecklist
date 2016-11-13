package com.autochecklist.utils.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

public class DocumentSectionsExtractor {

	private String mPlainText;
    private IRequirementsInfoOutBuildable mOutputBuilder;
	private ExpressionExtractor mSectionIdExtractor;

	public DocumentSectionsExtractor(String text, IRequirementsInfoOutBuildable outputBuilder) {
		mPlainText = text;
		mOutputBuilder = outputBuilder;
		this.mSectionIdExtractor = new ExpressionExtractor("RegexRules/sectionid.rules");
	}
	
	public void extract() {
		Utils.println("Extracting the document sections...");
		
        List<String> sectionIds = new ArrayList<String>(); // To maintain the document order.
		Map<String, DocumentSection> sections = new HashMap<String, DocumentSection>();

		BufferedReader bufReader = new BufferedReader(new StringReader(mPlainText));
		String line = null;
		try {
			while ((line = bufReader.readLine()) != null) {
				if (!Utils.isTextEmpty(line)) {
					String[] titleCandidate = line.split(" ");
					if ((titleCandidate == null) || (titleCandidate.length < 2)) continue;
					List<Pair<String, String>> matched = mSectionIdExtractor.extract(titleCandidate[0]);
					if ((matched == null) || matched.isEmpty()) continue;
					if (Character.isDigit(titleCandidate[1].charAt(0))) continue;

					String sectionId = titleCandidate[0];
					if ("APPENDIX_ID".equals(matched.get(0).first)) {
						sectionId += " " + titleCandidate[1];
					}

					if (sectionIds.contains(sectionId)) {
						sectionIds.remove(sectionId);
					}
					sectionIds.add(sectionId);
					
					DocumentSection currSection = new DocumentSection(sectionId, 
							line.substring(sectionId.length()));
					sections.put(sectionId, currSection);
				}
			}
		} catch (IOException e) {
			Utils.printError("Error when trying to extract the document sections!");
		}

		for (String sectionId : sectionIds) {
			String id = sections.get(sectionId).id;
			String description = sections.get(sectionId).description;
			Utils.println(id + " " + description);
		    mOutputBuilder.addDocumentSection(id, description);
		}
	}
	
	private class DocumentSection {
		public String id;
		public String description;
		
		public DocumentSection(String id, String description) {
			this.id = id;
			this.description = description;
		}
	}
}
