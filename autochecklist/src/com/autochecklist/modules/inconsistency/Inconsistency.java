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
	public void processRequirement(Requirement requirement) {
		Utils.println("Inconsistency: processing requirement " + requirement.getId());
		super.processRequirement(requirement);
	}

	@Override
	protected void processRequirementForQuestion(Requirement requirement, Question question) {
		// TODO process this correctly.
		question.setAnswerType(Question.ANSWER_WARNING);
	}
}
