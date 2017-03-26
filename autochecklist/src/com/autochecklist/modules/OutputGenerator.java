package com.autochecklist.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.autochecklist.base.Finding;
import com.autochecklist.base.NumberAndUnitOccurrences;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionAction;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.base.requirements.RequirementList;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.checklists.ErrorBasedChecklist;
import com.autochecklist.utils.filebuilder.CSVBuilder;
import com.autochecklist.utils.filebuilder.HtmlBuilder;

public class OutputGenerator extends Module {

	private RequirementList mRequirements;
	private List<QuestionCategory> mQuestions;
	private NumberAndUnitOccurrences mNumericOcc;

	private String mOutputDir;
	
	public OutputGenerator(RequirementList analyzedRequirements, List<QuestionCategory> answeredQuestions, NumberAndUnitOccurrences numericOcc, String workingDir) {
		mRequirements = analyzedRequirements;
		mQuestions = answeredQuestions;
        mNumericOcc = numericOcc;
		mOutputDir = setOutputDirectory(workingDir);
	}

	private String setOutputDirectory(String dir) {
		return dir + "AnalysisOutput" + Utils.getDateAndTimeApdStr() + File.separatorChar;
	}

	public Requirement getRequirement(String reqId) {
		return mRequirements.getRequirement(reqId);
	}
	public List<Requirement> getRequirements() {
		return mRequirements.getRequirements();
	}

	public Question getQuestion(int id) {
		return getQuestion(id, mQuestions);
	}

	private static Question getQuestion(int id, List<QuestionCategory> questions) {
		for (QuestionCategory cat : questions) {
			Question question = cat.getQuestionById(id);
			if (question != null) return question;
		}

		return null;
	}

	public List<Question> getQuestions() {
		List<Question> result = new ArrayList<Question>();
		for (QuestionCategory cat : mQuestions) {
			result.addAll(cat.getAllQuestions());
		}

		return result;
	}

	@Override
	public void start() {
		createOutputDirectory();
		generateQuestionsViewFile();
		generateRequirementsViewFile();
		generateNumericOccurrencesViewFile();
		generateAnalysisFile();
	}

	private File createOutputDirectory() {
		return Utils.createDirectory(mOutputDir);
	}

	public String generateQuestionsViewContent(boolean forUI) {
		StringBuilder outBuilder = new StringBuilder();
		String currentCat = null;
		for (QuestionCategory questionCat : mQuestions) {
			if (!questionCat.getCategoryName().equals(currentCat)) {
				currentCat = questionCat.getCategoryName();
			    outBuilder.append(" --- ").append("Questions about ")
			              .append(currentCat.substring(0, 1).toUpperCase()
			    		          + currentCat.substring(1))
			              .append(" /--- ");
			}

			for (Question question : questionCat.getAllQuestions()) {
				outBuilder.append(question.getId()).append(". ")
				    .append(question.getText()).append('\n');

				// Following this precedence order:
	        	// No > Possible No > Warning > Possible Yes > Yes
	        	Map<String, List<Finding>> noFindings = question.getNoFindingsMap();
	        	if (!noFindings.isEmpty()) {
	        		outBuilder.append(" - Answer: No").append('\n');

	        		for (String genericPart : noFindings.keySet()) {
	        			outBuilder.append(" -- ").append(formatQuestionFinding(genericPart, noFindings.get(genericPart))).append(" /-- ");
	        		}
	        	}

	        	Map<String, List<Finding>>  possibleNoFindings = question.getPossibleNoFindingsMap();
	        	if (!possibleNoFindings.isEmpty()) {
	        		outBuilder.append(" - Answer: Possible No").append('\n');

					for (String genericPart : possibleNoFindings.keySet()) {
						outBuilder.append(" -- ").append(formatQuestionFinding(genericPart, possibleNoFindings.get(genericPart))).append(" /-- ");
					}
	        	}

	        	Map<String, List<Finding>>  warningFindings = question.getWarningFindingsMap();
	        	if (!warningFindings.isEmpty()) {
	        		outBuilder.append(" - Answer: Warning").append('\n');

	                for (String genericPart : warningFindings.keySet()) {
	        			outBuilder.append(" -- ").append(formatQuestionFinding(genericPart, warningFindings.get(genericPart))).append(" /-- ");
	        		}
	        	}

	        	Map<String, List<Finding>>  possibleYesFindings = question.getPossibleYesFindingsMap();
	        	if (!possibleYesFindings.isEmpty()) {
	        		outBuilder.append(" - Answer: Possible Yes").append('\n');

	                for (String genericPart : possibleYesFindings.keySet()) {
	        			outBuilder.append(" -- ").append(formatQuestionFinding(genericPart, possibleYesFindings.get(genericPart))).append(" /-- ");
	        		}
	        	}

	        	List<Finding> yesFindings = question.getYesFindings();
	        	if (!yesFindings.isEmpty()) {
	        		outBuilder.append(" - Answer: Yes").append('\n');

	        		// In this case, there is no need to list the finding contents, only printing the IDs.
	                outBuilder.append(" -init-list- ");
	                outBuilder.append(" -- ");
	                outBuilder.append("Requirements: ");
	        		for (int i = 0; i < yesFindings.size(); i++) {
	        			outBuilder.append(yesFindings.get(i).getRequirementId());
	        			if (yesFindings.get(i).hasBeenReviewed()) {
	    					outBuilder.append('*');
	    				}
	        			if (yesFindings.get(i).hasReviewerComments()) {
	    			    	outBuilder.append(" (").append(formatReviewerComments(yesFindings.get(i))).append(')');
	    			    }
	        			if (i == yesFindings.size() -1) {
	        				outBuilder.append('.');
	        			} else if (i == yesFindings.size() -2) {
	        				outBuilder.append(", and ");
	        			} else {
	        				outBuilder.append(", ");
	        			}
	        		}
	        		outBuilder.append(" /-- ");
	        		outBuilder.append(" -end-list- ");
	        	}

	        	outBuilder.append('\n');
			}
		}

		return HtmlBuilder.generateContent("Checklist View", outBuilder.toString(), forUI);
	}

