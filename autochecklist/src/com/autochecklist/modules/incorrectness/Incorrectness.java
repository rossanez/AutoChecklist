package com.autochecklist.modules.incorrectness;

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
	
	private Pair<String, List<Pair<String, String>>> mMatchedExpressionsForReq;
	
	public Incorrectness(QuestionCategory question) {
		super(question);
        mExpressionExtractor = NLPTools.createExpressionExtractor("RegexRules/incorrectness.rules", "RegexRules/numbers.compose", "RegexRules/prefixes.compose", "RegexRules/units.compose");
        mNumUnitOcc = new NumberAndUnitOccurrences();
	}

	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Incorrectness: Requirement " + requirement.getId());
		List<Pair<String, String>> matchedExpressions = mExpressionExtractor.extract(requirement.getText());
		mMatchedExpressionsForReq = new Pair<String, List<Pair<String, String>>> (requirement.getId(), matchedExpressions);
	}

	@Override
	protected void performQuestionAction(Requirement requirement, Question question,
			int actionType, String actionSubType) {
		if ((mMatchedExpressionsForReq == null) || (!requirement.getId().equals(mMatchedExpressionsForReq.first))) {
			// These are not for this requirement.
			return;
		}
			
		List<Pair<String, String>> matchedList = mMatchedExpressionsForReq.second;
		for (Pair<String, String> matched : matchedList) {
			if ((actionType == QuestionAction.ACTION_TYPE_EXTRACT_TERM_OR_EXPRESSION)
					&& matched.first.equals(actionSubType)) {
				// Found a forbidden term or expression - generate a negative answer.
				handleForbiddenTermsOrExpressions(requirement, question, matched);
			} else if ((actionType == QuestionAction.ACTION_TYPE_CHECK_NUMBER_AND_UNIT)
					&& matched.first.equals("NUMBER_AND_UNIT")) {
				// Found a numeric item.
				handleNumbersAndUnits(requirement, question, matched);
			}
		}
	}

	private void handleForbiddenTermsOrExpressions(Requirement requirement, Question question,
			Pair<String, String> matched) {
		Finding finding = new Finding(question.getId(), requirement.getId(),
				"Contains \"" + matched.second + "\".", Question.ANSWER_NO);
		requirement.addFinding(finding);
		question.addFinding(finding);
		question.setAnswerType(finding.getAnswerType());
	}

	private void handleNumbersAndUnits(Requirement requirement, Question question, Pair<String, String> matched) {
		String findingDescription = "Please check: ";
		if ("UNIT".equals(question.getAction().getSubType())) {
			findingDescription = "Please check the unit: ";
		} else if ("MAGNITUDE".equals(question.getAction().getSubType())) {
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
	}

	public NumberAndUnitOccurrences getNumericOccurrences() {
		return mNumUnitOcc;
	}
}
