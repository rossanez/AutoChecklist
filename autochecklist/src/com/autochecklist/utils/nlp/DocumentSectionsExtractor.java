package com.autochecklist.utils.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.autochecklist.base.documentsections.DocumentSection;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

public class DocumentSectionsExtractor {

	private String mPlainText;
    private IRequirementsInfoOutBuildable mOutputBuilder;
	private ExpressionExtractor mSectionIdExtractor;
	private RequirementsTraceabilityMatrixSection mRTMSection;

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
				if ((mRTMSection != null) && mRTMSection.isAcquiringSectionLines()
						&& !Utils.isTextEmpty(line)) {
						mRTMSection.appendLine(line);
				}

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

					if (RequirementsTraceabilityMatrixSection.isRTMSectionDescription(description)) {
						mRTMSection = new RequirementsTraceabilityMatrixSection(currSection);
						mRTMSection.setAcquiringSectionLines(true);
					} else if (mRTMSection != null) {
						mRTMSection.setAcquiringSectionLines(false);
					}
				}
			}
		} catch (IOException e) {
			Utils.printError("Error when trying to extract the document sections!");
		}

		populateOutputFile(sectionIds, sections);
	}

	private void populateOutputFile(List<String> sectionIds, Map<String, DocumentSection> sections) {
		for (String sectionId : sectionIds) {
			String id = sections.get(sectionId).getId();
			String description = sections.get(sectionId).getDescription();
			Utils.println(id + " " + description);
		    mOutputBuilder.addDocumentSection(id, description);
		}

		if (mRTMSection != null) {
		    mOutputBuilder.addRTMContents(mRTMSection.getContents());
		}
	}
}
