package com.autochecklist.modules.incorrectness;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.base.Finding;
import com.autochecklist.base.NumberAndUnitOccurrences;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionAction;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.NLPTools;

public class Incorrectness extends AnalysisModule {

	private NumberAndUnitOccurrences mNumUnitOcc;
	
	private List<Pair<String, String>> mMatchedExpressionsForReq;
	
	public Incorrectness(QuestionCategory question) {
		super(question);

        mExpressionExtractor = NLPTools.createExpressionExtractor("RegexRules/incorrectness.rules", "RegexRules/numbers.compose", "RegexRules/prefixes.compose", "RegexRules/units.compose");
        mMatchedExpressionsForReq = new ArrayList<Pair<String, String>>();
        mNumUnitOcc = new NumberAndUnitOccurrences();
	}

	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Incorrectness: Requirement " + requirement.getId());

		mMatchedExpressionsForReq.clear();
		mMatchedExpressionsForReq = mExpressionExtractor.extract(requirement.getText());
	}

	@Override
	protected void performQuestionAction(Requirement requirement, Question question,
			int actionType, String actionSubType) {
		if (actionType == QuestionAction.ACTION_TYPE_EXTRACT_TERM_OR_EXPRESSION) {
			handleForbiddenTermsOrExpressions(requirement, question, actionSubType);
		} else if (actionType == QuestionAction.ACTION_TYPE_CHECK_NUMBER_AND_UNIT) {
			handleNumbersAndUnits(requirement, question, actionSubType);
		}
	}

	private void handleForbiddenTermsOrExpressions(Requirement requirement, Question question, String termOrExpression) {
		if ((mMatchedExpressionsForReq == null) || mMatchedExpressionsForReq.isEmpty()) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Does not contains \"" + termOrExpression + "\".", Question.ANSWER_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
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
				question.setAnswerType(finding.getAnswerType());

				found = true;
			}
		}

		if (!found) {
			// Ran all the list and found nothing.
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Does not contains \"" + termOrExpression + "\".", Question.ANSWER_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		}
	}

	private void handleNumbersAndUnits(Requirement requirement, Question question, String typeToCheck) {
		if ((mMatchedExpressionsForReq == null) || mMatchedExpressionsForReq.isEmpty()) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Does not contains \"" + typeToCheck + "\".", Question.ANSWER_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
			return;
		}

		boolean found = false;
		for (Pair<String, String> matched : mMatchedExpressionsForReq) {
			if ("NUMBER_AND_UNIT".equals(matched.first)) {
				// Found a numeric value.
				String findingDescription = "Please check: ";
				if ("UNIT".equals(typeToCheck)) {
					findingDescription = "Please check the unit: ";
				} else if ("MAGNITUDE".equals(typeToCheck)) {
					findingDescription = "Please check the magnitude: ";
				}

				// Generate a warning.
				Finding finding = new Finding(question.getId(), requirement.getId(),
						findingDescription + matched.second, Question.ANSWER_WARNING);
				requirement.addFinding(finding);
				question.addFinding(finding);
				question.setAnswerType(finding.getAnswerType());

				// Add it to the numeric occurrences.
				mNumUnitOcc.put(matched.second, requirement.getId());

				found = true;
			}
		}

		if (!found) {
			// Ran all the list and found nothing.
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Does not contains \"" + typeToCheck + "\".", Question.ANSWER_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		}
	}

	public NumberAndUnitOccurrences getNumericOccurrences() {
		return mNumUnitOcc;
	}
}
