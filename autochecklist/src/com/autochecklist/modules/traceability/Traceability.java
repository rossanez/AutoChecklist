package com.autochecklist.modules.traceability;

import com.autochecklist.base.documentsections.DocumentSectionList;
import com.autochecklist.base.documentsections.RequirementsTraceabilityMatrix;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Utils;

public class Traceability extends AnalysisModule {

	private DocumentSectionList mDocumentSections;
	private RequirementsTraceabilityMatrix mRTM;

	public Traceability(QuestionCategory questions, DocumentSectionList sections, RequirementsTraceabilityMatrix rtmSection) {
		super(questions);
		mDocumentSections = sections;
		mRTM = rtmSection;
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
