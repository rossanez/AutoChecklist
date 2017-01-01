package com.autochecklist.modules.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.autochecklist.base.Finding;
import com.autochecklist.base.NumberAndUnitOccurrences;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.base.requirements.RequirementList;
import com.autochecklist.modules.Module;
import com.autochecklist.utils.HtmlBuilder;
import com.autochecklist.utils.Utils;

public class OutputFormatter extends Module {

	private RequirementList mRequirements;
	private List<QuestionCategory> mQuestions;
	private NumberAndUnitOccurrences mNumericOcc;

	private String mOutputDir;
	
	public OutputFormatter(RequirementList analyzedRequirements, List<QuestionCategory> answeredQuestions, NumberAndUnitOccurrences numericOcc, String workingDir) {
		mRequirements = analyzedRequirements;
		mQuestions = answeredQuestions;
        mNumericOcc = numericOcc;
		mOutputDir = setOutputDirectory(workingDir);
	}

	private String setOutputDirectory(String dir) {
		return dir + "AnalysisOutput" + File.separatorChar;
	}

	public List<Requirement> getRequirements() {
		return mRequirements.getRequirements();
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

	private String generateQuestionsViewFile() {
		return new HtmlBuilder(mOutputDir + "checklist_view.html").build(generateQuestionsViewContent(false));
	}

	public String generateRequirementsViewContent(boolean forUI) {
		return generateDefaultRequirementsViewContent(forUI);
	}
	
	private String generateRequirementsViewFile() {
		return generateDefaultRequirementsView();
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
			    sb.append(": ").append(specificPart);
			} else {
				sb.append(findings.get(i).getRequirementId());
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
		return "Question " + finding.getQuestionId() + ": " + finding.getDetail();
	}

	public String generateNumericOccurrencesContent(boolean forUI) {
		if (mNumericOcc == null) {
			Utils.printError("No numeric occurrences reference on output formatter!");
			throw new RuntimeException("Not able to generate numeric occurrences content!");
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

	private String generateNumericOccurrencesViewFile() {
		return new HtmlBuilder(mOutputDir + "numeric_occurrences_view.html").build(generateNumericOccurrencesContent(false));
	}
}
