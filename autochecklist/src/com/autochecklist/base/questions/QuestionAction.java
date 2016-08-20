package com.autochecklist.base.questions;

public class QuestionAction {

	// Action types
	public static final int EXTRACT_TERM_OR_EXPRESSION = 0;
	public static final int CHECK_NUMBER_AND_UNIT = 1;

	private int mType;
	private String mExtractionTerm;
	private String mSecondaryType;

	public QuestionAction(int type, String extractionTerm) {
		mType = type;
		mExtractionTerm = extractionTerm;
		mSecondaryType = null;
	}

	public QuestionAction(int type, String extractionTerm, String secondaryType) {
		mType = type;
		mExtractionTerm = extractionTerm;
		mSecondaryType = secondaryType;
	}

	public int getType() {
		return mType;
	}

	public String getSecondaryType() {
		return mSecondaryType;
	}
	
	public String getExtractionTerm() {
		return mExtractionTerm;
	}
}
