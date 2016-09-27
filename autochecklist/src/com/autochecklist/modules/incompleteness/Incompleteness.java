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
import com.autochecklist.utils.nlp.CoreNLP;
import com.autochecklist.utils.nlp.ExpressionExtractor;

public class Incompleteness extends AnalysisModule {

	private Pair<String, List<Pair<String, String>>> mMatchedExpressionsForReq;
	
	public Incompleteness(QuestionCategory questions) {
		super(questions);
		mExpressionExtractor = new ExpressionExtractor("RegexRules/incompleteness.rules");
	}

	@Override
	public void processRequirement(Requirement requirement) {
		Utils.println("Incompleteness: processing requirement " + requirement.getId());
		List<Pair<String, String>> matchedExpressions = mExpressionExtractor.extract(requirement.getText());
		mMatchedExpressionsForReq = new Pair<String, List<Pair<String, String>>> (requirement.getId(), matchedExpressions);

        super.processRequirement(requirement);
	}

	@Override
	protected void processRequirementForQuestion(Requirement requirement, Question question) {
		if (question.hasAction()) {
		    if (question.getAction().getType() == QuestionAction.ACTION_TYPE_DETECT) {
		    	Set<String> detectedEvents = CoreNLP.getInstance().checkIfHasActionsAndGetEvents(requirement.getText());
				if (!detectedEvents.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					for (String event : detectedEvents) {
						sb.append('\n').append("- ").append(event);
					}

					Finding finding = new Finding(question.getId(), requirement.getId(),
							"Please check if all the possible events are considered. Possible event indicatives:" + sb.toString(),
							Question.ANSWER_WARNING);
					requirement.addFinding(finding);
					question.addFinding(finding);
					question.setAnswerType(finding.getAnswerType());
				} else {
					Finding finding = new Finding(question.getId(), requirement.getId(),
							"Unable to find event indicatives automatically. You may want to check it manually.",
							Question.ANSWER_POSSIBLE_YES);
					requirement.addFinding(finding);
					question.addFinding(finding);
					question.setAnswerType(finding.getAnswerType());
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
					if ((question.getAction().getType() == QuestionAction.ACTION_TYPE_EXTRACT_TERM_OR_EXPRESSION)
							&& matched.first.equals(question.getAction().getSubType())) {
						// Found a forbidden term or expression.
						Finding finding = new Finding(question.getId(), requirement.getId(),
								"Contains \"" + matched.second + "\".", Question.ANSWER_NO);
						requirement.addFinding(finding);
						question.addFinding(finding);
						question.setAnswerType(finding.getAnswerType());
					}
				}
			}
		} else {
			// No action: Must be completely manually checked.
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check it manually.", Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		}
	}
}
