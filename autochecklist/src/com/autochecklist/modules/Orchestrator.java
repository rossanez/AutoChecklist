package com.autochecklist.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.autochecklist.base.ErrorBasedChecklist;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.RequirementList;
import com.autochecklist.modules.output.OutputFormatter;
import com.autochecklist.modules.preprocess.PreProcessor;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

/**
 * This class should guide the execution flows of the tool.
 * @author Anderson Rossanez
 */
public class Orchestrator {
	
	private String mSRSFileName;
	private String mPreProcFileName;
	private RequirementList mRequirements;

	/**
	 * This constructor should be used for passing a SRS file only.
	 * @param srsFile The Software Requirements Specification document file
	 */
	public Orchestrator(String srsFile) {
		this(srsFile, false);
	}

	/**
	 * This constructor can be used for passing an input file (SRS or pre-processed)
	 * @param inputFileName The input file name
	 * @param isPreprocessedFile Tells whether the input file is a SRS (false) or a pre-processed file (true).
	 */
	public Orchestrator(String inputFileName, boolean isPreprocessedFile) {
		if (isPreprocessedFile) {
		    initParamsAfterPreproc(inputFileName);
		} else {
			mSRSFileName = inputFileName;
		}
	}

	private void initParamsAfterPreproc(String preProcFile) {
		mPreProcFileName = preProcFile;
		mRequirements = new RequirementList(mPreProcFileName);
	}

	/**
	 * Performs the pre-processing of the SRS file and then runs the analysis.
	 */
	public void preProcessThenAnalyze() {
		initParamsAfterPreproc(preProcess().getPath());
		analyze();
	}

	/**
	 * Performs the pre-processing of the SRS only.
	 */
	public void preProcessOnly() {
		File preOut = preProcess();
		Utils.println("Generated the following pre-processed file: " + preOut.getPath());
	}

	/**
	 * Performs the analysis of a previously pre-processed SRS.
	 */
	public void analyze() {
		if (mPreProcFileName == null) {
        	throw new RuntimeException("Need a pre-processed file for analysis!");
        }

		Pair<RequirementList, List<QuestionCategory>> out =
				new Pair<RequirementList, List<QuestionCategory>>(mRequirements, doAnalyze());
		getOutputFormatter(out).start();
	}

	private File preProcess() {
		if (mSRSFileName == null) {
        	throw new RuntimeException("Need a SRS file for pre-processing!");
        }

		PreProcessor preproc = ModuleFactory.createPreProcessor(mSRSFileName);
		preproc.start();
		return preproc.getPreprocessedFile();
	}

	private List<QuestionCategory> doAnalyze() {
		List<QuestionCategory> processedQuestions = new ArrayList<QuestionCategory>();
		for (AnalysisModule module : getAllAnalysisModules()) {
			processedQuestions.add(module.analyze(mRequirements));
		}
	
		return processedQuestions;
	}

	private List<AnalysisModule> getAllAnalysisModules() {
		return ModuleFactory.createAllAnalysisModules(new ErrorBasedChecklist());
	}

	private OutputFormatter getOutputFormatter(Pair<RequirementList, List<QuestionCategory>> output) {
		return new OutputFormatter(output.first, output.second, Utils.getParentDirectory(mPreProcFileName));
	}
}