	private void generateQuestionsViewFile() {
		new HtmlBuilder(mOutputDir + "checklist_view.html").build(generateQuestionsViewContent(false));
	}

	public String generateRequirementsViewContent(boolean forUI) {
		return generateDefaultRequirementsViewContent(forUI);
	}
	
	private void generateRequirementsViewFile() {
		generateDefaultRequirementsView();
	}

	private String generateDefaultRequirementsViewContent(boolean forUI) {
		StringBuilder outBuilder = new StringBuilder();
        for(Requirement requirement : mRequirements.getRequirements()) {
        	outBuilder.append(requirement.getId()).append(" ---- ")
        	          .append(requirement.getText()).append('\n');

        	// Following this precedence order:
        	// No > Possible No > Warning > Possible Yes > Yes
        	List<Finding> noFindings = requirement.getNoFindings();
        	if (!noFindings.isEmpty()) {
        		outBuilder.append(" - Answer: No").append('\n');

        		outBuilder.append(" -init-list- ");
        		for (Finding finding : noFindings) {
        			outBuilder.append(" -- ").append(formatRequirementFinding(finding)).append('\n').append(" /-- ");
        		}
        		outBuilder.append(" -end-list- ");
        	}

        	List<Finding> possibleNoFindings = requirement.getPossibleNoFindings();
        	if (!possibleNoFindings.isEmpty()) {
        		outBuilder.append(" - Answer: Possible No").append('\n');

                outBuilder.append(" -init-list- ");
        		for (Finding finding : possibleNoFindings) {
        			outBuilder.append(" -- ").append(formatRequirementFinding(finding)).append('\n').append(" /-- ");
        		}
        		outBuilder.append(" -end-list- ");
        	}

        	List<Finding> warningFindings = requirement.getWarningFindings();
        	if (!warningFindings.isEmpty()) {
        		outBuilder.append(" - Answer: Warning").append('\n');

                outBuilder.append(" -init-list- ");
        		for (Finding finding : warningFindings) {
        			outBuilder.append(" -- ").append(formatRequirementFinding(finding)).append('\n').append(" /-- ");
        		}
        		outBuilder.append(" -end-list- ");
        	}

        	List<Finding> possibleYesFindings = requirement.getPossibleYesFindings();
        	if (!possibleYesFindings.isEmpty()) {
        		outBuilder.append(" - Answer: Possible Yes").append('\n');

                outBuilder.append(" -init-list- ");
        		for (Finding finding : possibleYesFindings) {
        			outBuilder.append(" -- ").append(formatRequirementFinding(finding)).append('\n').append(" /-- ");
        		}
        		outBuilder.append(" -end-list- ");
        	}

        	List<Finding> yesFindings = requirement.getYesFindings();
        	if (!yesFindings.isEmpty()) {
        		outBuilder.append(" - Answer: Yes").append('\n');

        		// In this case, there is no need to list the finding contents, only printing the IDs.
                outBuilder.append(" -init-list- ");
                outBuilder.append(" -- ");
                outBuilder.append("Questions: ");
        		for (int i = 0; i < yesFindings.size(); i++) {
        			outBuilder.append(yesFindings.get(i).getQuestionId());
        			if (yesFindings.get(i).hasBeenReviewed()) {
    					outBuilder.append('*');
    				}
        			if (yesFindings.get(i).hasReviewerComments()) {
    			    	outBuilder.append(" (").append(formatReviewerComments(yesFindings.get(i))).append(')');
    			    }
        			if (i == yesFindings.size() -1) {
        				outBuilder.append('.');
        			} else if(i == yesFindings.size() -2) {
        				outBuilder.append(", and ");
        			} else {
        				outBuilder.append(", ");
        			}
        		}
        		outBuilder.append(" /-- ");
        		outBuilder.append(" -end-list- ");
        	}

        	outBuilder.append('\n');
        }

        return HtmlBuilder.generateContent("Requirements View",  outBuilder.toString(), forUI);
	}
	
