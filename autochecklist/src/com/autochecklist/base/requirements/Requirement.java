package com.autochecklist.base.requirements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.autochecklist.base.Finding;
import com.autochecklist.base.questions.Question;

public class Requirement {

	private List<Finding> mWarningFindings;
	private List<Finding> mNoFindings;
	private List<Finding> mPossibleNoFindings;
	private List<Finding> mPossibleYesFindings;
	private List<Finding> mYesFindings;
	
	private String mId;
	private String mText;

	public Requirement(String id, String text) {
		mId = id;
		mText = text;

		mWarningFindings = new ArrayList<Finding>();
		mNoFindings = new ArrayList<Finding>();
		mPossibleNoFindings = new ArrayList<Finding>();
		mPossibleYesFindings = new ArrayList<Finding>();
		mYesFindings = new ArrayList<Finding>();
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

	public List<Finding> getAllFindings() {
		List<Finding> all = new ArrayList<Finding>();
		all.addAll(mYesFindings);
		all.addAll(mPossibleYesFindings);
		all.addAll(mWarningFindings);
		all.addAll(mPossibleNoFindings);
		all.addAll(mNoFindings);

		return all;
	}

	private void clearAllLists() {
		mNoFindings.clear();
		mPossibleNoFindings.clear();
		mWarningFindings.clear();
		mPossibleYesFindings.clear();
		mYesFindings.clear();
	}

	public void rebuildForConsistency() {
		List<Finding> allFindings = getAllFindings();
		allFindings.sort(new Comparator<Finding>() {

			@Override
			public int compare(Finding o1, Finding o2) {
				return (o1.getQuestionId() < o2.getQuestionId() ? -1
						: (o1.getQuestionId() == o2.getQuestionId() ? 0 : 1));
			}
			
		});
		clearAllLists();

		for (Finding finding : allFindings) {
			addFinding(finding);
		}
	}
}
