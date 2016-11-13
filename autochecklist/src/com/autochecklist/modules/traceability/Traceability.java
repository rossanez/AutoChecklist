package com.autochecklist.modules.traceability;

import com.autochecklist.base.documentsections.DocumentSectionList;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Utils;

public class Traceability extends AnalysisModule {

	private DocumentSectionList mDocumentSections;

	public Traceability(QuestionCategory questions, DocumentSectionList sections) {
		super(questions);
		mDocumentSections = sections;
	}

	@Override
	public void processRequirement(Requirement requirement) {
		Utils.println("Traceability: processing requirement " + requirement.getId());
		super.processRequirement(requirement);
	}

	@Override
	protected void processRequirementForQuestion(Requirement requirement, Question question) {
        // TODO process this correctly.
		question.setAnswerType(Question.ANSWER_YES);
	}
}
