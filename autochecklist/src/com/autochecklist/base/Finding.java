package com.autochecklist.base;

public class Finding {

	private int mQuestionId;
	private String mDetail;
	private String mRequirementId;

	private int mAnswerType; // Question.ANSWER_YES / ANSWER_NO / ANSWER_WARNING / ...
	
	public Finding(int questionId, String requirementId, String detail, int type) {
		mQuestionId = questionId;
		mRequirementId = requirementId;
		mDetail = detail;
		mAnswerType = type;
	}

	public int getAnswerType() {
		return mAnswerType;
	}

	public String getRequirementId() {
		return mRequirementId;
	}

	public int getQuestionId() {
		return mQuestionId;
	}

	public String getDetail() {
		return mDetail;
	}
}
