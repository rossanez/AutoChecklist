package com.autochecklist.base.questions;

public class QuestionAction {

	// Action types
	public static final int EXTRACT_TERM_OR_EXPRESSION = 0;
	public static final int CHECK_NUMBER_AND_UNIT = 1;

	private int mType;
	private String mParam;

	public QuestionAction(int type, String param) {
		mType = type;
		mParam = param;
	}

	public int getType() {
		return mType;
	}

	public String getWhat() {
		return mParam;
	}
}
