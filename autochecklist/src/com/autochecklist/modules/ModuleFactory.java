package com.autochecklist.modules;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.base.ErrorBasedChecklist;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.RequirementList;
import com.autochecklist.modules.incompleteness.Incompleteness;
import com.autochecklist.modules.inconsistency.Inconsistency;
import com.autochecklist.modules.incorrectness.Incorrectness;
import com.autochecklist.modules.output.OutputFormatter;
import com.autochecklist.modules.preprocess.PreProcessor;
import com.autochecklist.modules.traceability.Traceability;

public class ModuleFactory {

	public static PreProcessor createPreProcessor(String srsFileName) {
		return new PreProcessor(srsFileName);
	}

	public static Traceability createTraceability(ErrorBasedChecklist check) {
		return new Traceability(check.getTraceabilityQuestions());
	}

	public static Incompleteness createIncompleteness(ErrorBasedChecklist check) {
		return new Incompleteness(check.getIncompletenessQuestions());
	}

	public static Incorrectness createIncorrectness(ErrorBasedChecklist check) {
		return new Incorrectness(check.getIncorrectnessQuestions());
	}

	public static Inconsistency createInconsistency(ErrorBasedChecklist check) {
		return new Inconsistency(check.getInconsistencyQuestions());
	}

	public static OutputFormatter createOutputFormatter(RequirementList analyzedRequirements,
			List<QuestionCategory> answeredQuestions, String workingDir) {
		return new OutputFormatter(analyzedRequirements, answeredQuestions, workingDir);
	}

	public static List<AnalysisModule> createAllAnalysisModules(ErrorBasedChecklist check) {
		List<AnalysisModule> list = new ArrayList<AnalysisModule>();
		list.add(createTraceability(check));
		list.add(createIncompleteness(check));
		list.add(createIncorrectness(check));
		list.add(createInconsistency(check));
		return list;
	}
}
