package com.autochecklist.utils.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		Set<String> sections = new HashSet<String>();

		BufferedReader bufReader = new BufferedReader(new StringReader(mPlainText));
		String line = null;
		try {
			while ((line = bufReader.readLine()) != null) {
				if (!Utils.isTextEmpty(line)) {
					String[] titleCandidate = line.split(" ");
					if ((titleCandidate == null) || (titleCandidate.length < 2)) continue;
					List<Pair<String, String>> matched = mSectionIdExtractor.extract(titleCandidate[0]);
					if ((matched == null) || matched.isEmpty()) continue;

					String sectionId = titleCandidate[0];
					if ("APPENDIX_ID".equals(matched.get(0).first)) {
						sectionId += " " + titleCandidate[1];
					}
					
					if (sections.add(sectionId)) {
					    Utils.println(line);
					    mOutputBuilder.addDocumentSection(sectionId, line);
					}
				}
			}
		} catch (IOException e) {
			Utils.printError("Error when trying to extract the document sections!");
		}
	}
}
