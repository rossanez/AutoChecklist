package com.autochecklist.modules.incompleteness;

import java.util.List;

import com.autochecklist.base.Finding;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionAction;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
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
		if (question.getAction() == null) {
			// TODO work on the other question types.
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check it manually.", Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else {
			if ((mMatchedExpressionsForReq == null) || (!requirement.getId().equals(mMatchedExpressionsForReq.first))) {
				// These are not for this requirement.
				return;
			}
			
			List<Pair<String, String>> matchedList = mMatchedExpressionsForReq.second;
			for (Pair<String, String> matched : matchedList) {
				if ((question.getAction().getType() == QuestionAction.EXTRACT_TERM_OR_EXPRESSION)
						&& matched.first.equals(question.getAction().getExtractionTerm())) {
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
}
