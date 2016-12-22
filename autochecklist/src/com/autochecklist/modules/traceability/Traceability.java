package com.autochecklist.modules.traceability;

import java.util.ArrayList;
import java.util.List;

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
import com.autochecklist.utils.nlp.IExpressionExtractable;
import com.autochecklist.utils.nlp.NLPTools;

public class Traceability extends AnalysisModule {

	private DocumentSectionList mDocumentSections;
	private RequirementsTraceabilityMatrix mRTM;

	private IExpressionExtractable mInternalReqIdExtractor;
	private IExpressionExtractable mReferencesExtractor;
	private IExpressionExtractable mFunctionsExtractor;

	private List<Requirement> mRequirementList;

	private int mRTMNumInstances;
	private boolean mRTMContainsRequirementPrecisely;

	private List<String> mInternalReqReferences;
	private List<String> mInternalReferences;
	private List<String> mExternalReqReferences;
	private List<String> mExternalReferences;
	private List<String> mFunctionReferences;

	public Traceability(QuestionCategory questions, DocumentSectionList sections, RequirementsTraceabilityMatrix rtmSection) {
		super(questions);
		mDocumentSections = sections;
		mRTM = rtmSection;

		mInternalReqIdExtractor = NLPTools.createExpressionExtractor("RegexRules/requirementid.rules");
		mReferencesExtractor = NLPTools.createExpressionExtractor("RegexRules/references.rules");
		mFunctionsExtractor = NLPTools.createExpressionExtractor("RegexRules/functions.rules");

		mInternalReqReferences = new ArrayList<String>();
		mInternalReferences = new ArrayList<String>();
		mExternalReqReferences = new ArrayList<String>();
		mExternalReferences = new ArrayList<String>();
		mFunctionReferences = new ArrayList<String>();
	}
	
	@Override
	public void preProcessRequirement(Requirement requirement) {
		Utils.println("Traceability: Requirement " + requirement.getId());

		// Reset the RTM variables and reevaluate them.
		mRTMContainsRequirementPrecisely = false;
		mRTMNumInstances = 0;
		if (mRTM.isInPreciseMode()) {
			// Checks if the traceability matrix contains this requirement mapped.
			mRTMContainsRequirementPrecisely = mRTM.hasRequirementPrecisely(requirement.getId());
		} else {
			// Get the number of occurrences of the requirement in the traceability matrix text chunk.
			mRTMNumInstances = mRTM.countInstances(requirement.getId());
		}

		// New references for a new requirement.
		evaluateReferences(requirement);
		evaluateFunctions(requirement);
	}

