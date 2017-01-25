package com.autochecklist.utils.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.autochecklist.base.documentsections.DocumentSection;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.interfaces.IRequirementsInfoOutBuildable;

/* package */ class DocumentSectionsExtractor {

	private String mPlainText;
    private IRequirementsInfoOutBuildable mOutputBuilder;
	private ExpressionExtractor mSectionIdExtractor;
	private RequirementsTraceabilityMatrixSectionExtractor mRTMSectionExtractor;

	public DocumentSectionsExtractor(String text, IRequirementsInfoOutBuildable outputBuilder) {
		mPlainText = text;
		mOutputBuilder = outputBuilder;
		mSectionIdExtractor = new ExpressionExtractor("RegexRules/sectionid.rules");
		mRTMSectionExtractor = new RequirementsTraceabilityMatrixSectionExtractor(outputBuilder);
	}
	
	public void extract() {
		Utils.println("Extracting the document sections...");
		
        List<String> sectionIds = new LinkedList<String>(); // To maintain the document order.
		Map<String, DocumentSection> sections = new HashMap<String, DocumentSection>();

		BufferedReader bufReader = new BufferedReader(new StringReader(mPlainText));
		String line = null;
		try {
			while ((line = bufReader.readLine()) != null) {
				if (!Utils.isTextEmpty(line)) {
					// Append RTM content if applicable.
					if (mRTMSectionExtractor.isAcquiringSectionLines()) {
						mRTMSectionExtractor.appendLine(line);
					}

					// Then start evaluating if we are dealing with a section title.
					List<Pair<String, String>> matches = mSectionIdExtractor.extract(line);
					if ((matches == null) || matches.isEmpty()) continue;
					Pair<String, String> titleCandidateMatch = null;
					for (Pair<String, String> match : matches) {
						if (line.equals(match.second) || "APPENDIX_ID".equals(match.first)) {
							titleCandidateMatch = match;
							break;
						}
					}
					if (titleCandidateMatch == null) continue;

					String[] titleCandidate = titleCandidateMatch.second.split(" ");
					String sectionId = titleCandidate[0];
					if ("APPENDIX_ID".equals(titleCandidateMatch.first)) {
						sectionId += " " + titleCandidate[1];
					}

					if (sectionIds.contains(sectionId)) {
						sectionIds.remove(sectionId);
					}
					sectionIds.add(sectionId);

					String description = line.substring(sectionId.length());
					if (!Utils.isTextEmpty(description)) {
						description = description.trim();
					} else {
						// Get the next line as description.
						while ((description != null) && (Utils.isTextEmpty(description))) {
						    description = bufReader.readLine();
						}
					}
					DocumentSection currSection = new DocumentSection(sectionId, description);
					sections.put(sectionId, currSection);

					if (mRTMSectionExtractor.isRTMSectionDescription(description)) {
						mRTMSectionExtractor.clearContents();
						mRTMSectionExtractor.setAcquiringSectionLines(true);
					} else {
						mRTMSectionExtractor.setAcquiringSectionLines(false);
					}
				}
			}
		} catch (IOException e) {
			Utils.printError("Error when trying to extract the document sections!");
		}

		populateOutputFile(sectionIds, sections);
	}

	private void populateOutputFile(List<String> sectionIds, Map<String, DocumentSection> sections) {
		// First the sections content.
		for (String sectionId : sectionIds) {
			String id = sections.get(sectionId).getId();
			String description = sections.get(sectionId).getDescription();
			Utils.println(id + " " + description);
		    mOutputBuilder.addDocumentSection(id, description);
		}

		// Then the RTM content.
		mRTMSectionExtractor.populateOutputFile();
	}
}
