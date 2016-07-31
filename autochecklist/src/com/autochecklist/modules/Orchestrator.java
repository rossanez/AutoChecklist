package com.autochecklist.modules;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.model.ErrorBasedChecklist;
import com.autochecklist.model.questions.QuestionCategory;
import com.autochecklist.model.requirements.RequirementList;
import com.autochecklist.modules.output.OutputFormatter;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

public class Orchestrator {
	
	private String mPreProcFile;
	private RequirementList mRequirements;
	
	public Orchestrator(String preProcFile) {
		mPreProcFile = preProcFile;
		mRequirements = new RequirementList(mPreProcFile);
	}

	public void start() {
		Pair<RequirementList, List<QuestionCategory>> out =
				new Pair<RequirementList, List<QuestionCategory>>(mRequirements, analyze());
		getOutputFormatter(out).processOutput();
	}
	
	private List<QuestionCategory> analyze() {
		List<QuestionCategory> processedQuestions = new ArrayList<QuestionCategory>();
		for (Module module : getAllAnalysisModules()) {
			processedQuestions.add(module.analyze(mRequirements));
		}
	
		return processedQuestions;
	}

	private List<Module> getAllAnalysisModules() {
        if (mPreProcFile == null) {
        	throw new RuntimeException("Need a pre-processed file for analysis!");
        }

		ErrorBasedChecklist errorChecklist = new ErrorBasedChecklist();
		List<Module> list = new ArrayList<Module>();
		list.add(ModuleFactory.createTraceability(errorChecklist));
		list.add(ModuleFactory.createIncompleteness(errorChecklist));
		list.add(ModuleFactory.createIncorrectness(errorChecklist));
		list.add(ModuleFactory.createInconsistency(errorChecklist));
		return list;
	}

	private OutputFormatter getOutputFormatter(Pair<RequirementList, List<QuestionCategory>> output) {
		return new OutputFormatter(output.first, output.second, Utils.getParentDirectory(mPreProcFile));
	}
}