package com.autochecklist.base.questions;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.base.Finding;

public class Question {

	// Possible answer types.
	public static final int ANSWER_YES = 0;
	public static final int ANSWER_NO = 1;
	public static final int ANSWER_WARNING = 2;

	private int mAnswerType = ANSWER_YES;
	private List<Finding> mFindings;

	private String mText;
	private int mId;
	private QuestionAction mAction;

	public Question(int id, String text) {
		mId = id;
		mText = text;

		mFindings = new ArrayList<Finding>();
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

	public void setAnswerType(int answer) {
		if (mAnswerType == ANSWER_NO) {
			// "No" should have precedence over the others.
			return;
		}

		mAnswerType = answer;
	}

	public int getAnswerType() {
		return mAnswerType;
	}

	public String getAnswerAsString() {
		switch (mAnswerType) {
		    case ANSWER_YES:
			    return "Yes";
		    case ANSWER_NO:
			    return "No";
            case ANSWER_WARNING:
			    return "Warning";
			default:
				return null;
		}
	}

	public void addFinding(Finding finding) {
		mFindings.add(finding);
	}

	public List<Finding> getFindings() {
		return mFindings;
	}
}
