package com.autochecklist.modules;

import com.autochecklist.base.Finding;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.base.requirements.RequirementList;
import com.autochecklist.utils.nlp.IExpressionExtractable;

/**
 * This class represents a generic analysis module.
 * @author Anderson Rossanez
 */
public abstract class AnalysisModule extends Module {

	protected QuestionCategory mQuestions;

	protected IExpressionExtractable mExpressionExtractor;

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
	protected void processRequirements(RequirementList requirements) {
		for (Requirement req : requirements.getRequirements()) {
			processRequirement(req);
		}
	}

	/**
	 * This method should be overridden in the child classes if they need
	 * to perform some action with the requirement before dealing with the questions.
	 * @param requirement The current requirement.
	 */
	protected void preProcessRequirement(Requirement requirement) { }
	
	/**
	 * This method will be called for each requirement in the list.
	 * After it getting called, it will call {@code processRequirementForQuestion}
	 * in a loop, for each available question.
	 * @param requirement The current requirement
	 */
	 protected final void processRequirement(Requirement requirement) {
		preProcessRequirement(requirement);

		for (Question question : mQuestions.getAllQuestions()) {
			processRequirementForQuestion(requirement, question);
		}
	}

	/**
	 * This method will be called for each question, given a requirement.
	 * @param requirement The current requirement
	 * @param question The current question
	 */
	protected final void processRequirementForQuestion(Requirement requirement, Question question) {
		if (!question.hasAction()) {
			handleQuestionWithoutAction(requirement, question);
			return;
		}

		// Sends the action type and sub-type as parameters for convenience.
		performQuestionAction(requirement, question,
				question.getAction().getType(), question.getAction().getSubType());
	}

	/**
	 * This method will be called for each question containing a defined action, 
	 * given a requirement.
	 * @param requirement The current requirement
	 * @param question The current question
	 * @param action The question action
	 * @param subAction The question subAction (it may be null!)
	 */
	protected abstract void performQuestionAction(Requirement requirement, Question question,
			int actionType, String actionSubType);

	/**
	 * This method sets a generic finding for each question which does not have a defined action.
	 * @param requirement The current requirement
	 * @param question The current question
	 */
	protected void handleQuestionWithoutAction(Requirement requirement, Question question) {
		// Must be completely manually checked.
		Finding finding = new Finding(question.getId(), requirement.getId(),
				"Please check it manually.", Question.ANSWER_WARNING);
		requirement.addFinding(finding);
		question.addFinding(finding);
	}
}
