package com.autochecklist.utils.nlp;

import com.autochecklist.base.documentsections.DocumentSection;

/*package*/ class RequirementsTraceabilityMatrixSection {

	private DocumentSection mRTMSection;
	private StringBuilder mSectionContents;
	private boolean mIsAcquiringSectionLines = false;

	public RequirementsTraceabilityMatrixSection(DocumentSection rtmSection) {
		mRTMSection = rtmSection;
		mSectionContents = new StringBuilder();
	}

	public DocumentSection getRTMSection() {
		return mRTMSection;
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

	public String getContents() {
		return mSectionContents.toString();
	}

	public static boolean isRTMSectionDescription(String description) {
        // TODO improve it!
		if (description.contains("Traceability")) {
			return true;
		}

		return false;
	}
}
