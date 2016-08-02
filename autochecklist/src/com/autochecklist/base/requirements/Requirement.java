package com.autochecklist.base.requirements;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.base.Finding;
import com.autochecklist.base.questions.Question;

public class Requirement {

	private List<Finding> mWarningFindings;
	private List<Finding> mNoFindings;
	
	private String mId;
	private String mText;

	public Requirement(String id, String text) {
		mId = id;
		mText = text;

		mWarningFindings = new ArrayList<Finding>();
		mNoFindings = new ArrayList<Finding>();
	}

	public String getId() {
		return mId;
	}

	public String getText() {
		return mText;
	}

	public void addFinding(Finding finding) {
		if (finding.getAnswerType() == Question.ANSWER_NO) {
			mNoFindings.add(finding);
		} else if (finding.getAnswerType() == Question.ANSWER_WARNING) {
			mWarningFindings.add(finding);
		}
	}

	public List<Finding> getWarningFindings() {
		return mWarningFindings;
	}

	public List<Finding> getNoFindings() {
		return mNoFindings;
	}
}
