package com.autochecklist.modules.traceability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.autochecklist.base.Finding;
import com.autochecklist.base.documentsections.DocumentSectionList;
import com.autochecklist.base.documentsections.RequirementsTraceabilityMatrix;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionAction;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.base.requirements.RequirementList;
import com.autochecklist.modules.AnalysisModule;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.ExpressionExtractor;

public class Traceability extends AnalysisModule {

	private DocumentSectionList mDocumentSections;
	private RequirementsTraceabilityMatrix mRTM;

	private ExpressionExtractor mReqIdExtractor;
	private ExpressionExtractor mReferencesExtractor;

	private List<Requirement> mRequirementList;

	private Map<String, Integer> mRTMInstances;

	private List<String> mInternalReqReferences;
	private List<String> mInternalReferences;
	private List<String> mExternalReqReferences;
	private List<String> mExternalReferences;

	public Traceability(QuestionCategory questions, DocumentSectionList sections, RequirementsTraceabilityMatrix rtmSection) {
		super(questions);
		mDocumentSections = sections;
		mRTM = rtmSection;
		mReqIdExtractor = new ExpressionExtractor("RegexRules/requirementid.rules");
		mReferencesExtractor = new ExpressionExtractor("RegexRules/references.rules");
		mRTMInstances = new HashMap<String, Integer>();
		mInternalReqReferences = new ArrayList<String>();
		mInternalReferences = new ArrayList<String>();
		mExternalReqReferences = new ArrayList<String>();
		mExternalReferences = new ArrayList<String>();
	}
	
	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Traceability: Requirement " + requirement.getId());

		// Get the number of occurrences of the requirement in the traceability matrix.
		int numRTMInstances = mRTM.countInstances(requirement.getId());
		if (numRTMInstances > 0) {
			mRTMInstances.put(requirement.getId(), numRTMInstances);
		}

		// New references for a new requirement.
		evaluateReferences(requirement);
	}

	@Override
	protected void processRequirements(RequirementList requirements) {
		if (requirements != null) {
		    mRequirementList = requirements.getRequirements();
		}

		super.processRequirements(requirements);
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
		} else if (actionType == QuestionAction.ACTION_TYPE_REFERENCE) {
			handleReferences(requirement, question, actionSubType);
		}
	}

	private void handleReferences(Requirement requirement, Question question, String type) {
		if ("INTERNAL".equals(type)) {
			handleInternalReferences(requirement, question);
		} else if ("EXTERNAL".equals(type)) {
			handleExternalReferences(requirement, question);
		}
	}

	private void handleInternalReferences(Requirement requirement, Question question) {
		StringBuilder sb = new StringBuilder();
		if ((mInternalReferences != null) && !mInternalReferences.isEmpty()) {
			sb.append('\n').append("Possible internal references:");
			for (String internalRef : mInternalReferences) {
				sb.append('\n').append("- ").append(internalRef);
			}
		}
		if ((mInternalReqReferences != null) && !mInternalReqReferences.isEmpty()) {
			sb.append('\n').append("Possible internal requirements:");
			for (String internalReq : mInternalReqReferences) {
				sb.append('\n').append("- ").append(internalReq);
			}
		}

		if (sb.length() > 0) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check if the internal references are correct." + sb.toString(),
					Question.ANSWER_WARNING);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find instances of internal requirements or references. You may want to check it manually.",
					Question.ANSWER_POSSIBLE_YES);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		}
	}

	private void handleExternalReferences(Requirement requirement, Question question) {
		StringBuilder sb = new StringBuilder();
		if ((mExternalReferences != null) && !mExternalReferences.isEmpty()) {
			sb.append('\n').append("Possible external references:");
			for (String internalRef : mExternalReferences) {
				sb.append('\n').append("- ").append(internalRef);
			}
		}
		if ((mExternalReqReferences != null) && !mExternalReqReferences.isEmpty()) {
			sb.append('\n').append("Possible external requirements:");
			for (String internalReq : mExternalReqReferences) {
				sb.append('\n').append("- ").append(internalReq);
			}
		}

		if (sb.length() > 0) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check if the external references are correct." + sb.toString(),
					Question.ANSWER_WARNING);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find instances of external requirements or references. You may want to check it manually.",
					Question.ANSWER_POSSIBLE_YES);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		}
	}

	private void evaluateReferences(Requirement requirement) {
		// Clear all the lists.
		mInternalReqReferences.clear();
		mInternalReferences.clear();
		mExternalReqReferences.clear();
		mExternalReferences.clear();

		// Search for references. The calling order matters!
		findReferences(requirement.getText());
		findRequirementsReferences(requirement.getText());
	}

	private void findRequirementsReferences(String reqText) {
		if (Utils.isTextEmpty(reqText)) return;

		List<Pair<String, String>> matched = mReqIdExtractor.extract(reqText);
		if ((matched != null) && (!matched.isEmpty())) {
			for (Pair<String, String> instance : matched) {
				// Take the ID from the text, not the matched, because it may not be exactly as is.
				String matchedReqReference = instance.second;
				String reqIdFound = reqText.substring(reqText.indexOf(matchedReqReference)).trim();
				int lastIndex = reqIdFound.indexOf(" ");
				if (lastIndex < 0) lastIndex = reqIdFound.length();
				reqIdFound = reqIdFound.substring(0, lastIndex);

				if (Utils.isTextEmpty(reqIdFound)) continue;

				if (isInternalReqReference(reqIdFound)) {
					mInternalReqReferences.add(reqIdFound);
				} else {
					// For the external case, some requirements/sections
					// may be repeated.
					if (!mExternalReferences.contains(reqIdFound)) {
					    mExternalReqReferences.add(reqIdFound);
					}
				}
			}
		}
	}

	private boolean isInternalReqReference(String reqRef) {
		if (Utils.isTextEmpty(reqRef)) return false;
        if ((mRequirementList == null) || mRequirementList.isEmpty()) {
        	return false;
        }

        // Sequential search :-(
        for (Requirement req : mRequirementList) {
        	if (reqRef.equals(req.getId())) {
        		return true;
        	}
        }

        return false;
	}

	private void findReferences(String reqText) {
		if (Utils.isTextEmpty(reqText)) return;

		List<Pair<String, String>> matched = mReferencesExtractor.extract(reqText);
		if ((matched != null) && (!matched.isEmpty())) {
			// Sequential search :-(
			for (Pair<String, String> instance : matched) {
				String ref = instance.second;
				if (isInternalReference(ref)) {
					mInternalReferences.add(ref);
				} else {
					// For the external case, some requirements/sections
					// may be repeated.
					if (!mExternalReqReferences.contains(ref)) {
					    mExternalReferences.add(ref);
					}
				}
			}
		}
	}

	private boolean isInternalReference(String ref) {
		if (Utils.isTextEmpty(ref)) return false;
		if ((mDocumentSections == null) || mDocumentSections.isEmpty()) {
			return false;
		}

		return mDocumentSections.containsId(ref);
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
