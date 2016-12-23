package com.autochecklist.base.questions;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.base.Finding;

public class Question {

	// Possible answer types.
	public static final int ANSWER_YES = 0;
	public static final int ANSWER_POSSIBLE_YES = 1;
	public static final int ANSWER_WARNING = 2;
	public static final int ANSWER_POSSIBLE_NO = 3;
	public static final int ANSWER_NO = 4;

	private List<Finding> mWarningFindings;
	private List<Finding> mNoFindings;
	private List<Finding> mPossibleNoFindings;
	private List<Finding> mPossibleYesFindings;
	private List<Finding> mYesFindings;

	private String mText;
	private int mId;
	private QuestionAction mAction;

	public Question(int id, String text) {
		mId = id;
		mText = text;

		mWarningFindings = new ArrayList<Finding>();
		mNoFindings = new ArrayList<Finding>();
		mPossibleNoFindings = new ArrayList<Finding>();
		mPossibleYesFindings = new ArrayList<Finding>();
		mYesFindings = new ArrayList<Finding>();
	}

	public void setAction(QuestionAction action) {
		mAction = action;
	}

	public QuestionAction getAction() {
        return mAction;
	}

	public boolean hasAction() {
		return (mAction != null);
	}

	public int getId() {
		return mId;
	}

	public String getText() {
		return mText;
	}

	public void addFinding(Finding finding) {
		if (finding.getAnswerType() == Question.ANSWER_NO) {
			mNoFindings.add(finding);
		} else if (finding.getAnswerType() == Question.ANSWER_POSSIBLE_NO) {
			mPossibleNoFindings.add(finding);
		} else if (finding.getAnswerType() == Question.ANSWER_WARNING) {
			mWarningFindings.add(finding);
		} else if (finding.getAnswerType() == Question.ANSWER_POSSIBLE_YES) {
			mPossibleYesFindings.add(finding);
		} else if (finding.getAnswerType() == Question.ANSWER_YES) {
			mYesFindings.add(finding);
		}
	}

	public List<Finding> getYesFindings() {
		return mYesFindings;
	}

	public List<Finding> getPossibleYesFindings() {
		return mPossibleYesFindings;
	}

	public List<Finding> getWarningFindings() {
		return mWarningFindings;
	}

	public List<Finding> getNoFindings() {
		return mNoFindings;
	}

	public List<Finding> getPossibleNoFindings() {
		return mPossibleNoFindings;
	}
}
