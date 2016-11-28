package com.autochecklist.modules.inconsistency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.autochecklist.base.Finding;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionAction;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.ExpressionExtractor;

public class Inconsistency extends AnalysisModule {

	private ExpressionExtractor mExpressionExtractor;
	private Set<String> mWatchDogReferences;
	private Map<String, Set<String>> mPeriodicReferences;

	public Inconsistency(QuestionCategory questions) {
		super(questions);

		mExpressionExtractor = new ExpressionExtractor("RegexRules/inconsistency.rules",  "RegexRules/numbers.compose", "RegexRules/prefixes.compose", "RegexRules/time_frequency.compose");
		mWatchDogReferences = new HashSet<String>();
		mPeriodicReferences = new HashMap<String, Set<String>>();
	}

	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Inconsistency: Requirement " + requirement.getId());

		evaluateExpressions(requirement);
	}

	@Override
	protected void handleQuestionWithoutAction(Requirement requirement, Question question) {
		// TODO delete this method and implement performQuestionAction below!
		question.setAnswerType(Question.ANSWER_YES);
	}

	@Override
	protected void performQuestionAction(Requirement requirement, Question question,
			int actionType, String actionSubType) {
		if (actionType == QuestionAction.ACTION_TYPE_CONTAINS) {
		    if ("WATCHDOG".equals(actionSubType)) {
			    handleWatchDogReferences(requirement, question);
		    } else if ("PERIODIC".equals(actionSubType)) {
		    	handlePeriodReferences(requirement, question);
		    }
		}
	}

	private void handlePeriodReferences(Requirement requirement, Question question) {
		if (mPeriodicReferences.containsKey(requirement.getId())) {
			StringBuilder sb = new StringBuilder();
			for (String indicator : mPeriodicReferences.get(requirement.getId())) {
				sb.append('\n').append("- ").append(indicator);
			}
		
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Contains possible frequency/period references.\nPossible indicatives:"
							+ sb.toString(),
					Question.ANSWER_WARNING);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find frequency/period references. You may want to confirm it manually.",
					Question.ANSWER_POSSIBLE_YES);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		}
	}
	
	private void handleWatchDogReferences(Requirement requirement, Question question) {
		if (mWatchDogReferences.contains(requirement.getId())) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Contains watch dog references. Please check for consistency!",
					Question.ANSWER_WARNING);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find watch dog references. You may want to confirm it manually.",
					Question.ANSWER_POSSIBLE_YES);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		}
	}
	
	private void evaluateExpressions(Requirement requirement) {
		String text = requirement.getText();
		if (Utils.isTextEmpty(text)) return;

		List<Pair<String, String>> matches = mExpressionExtractor.extract(text);
		if ((matches != null) && (!matches.isEmpty())) {
			for (Pair<String, String> match : matches) {
				if (!Utils.isTextEmpty(match.first)) {
					if (match.first.startsWith("WATCH")) {
						mWatchDogReferences.add(requirement.getId());
					} else if (match.first.startsWith("TIME")) {
						Set<String> refs = mPeriodicReferences.get(requirement.getId());
						if (refs == null) {
							refs = new HashSet<String>();
						}
						refs.add(match.second);
						mPeriodicReferences.put(requirement.getId(), refs);
					}
				}
			}
		}
	}
}
