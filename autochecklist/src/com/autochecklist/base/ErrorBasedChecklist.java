package com.autochecklist.base;

import com.autochecklist.base.questions.QuestionCategory;

public class ErrorBasedChecklist extends Checklist {

	private static String CHECKLIST_RESOURCE_NAME = "Checklists/Error-based.xml";

    private QuestionCategory mTraceability;
    private QuestionCategory mIncompleteness;
    private QuestionCategory mIncorrectness;
    private QuestionCategory mInconsistency;

    public ErrorBasedChecklist() {
    	mTraceability = new QuestionCategory("traceability");
    	mIncompleteness = new QuestionCategory("incompleteness");
    	mIncorrectness = new QuestionCategory("incorrectness");
    	mInconsistency = new QuestionCategory("inconsistency");
    	init();
    }

    public void init() {
    	parseChecklistResource(CHECKLIST_RESOURCE_NAME, mTraceability, mIncompleteness, mIncorrectness, mInconsistency);
    }
    
    public QuestionCategory getTraceabilityQuestions() {
    	return mTraceability;
    }

    public QuestionCategory getIncompletenessQuestions() {
    	return mIncompleteness;
    }

    public QuestionCategory getIncorrectnessQuestions() {
    	return mIncorrectness;
    }

    public QuestionCategory getInconsistencyQuestions() {
    	return mInconsistency;
    }
}