	private String generateDefaultRequirementsView() {
		return new HtmlBuilder(mOutputDir + "requirements_view_default.html").build(generateDefaultRequirementsViewContent(false));
	}

	private String formatQuestionFinding(String genericPart, List<Finding> findings) {
		StringBuilder sb = new StringBuilder();
		sb.append(genericPart).append('\n');
		sb.append(" -init-list- ");
		for (int i = 0; i < findings.size(); i++) {
			String specificPart = findings.get(i).getSpecificPart();
			if (!Utils.isTextEmpty(specificPart)) {
				sb.append(" -init-list- ");
				sb.append(" -- ");
				sb.append(findings.get(i).getRequirementId());
				if (findings.get(i).hasBeenReviewed()) {
					sb.append('*');
				}
			    sb.append(": ").append(specificPart);
			    if (findings.get(i).hasReviewerComments()) {
			    	sb.append('\n').append(formatReviewerComments(findings.get(i)));
			    }
			} else {
				sb.append(findings.get(i).getRequirementId());
				if (findings.get(i).hasBeenReviewed()) {
					sb.append('*');
				}
				if (findings.get(i).hasReviewerComments()) {
			    	sb.append(" (").append(formatReviewerComments(findings.get(i))).append(')');
			    }
				if (i == findings.size() - 1) {
					sb.append('.');
				} else if (i == findings.size() - 2) {
					sb.append(", and ");
				} else {
					sb.append(", ");
				}
			}

			if (!Utils.isTextEmpty(specificPart)) {
			    sb.append('\n');
			    sb.append(" /-- ");
			    sb.append(" -end-list- ");
			}
		}
		sb.append(" -end-list- ");

		return sb.toString();
	}

	private String formatRequirementFinding(Finding finding) {
		String formatted = "Question " + finding.getQuestionId()
		    + (finding.hasBeenReviewed() ? "*: " : ": ") + finding.getDetail();
		if (finding.hasReviewerComments()) {
			return formatted + '\n' + formatReviewerComments(finding);
		}

		return formatted;
	}

	private String formatReviewerComments(Finding finding) {
		if (!finding.hasReviewerComments()) return null;

		return " -init-reviewer- [Reviewer] " + finding.getReviewerComments() + " -end-reviewer- ";
	}

	public String generateNumericOccurrencesContent(boolean forUI) {
		if (mNumericOcc == null) {
			mNumericOcc = createNumberAndUnitOccurrences();
		}

		if (mNumericOcc.isEmpty()) {
			return HtmlBuilder.generateContent("Numeric Occurrences View",  "No numbers and units were found in the document!", forUI);
		}

		StringBuilder outBuilder = new StringBuilder();
		for (String numVal : mNumericOcc.getAllOccurrences()) {
			outBuilder.append("- Value: ").append(numVal).append('\n');
			outBuilder.append(" -- Occurrences: ");
			boolean first = true;
			for (String occurrence : mNumericOcc.get(numVal)) {
				if (!first) outBuilder.append(", ");
				outBuilder.append(occurrence);
				if (first) first = false;
			}
			outBuilder.append('\n');
		}

		return HtmlBuilder.generateContent("Numeric Occurrences View",  outBuilder.toString(), forUI);
	}

