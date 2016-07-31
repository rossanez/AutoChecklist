package com.autochecklist.modules;

import com.autochecklist.model.questions.Question;
import com.autochecklist.model.questions.QuestionCategory;
import com.autochecklist.model.requirements.Requirement;
import com.autochecklist.model.requirements.RequirementList;
import com.autochecklist.utils.nlp.ExpressionExtractor;

public abstract class Module {

	protected QuestionCategory mQuestions;

	protected ExpressionExtractor mExpressionExtractor;

	public Module(QuestionCategory questions) {
		mQuestions = questions;
	}

	public final QuestionCategory analyze(RequirementList requirements) {
		processRequirements(requirements);
		return mQuestions;
	}

	/**
	 * This method should start the analysis of the requirements.
	 * @param requirements The list of requirements
	 */
	private void processRequirements(RequirementList requirements) {
		for (Requirement req : requirements.getRequirements()) {
			processRequirement(req);
		}
	}

	/**
	 * This method will be called for each requirement in the list.
	 * After it getting called, it will call {@code processRequirementForQuestion}
	 * in a loop, for each available question.
	 * @param requirement The current requirement
	 */
	protected void processRequirement(Requirement requirement) {
		for (Question question : mQuestions.getAllQuestions()) {
			processRequirementForQuestion(requirement, question);
		}
	}

	/**
	 * This method will be called for each requirement versus question.
	 * @param requirement The current requirement
	 * @param question The current question
	 */
	protected abstract void processRequirementForQuestion(Requirement requirement, Question question);
}
