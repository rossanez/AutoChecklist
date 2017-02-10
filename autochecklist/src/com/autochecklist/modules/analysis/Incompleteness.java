package com.autochecklist.modules.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.autochecklist.base.Finding;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionAction;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.NLPTools;
import com.autochecklist.utils.nlp.interfaces.IEventActionDetectable;
import com.autochecklist.utils.nlp.interfaces.IMissingNumericValuesIndicativeDetectable;

public class Incompleteness extends AnalysisModule {

	private IEventActionDetectable mEventActionsDetector;
	private IMissingNumericValuesIndicativeDetectable mMissingNumericValuesDetector;
	private List<Pair<String, String>> mMatchedExpressionsForReq;
	
	public Incompleteness(QuestionCategory questions) {
		super(questions);

		mEventActionsDetector = NLPTools.getInstance().getEventActionDetector();
		mMissingNumericValuesDetector = NLPTools.getInstance().getMissingNumericValueIndicativesDetector();
		mExpressionExtractor = NLPTools.createExpressionExtractor("RegexRules/incompleteness.rules");
		mMatchedExpressionsForReq = new ArrayList<Pair<String, String>>();
	}

	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Incompleteness: Requirement " + requirement.getId());

		mMatchedExpressionsForReq.clear();
		mMatchedExpressionsForReq = mExpressionExtractor.extract(requirement.getText());
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
		} else if (actionType == QuestionAction.ACTION_TYPE_EXTRACT_TERM_OR_EXPRESSION) {
			handleForbiddenTermsOrExpressions(requirement, question, actionSubType);
		}
	}

	private void handleMissingNumericValues(Requirement requirement, Question question) {
		Set<String> missingNumericValueIndicators = mMissingNumericValuesDetector.detect(requirement.getText());
		if (!missingNumericValueIndicators.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String indicator : missingNumericValueIndicators) {
				sb.append('\n').append("- ").append(indicator);
			}

			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check if there are missing numeric values.",
					"Possible indicatives:" + sb.toString(),
					Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find missing numeric values indicatives automatically. You may want to confirm it manually.",
					Question.ANSWER_POSSIBLE_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
		}
	}

	private void handleActionsAndEvents(Requirement requirement, Question question) {
		Set<String> detectedEvents = mEventActionsDetector.checkIfHasActionsAndGetEvents(requirement.getText());
		if (!detectedEvents.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String event : detectedEvents) {
				sb.append('\n').append("- ").append(event);
			}

			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check if all the possible events are considered.",
					"Possible event indicatives:" + sb.toString(),
					Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find event indicatives automatically. You may want to confirm it manually.",
					Question.ANSWER_POSSIBLE_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
		}
	}

	private void handleForbiddenTermsOrExpressions(Requirement requirement, Question question, String termOrExpression) {
		if ((mMatchedExpressionsForReq == null) || mMatchedExpressionsForReq.isEmpty()) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Does not contains \"" + termOrExpression + "\".", Question.ANSWER_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			return;
		}

		boolean found = false;
		for (Pair<String, String> matched : mMatchedExpressionsForReq) {
			if (matched.first.equals(termOrExpression)) {
				// Found a forbidden term or expression.
				Finding finding = new Finding(question.getId(), requirement.getId(),
						"Contains \"" + matched.second + "\".", Question.ANSWER_NO);
				requirement.addFinding(finding);
				question.addFinding(finding);

				found = true;
			}
		}

		if (!found) {
			// Ran all the list and found nothing.
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Does not contains \"" + termOrExpression + "\".", Question.ANSWER_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
		}
	}
}
