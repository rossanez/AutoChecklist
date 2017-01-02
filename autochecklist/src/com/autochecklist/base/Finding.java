package com.autochecklist.base;

import java.security.InvalidParameterException;

import com.autochecklist.utils.Utils;

public class Finding {

	private int mQuestionId;
	private String mGenericPart;
	private String mSpecificPart;
	private String mRequirementId;
	private String mReviewerComments;

	private int mAnswerType; // Question.ANSWER_YES / ANSWER_NO / ANSWER_WARNING / ...
	private int mReviewedAnswerType = -1; // Question.ANSWER_YES or ANSWER_NO, when reviewed.

	public Finding(int questionId, String requirementId, String detail, int type) {
		this(questionId, requirementId, detail, null, type);
	}

	public Finding(int questionId, String requirementId, String generic, String specific, int type) {
		if (Utils.isTextEmpty(requirementId) || Utils.isTextEmpty(generic)) {
			Utils.printError("The requirementId and the generic description must be valid!");
			throw new InvalidParameterException("The requirementId and the generic description cannot be empty!");
		}
		mQuestionId = questionId;
		mRequirementId = requirementId;
		mGenericPart = generic;
		mSpecificPart = specific;
		mAnswerType = type;
	}

	public int getAutomatedAnswerType() {
		return mAnswerType;
	}

	public int getAnswerType() {
		if (mReviewedAnswerType >= 0) {
			return mReviewedAnswerType;
		}

		return mAnswerType;
	}

	public String getRequirementId() {
		return mRequirementId;
	}

	public int getQuestionId() {
		return mQuestionId;
	}

	public String getGenericPart() {
		return mGenericPart;
	}

	public String getSpecificPart() {
		return mSpecificPart;
	}

	public String getDetail() {
		if (Utils.isTextEmpty(mSpecificPart)) {
			return mGenericPart;
		}

		return mGenericPart + ' ' + mSpecificPart;
	}

	public int getReviewedAnswerType() {
		return mReviewedAnswerType;
	}

	public void setReviewedAnswerType(int answerType) {
		mReviewedAnswerType = answerType;
	}

	public String getReviewerComments() {
		return mReviewerComments;
	}

	public void setReviewerComments(String revComments) {
		mReviewerComments = revComments;
	}

	public boolean hasReviewerComments() {
		return !Utils.isTextEmpty(mReviewerComments);
	}

	public boolean hasBeenReviewed() {
		return (mReviewedAnswerType >= 0);
	}
}
