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
import com.autochecklist.utils.nlp.CoreNLP;
import com.autochecklist.utils.nlp.ExpressionExtractor;

public class Inconsistency extends AnalysisModule {

	private ExpressionExtractor mExpressionExtractor;
	private ExpressionExtractor mFunctionsExtractor;
	private Set<String> mActionReferences;
	private Set<String> mFunctionReferences;
	private Set<String> mWatchDogReferences;
	private Map<String, Set<String>> mPeriodicReferences;

	public Inconsistency(QuestionCategory questions) {
		super(questions);

		mExpressionExtractor = new ExpressionExtractor("RegexRules/inconsistency.rules",  "RegexRules/numbers.compose", "RegexRules/prefixes.compose", "RegexRules/time_frequency.compose");
		mFunctionsExtractor = new ExpressionExtractor("RegexRules/functions.rules");
		mActionReferences = new HashSet<String>();
		mFunctionReferences = new HashSet<String>();
		mWatchDogReferences = new HashSet<String>();
		mPeriodicReferences = new HashMap<String, Set<String>>();
	}

	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Inconsistency: Requirement " + requirement.getId());

		evaluateExpressions(requirement);
		evaluateActionsAndFunctions(requirement);
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
		    } else if ("ACTIONS_FUNCTIONS".equals(actionSubType)) {
		    	handleActionsAndFunctionsReferences(requirement, question);
		    }
		}
	}

	private void handleActionsAndFunctionsReferences(Requirement requirement, Question question) {
		if (!mActionReferences.isEmpty() || !mFunctionReferences.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			if (!mActionReferences.isEmpty()) {
				sb.append("\nPossible actions:");
				for (String indicator : mActionReferences) {
					sb.append('\n').append("- ").append(indicator);
				}
			}
			if (!mFunctionReferences.isEmpty()) {
				sb.append("\nPossible functions:");
				for (String indicator : mFunctionReferences) {
					sb.append('\n').append("- ").append(indicator);
				}
			}
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Contains possible action/function references. Please check if there are other places "
			        + "(such as other requirements, figures, tables or lists) containing the same references for consistency."
					+ sb.toString(),
					Question.ANSWER_WARNING);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find action/function references. You may want to confirm it manually.",
					Question.ANSWER_POSSIBLE_YES);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		}
	}
	
	private void handlePeriodReferences(Requirement requirement, Question question) {
		if (mPeriodicReferences.containsKey(requirement.getId())) {
			StringBuilder sb = new StringBuilder();
			for (String indicator : mPeriodicReferences.get(requirement.getId())) {
				sb.append('\n').append("- ").append(indicator);
			}
		
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Contains possible frequency/period references. Please check if there are other places "
			        + "(such as other requirements, figures, tables or lists) containing the same references for consistency.\n"
						+ "Possible frequency/period indicatives:"
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
					"Contains watch dog references. Please check if there are other places "
					+ "(such as other requirements, figures, tables or lists) containing the same watch dog references for consistency.",
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

	private void evaluateActionsAndFunctions(Requirement requirement) {
		mActionReferences.clear();
		mFunctionReferences.clear();

		mActionReferences = CoreNLP.getInstance().getActions(requirement.getText());
		findFunctions(requirement.getText());
	}

	private void findFunctions(String reqText) {
		if (Utils.isTextEmpty(reqText)) return;

		List<Pair<String, String>> matched = mFunctionsExtractor.extract(reqText);
		if ((matched != null) && !matched.isEmpty()) {
			for (Pair<String, String> instance : matched) {
				mFunctionReferences.add(instance.second);
			}
		}
	}
}
