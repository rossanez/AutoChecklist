package com.autochecklist.modules.inconsistency;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.ExpressionExtractor;

public class Inconsistency extends AnalysisModule {

	private ExpressionExtractor mExpressionExtractor;
	private Set<String> mContainsWatchDogReferences;

	public Inconsistency(QuestionCategory questions) {
		super(questions);

		mExpressionExtractor = new ExpressionExtractor("RegexRules/inconsistency.rules");
		mContainsWatchDogReferences = new HashSet<String>();
	}

	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Inconsistency: Requirement " + requirement.getId());

		if (findWatchDogReferences(requirement.getText())) {
			mContainsWatchDogReferences.add(requirement.getId());
		}
	}

	@Override
	protected void handleQuestionWithoutAction(Requirement requirement, Question question) {
		// TODO delete this method and implement performQuestionAction below!
		question.setAnswerType(Question.ANSWER_YES);
	}

	@Override
	protected void performQuestionAction(Requirement requirement, Question question,
			int actionType, String actionSubType) {
		// TODO implement it!
	}

	private boolean findWatchDogReferences(String text) {
		if (Utils.isTextEmpty(text)) return false;

		List<Pair<String, String>> matched = mExpressionExtractor.extract(text);
		if ((matched != null) && (!matched.isEmpty())) {
			// If it contains results, it is enough.
			return true;
		}

		return false;
	}
}
