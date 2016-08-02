package com.autochecklist.modules;

import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.base.requirements.RequirementList;
import com.autochecklist.utils.nlp.ExpressionExtractor;

/**
 * This class represents a generic analysis module.
 * @author Anderson Rossanez
 */
public abstract class AnalysisModule extends Module {

	protected QuestionCategory mQuestions;

	protected ExpressionExtractor mExpressionExtractor;

	public AnalysisModule(QuestionCategory questions) {
		mQuestions = questions;
	}

	/**
	 * This will have no action on analysis modules.
	 * The method {@code analyze} should be called for starting the analysis.
	 */
	@Override
	public final void start() { }

	/**
	 * This method should be called for starting the analysis of the requirements.
	 * @param requirements The list of requirements to be analyzed.
	 * @return The answered questions.
	 */
	public final QuestionCategory analyze(RequirementList requirements) {
		processRequirements(requirements);
		return mQuestions;
	}

	/**
	 * This method will start the analysis of the requirements.
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
	 * This method will be called for each question, given a requirement.
	 * @param requirement The current requirement
	 * @param question The current question
	 */
	protected abstract void processRequirementForQuestion(Requirement requirement, Question question);
}
