package com.autochecklist.modules.output;

import java.io.File;
import java.util.List;

import com.autochecklist.model.Finding;
import com.autochecklist.model.questions.Question;
import com.autochecklist.model.questions.QuestionCategory;
import com.autochecklist.model.requirements.Requirement;
import com.autochecklist.model.requirements.RequirementList;
import com.autochecklist.modules.Module;
import com.autochecklist.utils.Utils;

public class OutputFormatter extends Module {

	private RequirementList mRequirements;
	private List<QuestionCategory> mQuestions;

	private String mOutputDir;
	
	public OutputFormatter(RequirementList analyzedRequirements, List<QuestionCategory> answeredQuestions, String workingDir) {
		mRequirements = analyzedRequirements;
		mQuestions = answeredQuestions;

		mOutputDir = setOutputDirectory(workingDir);
	}

	private String setOutputDirectory(String dir) {
		return dir + "AnalysisOutput" + File.separatorChar;
	}

	@Override
	public void start() {
		createOutputDirectory();
		generateQuestionsView();
		generateRequirementsView();
	}

	private File createOutputDirectory() {
		return Utils.createDirectory(mOutputDir);
	}

	private String generateQuestionsView() {
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

		return new HtmlBuilder(mOutputDir + "checklist_view.html").build("Checklist View", outBuilder.toString());
	}

	private String generateRequirementsView() {
		return generateDefaultRequirementsView();
	}

	private String generateDefaultRequirementsView() {
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

		return new HtmlBuilder(mOutputDir + "requirements_view_default.html").build("Requirements View",  outBuilder.toString());
	}

	private String formatQuestionFinding(Finding finding) {
		return "Requirement " + finding.getRequirementId() + ": " + finding.getDetail();
	}

	private String formatRequirementFinding(Finding finding) {
		return "Question " + finding.getQuestionId() + ": " + finding.getDetail();
	}
}