	// Too inefficient method. Should be called only as a safeguard, and not in the main thread (UI cases). 
	private NumberAndUnitOccurrences createNumberAndUnitOccurrences() {
		NumberAndUnitOccurrences numericOcc = new NumberAndUnitOccurrences();
		// Search for questions dealing with numeric occurrences.
		for (QuestionCategory cat : mQuestions) {
			if (cat.getCategoryName() == "incorrectness") {
				for (Question question : cat.getAllQuestions()) {
					if (question.getAction().getType() == QuestionAction.ACTION_TYPE_CHECK_NUMBER_AND_UNIT) {
						for (Finding finding : question.getAllFindings()) {
							if (!Utils.isTextEmpty(finding.getSpecificPart())) {
							    numericOcc.put(finding.getSpecificPart(), finding.getRequirementId());
							}
						}
					}
				}
			}
		}

		return numericOcc;
	}

	private void generateNumericOccurrencesViewFile() {
		new HtmlBuilder(mOutputDir + "numeric_occurrences_view.html").build(generateNumericOccurrencesContent(false));
	}

	private void generateAnalysisFile() {
		saveToFile(mOutputDir + "analysis.csv");
	}

	public void runConsistencyCheck() {
		for (QuestionCategory cat : mQuestions) {
			for (Question question : cat.getAllQuestions()) {
			    question.rebuildForConsistency();
			}
		}
		for (Requirement requirement : mRequirements.getRequirements()) {
			requirement.rebuildForConsistency();
		}
	}

	public void saveToFile(String fileName) {
		List<String[]> arrayList = new ArrayList<String[]>();
		for (Requirement requirement : mRequirements.getRequirements()) {
			for (Finding finding : requirement.getAllFindings()) {
				String[] row = new String[CSVBuilder.HEADER.length];
				int index = 0;
				row[index++] = finding.getRequirementId();
				row[index++] = requirement.getText();
				row[index++] = Integer.toString(finding.getQuestionId());
				row[index++] = finding.getGenericPart();
				row[index++] = finding.getSpecificPart();
				row[index++] = Question.getAnswerStringValue(finding.getAutomatedAnswerType());
				row[index++] = Question.getAnswerStringValue(finding.getReviewedAnswerType());
				row[index++] = finding.getReviewerComments();

				arrayList.add(row);
			}
		}

		CSVBuilder.saveFile(fileName, arrayList);
	}

	public static OutputGenerator createFromFile(String[][] contents, String fileName) {
		ErrorBasedChecklist ebs = new ErrorBasedChecklist();
		List<QuestionCategory> questions = new ArrayList<QuestionCategory>();
		questions.add(ebs.getTraceabilityQuestions());
		questions.add(ebs.getIncompletenessQuestions());
		questions.add(ebs.getIncorrectnessQuestions());
		questions.add(ebs.getInconsistencyQuestions());

		// Using LinkedHashMap to keep the insertion order later.
		Map<String, Requirement> requirementMap = new LinkedHashMap<String, Requirement>();
		NumberAndUnitOccurrences numOccurrences = new NumberAndUnitOccurrences();
		for (String[] row : contents) {
			int index = 0;
			String reqId = row[index++];
			String reqText = row[index++];
			int questionId = Integer.parseInt(row[index++]);
			String genFind = row[index++];
			String specFind = row[index++];
			int answer = Question.getAnswerIntValue(row[index++]);
			int revAnswer = Question.getAnswerIntValue(row[index++]);
			String revComments = row[index++];

			Requirement requirement;
			if (!requirementMap.containsKey(reqId)) {
				requirement = new Requirement(reqId, reqText);
				requirementMap.put(reqId, requirement);
			} else {
				requirement = requirementMap.get(reqId);
			}

			Question question = getQuestion(questionId, questions);
			
			Finding finding = new Finding(questionId, reqId, genFind, specFind, answer);
			finding.setReviewedAnswerType(revAnswer);
			finding.setReviewerComments(revComments);
			requirement.addFinding(finding);
			question.addFinding(finding);

			// Questions dealing with numeric occurrences.
			if ((question.getAction().getType() == QuestionAction.ACTION_TYPE_CHECK_NUMBER_AND_UNIT)
			    && !Utils.isTextEmpty(specFind)) {
			    numOccurrences.put(specFind, reqId);
			}
		}

		return new OutputGenerator(new RequirementList(new ArrayList<Requirement>(requirementMap.values())), questions, numOccurrences, Utils.getParentDirectory(fileName));
	}
}
