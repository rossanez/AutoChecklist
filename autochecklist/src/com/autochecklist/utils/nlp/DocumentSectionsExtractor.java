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

	private ExpressionExtractor mSectionIdExtractor;

	public DocumentSectionsExtractor() {
		this.mSectionIdExtractor = new ExpressionExtractor("RegexRules/sectionid.rules");
	}
	
	public Set<String> extract(String document) {
		Set<String> sections = new HashSet<String>();

		BufferedReader bufReader = new BufferedReader(new StringReader(document));
		String line = null;
		try {
			while ((line = bufReader.readLine()) != null) {
				if (!Utils.isTextEmpty(line)) {
					String[] titleCandidate = line.split(" ");
					if ((titleCandidate == null) || (titleCandidate.length < 2)) continue;
					List<Pair<String, String>> matched = mSectionIdExtractor.extract(titleCandidate[0]);
					if ((matched == null) || matched.isEmpty()) continue;

					Utils.println(line);
					sections.add(line);
				}
			}
		} catch (IOException e) {
			Utils.printError("Error when trying to extract the document sections!");
		}

		return sections;
	}
}
