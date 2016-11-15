package com.autochecklist.modules.traceability;

import java.util.HashMap;
import java.util.Map;

import com.autochecklist.base.Finding;
import com.autochecklist.base.documentsections.DocumentSectionList;
import com.autochecklist.base.documentsections.RequirementsTraceabilityMatrix;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionAction;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Utils;

public class Traceability extends AnalysisModule {

	private DocumentSectionList mDocumentSections;
	private RequirementsTraceabilityMatrix mRTM;
	private Map<String, Integer> mRTMInstances;

	public Traceability(QuestionCategory questions, DocumentSectionList sections, RequirementsTraceabilityMatrix rtmSection) {
		super(questions);
		mDocumentSections = sections;
		mRTM = rtmSection;
		mRTMInstances = new HashMap<String, Integer>();
	}

	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Traceability: Requirement " + requirement.getId());

		// Get the number of occurrences of the requirement in the traceability matrix.
		int numRTMInstances = mRTM.countInstances(requirement.getId());
		if (numRTMInstances > 0) {
			mRTMInstances.put(requirement.getId(), numRTMInstances);
		}
	}

	@Override
	protected void handleQuestionWithoutAction(Requirement requirement, Question question) {
		// TODO delete this method and implement performQuestionAction below completely!
		question.setAnswerType(Question.ANSWER_YES);
	}

	@Override
	protected void performQuestionAction(Requirement requirement, Question question,
			int actionType, String actionSubType) {
		if ((actionType == QuestionAction.ACTION_TYPE_CONTAINS)
		   && ("RTM".equals(actionSubType))) {
			handleRTMContains(requirement, question);
		} else if ((actionType == QuestionAction.ACTION_TYPE_CORRECT_TRACEABILITY)
				   && ("RTM".equals(actionSubType))) {
			handleRTMContainsCorrect(requirement, question);
		}
	}

	private void handleRTMContains(Requirement requirement, Question question) {
		if (!mRTM.hasContents()) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find the requirements traceability matrix in the document!",
					Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else if (mRTMInstances.containsKey(requirement.getId())) {
			int numInstances = mRTMInstances.get(requirement.getId());
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Found " + numInstances
			        + ((numInstances > 1) ? " instances" : " instance") 
					+ " of \"" + requirement.getId() + "\" in the traceability matrix.",
					Question.ANSWER_POSSIBLE_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Not found in the traceability matrix!", Question.ANSWER_NO);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		}
	}

	private void handleRTMContainsCorrect(Requirement requirement, Question question) {
		if (!mRTM.hasContents()) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find the requirements traceability matrix in the document!",
					Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else if (mRTMInstances.containsKey(requirement.getId())) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check if the traceability is correct.",
					Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Not found in the traceability matrix!", Question.ANSWER_NO);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		}
	}
}
