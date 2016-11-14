package com.autochecklist.utils.nlp;

import java.util.List;

import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

/*package*/ class RequirementsTraceabilityMatrixSectionExtractor {

	private StringBuilder mSectionContents;
	private ExpressionExtractor mRTMIndicationExtractor;
	private boolean mIsAcquiringSectionLines = false;

	public RequirementsTraceabilityMatrixSectionExtractor() {
		mRTMIndicationExtractor = new ExpressionExtractor("RegexRules/rtmsection.rules");
		clearContents();
	}

	public void setAcquiringSectionLines(boolean isAcquiring) {
		mIsAcquiringSectionLines = isAcquiring;
	}

	public boolean isAcquiringSectionLines() {
		return mIsAcquiringSectionLines;
	}

	public void appendLine(String line) {
		mSectionContents.append(line).append(" ");
	}

	public void clearContents() {
		mSectionContents = new StringBuilder();
	}
	
	public String getContents() {
		return mSectionContents.toString();
	}

	public boolean isRTMSectionDescription(String description) {
		if (Utils.isTextEmpty(description)) return false;

		List<Pair<String, String>> matched = mRTMIndicationExtractor.extract(description);
		if ((matched == null) || matched.isEmpty()) return false;

		return true;
	}
}
