package com.autochecklist.modules.incompleteness;

import java.util.List;
import java.util.Set;

import com.autochecklist.base.Finding;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionAction;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.NLPTools;
import com.autochecklist.utils.nlp.ExpressionExtractor;

public class Incompleteness extends AnalysisModule {

	private Pair<String, List<Pair<String, String>>> mMatchedExpressionsForReq;
	
	public Incompleteness(QuestionCategory questions) {
		super(questions);
		mExpressionExtractor = new ExpressionExtractor("RegexRules/incompleteness.rules");
	}

	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Incompleteness: Requirement " + requirement.getId());
		List<Pair<String, String>> matchedExpressions = mExpressionExtractor.extract(requirement.getText());
		mMatchedExpressionsForReq = new Pair<String, List<Pair<String, String>>> (requirement.getId(), matchedExpressions);
	}

	@Override
	protected void performQuestionAction(Requirement requirement, Question question,
			int actionType, String actionSubType) {
	   if (actionType == QuestionAction.ACTION_TYPE_DETECT) {
			if ("EVENT_AND_ACTION".equals(actionSubType)) {
				handleActionsAndEvents(requirement, question);
			} else if ("MISSING_NUMERIC_VALUE".equals(actionSubType)) {
                handleMissingNumericValues(requirement, question);
			}
		} else {
			// Terms and expressions extraction.
			if ((mMatchedExpressionsForReq == null)
					|| (!requirement.getId().equals(mMatchedExpressionsForReq.first))) {
				// These are not for this requirement.
				return;
			}

			List<Pair<String, String>> matchedList = mMatchedExpressionsForReq.second;
			for (Pair<String, String> matched : matchedList) {
				if ((actionType == QuestionAction.ACTION_TYPE_EXTRACT_TERM_OR_EXPRESSION)
						&& matched.first.equals(actionSubType)) {
					// Found a forbidden term or expression.
					Finding finding = new Finding(question.getId(), requirement.getId(),
							"Contains \"" + matched.second + "\".", Question.ANSWER_NO);
					requirement.addFinding(finding);
					question.addFinding(finding);
					question.setAnswerType(finding.getAnswerType());
				}
			}
		}
	}

	private void handleMissingNumericValues(Requirement requirement, Question question) {
		Set<String> missingNumericValueIndicators = NLPTools.getInstance().getMissingNumericValueIndicators(requirement.getText());
		if (!missingNumericValueIndicators.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String indicator : missingNumericValueIndicators) {
				sb.append('\n').append("- ").append(indicator);
			}

			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check if there are missing numeric values.\nPossible indicatives:"
							+ sb.toString(),
					Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find missing numeric values indicatives automatically. You may want to confirm it manually.",
					Question.ANSWER_POSSIBLE_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		}
	}

	private void handleActionsAndEvents(Requirement requirement, Question question) {
		Set<String> detectedEvents = NLPTools.getInstance().checkIfHasActionsAndGetEvents(requirement.getText());
		if (!detectedEvents.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String event : detectedEvents) {
				sb.append('\n').append("- ").append(event);
			}

			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check if all the possible events are considered.\nPossible event indicatives:"
							+ sb.toString(),
					Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find event indicatives automatically. You may want to confirm it manually.",
					Question.ANSWER_POSSIBLE_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		}
	}
}
