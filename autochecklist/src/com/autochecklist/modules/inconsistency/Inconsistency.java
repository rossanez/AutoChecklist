package com.autochecklist.modules.inconsistency;

import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Utils;

public class Inconsistency extends AnalysisModule {

	public Inconsistency(QuestionCategory questions) {
		super(questions);
	}

	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Inconsistency: Requirement " + requirement.getId());
	}

	@Override
	protected void handleQuestionWithoutAction(Requirement requirement, Question question) {
		// TODO delete this method and implement performQuestionAction below!
		question.setAnswerType(Question.ANSWER_YES);
	}

	@Override
	protected void performQuestionAction(Requirement requirement, Question question,
			int actionType, String actionSubType) {
		// TODO implement it!
	}
}
