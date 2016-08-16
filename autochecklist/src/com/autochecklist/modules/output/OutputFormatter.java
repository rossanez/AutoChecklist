package com.autochecklist.modules.output;

import java.io.File;
import java.util.List;

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
				outBuilder.append(" - Answer: ").append(question.getAnswerAsString()).append('\n');
				List<Finding> findings = question.getFindings();
				for (Finding finding : findings) {
					outBuilder.append(" -- ").append(formatQuestionFinding(finding)).append('\n');
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
 
        	List<Finding> noFindings = requirement.getNoFindings();
        	if (!noFindings.isEmpty()) {
        		outBuilder.append(" - Answer: No").append('\n');
        		for (Finding finding : noFindings) {
        			outBuilder.append(" -- ").append(formatRequirementFinding(finding)).append('\n');
        		}
        	}

        	List<Finding> warningFindings = requirement.getWarningFindings();
        	if (!warningFindings.isEmpty()) {
        		outBuilder.append(" - Answer: Warning").append('\n');
        		for (Finding finding : warningFindings) {
        			outBuilder.append(" -- ").append(formatRequirementFinding(finding)).append('\n');
        		}
        	}
        	
        	outBuilder.append('\n');
        }

        return HtmlBuilder.generateContent("Requirements View",  outBuilder.toString(), forUI);
	}
	
	private String generateDefaultRequirementsView() {
		return new HtmlBuilder(mOutputDir + "requirements_view_default.html").build(generateDefaultRequirementsViewContent(false));
	}

	private String formatQuestionFinding(Finding finding) {
		return "Requirement " + finding.getRequirementId() + ": " + finding.getDetail();
	}

	private String formatRequirementFinding(Finding finding) {
		return "Question " + finding.getQuestionId() + ": " + finding.getDetail();
	}

	public String generateNumericOccurrencesContent(boolean forUI) {
		if (mNumericOcc == null) {
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
