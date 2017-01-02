package com.autochecklist.base.questions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.autochecklist.base.Finding;

public class Question {

	// Possible answer types.
	public static final int ANSWER_YES = 0;
	public static final int ANSWER_POSSIBLE_YES = 1;
	public static final int ANSWER_WARNING = 2;
	public static final int ANSWER_POSSIBLE_NO = 3;
	public static final int ANSWER_NO = 4;

	private Map<String, List<Finding>> mWarningFindings;
	private Map<String, List<Finding>> mNoFindings;
	private Map<String, List<Finding>> mPossibleNoFindings;
	private Map<String, List<Finding>> mPossibleYesFindings;
	private Map<String, List<Finding>> mYesFindings;

	private String mText;
	private int mId;
	private QuestionAction mAction;

	public Question(int id, String text) {
		mId = id;
		mText = text;

		mWarningFindings = new HashMap<String, List<Finding>>();
		mNoFindings = new HashMap<String, List<Finding>>();
		mPossibleNoFindings = new HashMap<String, List<Finding>>();
		mPossibleYesFindings = new HashMap<String, List<Finding>>();
		mYesFindings = new HashMap<String, List<Finding>>();
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
			addFinding(finding, mNoFindings);
		} else if (finding.getAnswerType() == Question.ANSWER_POSSIBLE_NO) {
			addFinding(finding, mPossibleNoFindings);
		} else if (finding.getAnswerType() == Question.ANSWER_WARNING) {
			addFinding(finding, mWarningFindings);
		} else if (finding.getAnswerType() == Question.ANSWER_POSSIBLE_YES) {
			addFinding(finding, mPossibleYesFindings);
		} else if (finding.getAnswerType() == Question.ANSWER_YES) {
			addFinding(finding, mYesFindings);
		}
	}

	public Map<String, List<Finding>> getYesFindingsMap() {
		return mYesFindings;
	}

	public List<Finding> getYesFindings() {
		return gatherAllFindings(mYesFindings);
	}

	public Map<String, List<Finding>> getPossibleYesFindingsMap() {
		return mPossibleYesFindings;
	}

	public List<Finding> getPossibleYesFindings() {
		return gatherAllFindings(mPossibleYesFindings);
	}

	public Map<String, List<Finding>> getWarningFindingsMap() {
		return mWarningFindings;
	}

	public List<Finding> getWarningFindings() {
		return gatherAllFindings(mWarningFindings);
	}

	public Map<String, List<Finding>> getNoFindingsMap() {
		return mNoFindings;
	}

	public List<Finding> getNoFindings() {
		return gatherAllFindings(mNoFindings);
	}

	public Map<String, List<Finding>> getPossibleNoFindingsMap() {
		return mPossibleNoFindings;
	}

	public List<Finding> getPossibleNoFindings() {
		return gatherAllFindings(mPossibleNoFindings);
	}

	private void addFinding(Finding finding, Map<String, List<Finding>> findingsMap) {
		String generic = finding.getGenericPart();
		List<Finding> findings = findingsMap.get(generic);
		if (findings == null) {
			findings = new ArrayList<Finding>();
		}
		findings.add(finding);
		findingsMap.put(generic, findings);
	}

	private List<Finding> gatherAllFindings(Map<String, List<Finding>> findingsMap) {
		List<Finding> findings = new ArrayList<Finding>();
		for (String generic : findingsMap.keySet()) {
			findings.addAll(findingsMap.get(generic));
		}

		return findings;
	}

	public static String getAnswerStringValue(int answerType) {
		switch (answerType) {
		case ANSWER_NO:
			return "No";
		case ANSWER_POSSIBLE_NO:
			return "Possible No";
		case ANSWER_WARNING:
			return "Warning";
		case ANSWER_POSSIBLE_YES:
			return "Possible Yes";
		case ANSWER_YES:
			return "Yes";
		default:
			return "None";
		}
	}
}
