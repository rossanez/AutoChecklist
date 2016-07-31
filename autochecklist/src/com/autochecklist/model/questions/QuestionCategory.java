package com.autochecklist.model.questions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QuestionCategory {

	private Map<Integer, Question> mCategory;
	private String mCategoryName;

	public QuestionCategory(String catName) {
		mCategory = new HashMap<Integer, Question>();
		mCategoryName = catName;
	}

	public String getCategoryName() {
		return mCategoryName;
	}

	public void addQuestion(Question question) {
		mCategory.put(question.getId(), question);
	}

	public Question getQuestionById(int id) {
		return mCategory.get(id);
	}

	public Collection<Question> getAllQuestions() {
		return mCategory.values();
	}
}