	@Override
	protected void processRequirements(RequirementList requirements) {
		if (requirements != null) {
		    mRequirementList = requirements.getRequirements();
		}

		super.processRequirements(requirements);
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
		} else if ("FUNCTION".equals(type)) {
			handleFunctionReferences(requirement, question);
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
					"Unable to find instances of internal requirements or references. You may want to confirm it manually.",
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
					"Unable to find instances of external requirements or references. You may want to confirm it manually.",
					Question.ANSWER_POSSIBLE_YES);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		}
	}

	private void handleFunctionReferences(Requirement requirement, Question question) {
		if (!mFunctionReferences.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append('\n').append("Possible function references:");
			for (String functionRef : mFunctionReferences) {
				sb.append('\n').append("- ").append(functionRef);
			}
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check if the function references are correct." + sb.toString(),
					Question.ANSWER_WARNING);
		    question.addFinding(finding);
		    requirement.addFinding(finding);
		    question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find instances of functions. You may want to confirm it manually.",
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
		findReferencesAndExternalRequirements(requirement.getText());
		findInternalRequirements(requirement.getText());
	}

	private void evaluateFunctions(Requirement requirement) {
		// Clear the list.
		mFunctionReferences.clear();

		// Search for functions.
		findFunctions(requirement.getText());
	}

	private void findInternalRequirements(String reqText) {
		if (Utils.isTextEmpty(reqText)) return;

		List<Pair<String, String>> matched = mInternalReqIdExtractor.extract(reqText);
		if ((matched != null) && !matched.isEmpty()) {
			for (Pair<String, String> instance : matched) {
				// Take the ID from the text, not the matched, because it may not be exactly as is.
				String matchedReqReference = instance.second;
				String reqIdFound = reqText.substring(reqText.indexOf(matchedReqReference)).trim();
				int lastIndex = reqIdFound.indexOf(" ");
				if (lastIndex < 0) lastIndex = reqIdFound.length();
				reqIdFound = reqIdFound.substring(0, lastIndex);

				if (Utils.isTextEmpty(reqIdFound)) continue;

				// Only adding if it is an internal requirement.
				// External references will be evaluated on findReferencesAndExternalRequirements.
				if (isInternalReqReference(reqIdFound)) {
				    addFoundReqReference(reqIdFound);
				}
			}
		}
	}

	private void findReferencesAndExternalRequirements(String reqText) {
		if (Utils.isTextEmpty(reqText)) return;

		List<Pair<String, String>> matched = mReferencesExtractor.extract(reqText);
		if ((matched != null) && !matched.isEmpty()) {
			// Sequential search :-(
			for (Pair<String, String> instance : matched) {
				String refId = extractReferenceId(instance.second);
				if (isReference(instance.first)) {
				    addFoundReference(refId);
				} else if (isRequirement(instance.first)) {
					// External requirement.
					addFoundReqReference(refId);
				}
			}
		}
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

	private boolean isReference(String matchType) {
		if (Utils.isTextEmpty(matchType)) return false;

		return matchType.contains("REFERENCE");
	}

	private boolean isRequirement(String matchType) {
		if (Utils.isTextEmpty(matchType)) return false;

		return matchType.contains("REQUIREMENT");
	}
	
	private String extractReferenceId(String refText) {
		if (Utils.isTextEmpty(refText)) return null;

		String[] tokens = refText.split(" ");
		if ((tokens == null) || (tokens.length < 1)) return null;

		// returns the last token.
		return tokens[tokens.length -1];
	}

	private void addFoundReference(String refId) {
		if (isInternalReference(refId)) {
			mInternalReferences.add(refId);
		} else {
			// For the external case, some requirements/sections
			// may be repeated.
			if (!mExternalReqReferences.contains(refId)) {
			    mExternalReferences.add(refId);
			}
		}
	}

	private void addFoundReqReference(String reqIdFound) {
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

	private boolean isInternalReference(String ref) {
		if (Utils.isTextEmpty(ref)) return false;
		if ((mDocumentSections == null) || mDocumentSections.isEmpty()) {
			return false;
		}

		return mDocumentSections.containsId(ref);
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
	
	private void handleRTMContains(Requirement requirement, Question question) {
		if (!mRTM.hasContents()) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Unable to find the requirements traceability matrix in the document!",
					Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else if (mRTM.isInPreciseMode() && mRTMContainsRequirementPrecisely) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Found in the traceability matrix.",
					Question.ANSWER_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else if (!mRTM.isInPreciseMode() && (mRTMNumInstances > 0)) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Found " + mRTMNumInstances
			        + ((mRTMNumInstances > 1) ? " instances" : " instance") 
					+ " of \"" + requirement.getId() + "\" in the traceability matrix.",
					Question.ANSWER_POSSIBLE_YES);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Not found in the traceability matrix!",
					mRTM.isInPreciseMode() ? Question.ANSWER_NO : Question.ANSWER_POSSIBLE_NO);
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
		} else if (mRTMContainsRequirementPrecisely || (mRTMNumInstances > 0)) {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Please check if the traceability is correct.",
					Question.ANSWER_WARNING);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		} else {
			Finding finding = new Finding(question.getId(), requirement.getId(),
					"Not found in the traceability matrix!",
					mRTM.isInPreciseMode() ? Question.ANSWER_NO : Question.ANSWER_POSSIBLE_NO);
			requirement.addFinding(finding);
			question.addFinding(finding);
			question.setAnswerType(finding.getAnswerType());
		}
	}
}
