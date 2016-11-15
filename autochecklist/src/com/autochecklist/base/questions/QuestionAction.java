package com.autochecklist.base.questions;

public class QuestionAction {

	// Action types
	public static final int ACTION_TYPE_EXTRACT_TERM_OR_EXPRESSION = 0;
	public static final int ACTION_TYPE_CHECK_NUMBER_AND_UNIT = 1;
	public static final int ACTION_TYPE_DETECT = 2;
	public static final int ACTION_TYPE_CONTAINS = 3;
	public static final int ACTION_TYPE_CORRECT_TRACEABILITY = 4;

	private int mType;
	private String mSubType;

	public QuestionAction(int type, String subType) {
		mType = type;
		mSubType = subType;
	}

	public int getType() {
		return mType;
	}

	public String getSubType() {
		return mSubType;
	}
}
