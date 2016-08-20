package com.autochecklist.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.autochecklist.base.ErrorBasedChecklist;
import com.autochecklist.base.NumberAndUnitOccurrences;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.RequirementList;
import com.autochecklist.modules.incorrectness.Incorrectness;
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
	private NumberAndUnitOccurrences mNumericOcc;

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
	public File preProcessOnly() {
		File preOut = preProcess();
		Utils.println("Generated the following pre-processed file: " + preOut.getPath());
		return preOut;
	}

	/**
	 * Performs the analysis of a previously pre-processed SRS and starts the OutputFormatter.
	 */
	public void analyze() {
		analyzeOnly().start();
	}

	/**
	 * Performs the analysis of a previously pre-processed SRS and returns the OutputFormatter.
	 * @return The OutputFormatter.
	 */
	public OutputFormatter analyzeOnly() {
		if (mPreProcFileName == null) {
			Utils.printError("No preprocessed file for analysis!");
        	throw new RuntimeException("Need a preprocessed file for analysis!");
        }

		Pair<RequirementList, List<QuestionCategory>> out =
				new Pair<RequirementList, List<QuestionCategory>>(mRequirements, doAnalyze());
		return getOutputFormatter(out);
	}

	private File preProcess() {
		if (mSRSFileName == null) {
			Utils.printError("No SRS file for preprocessing!");
        	throw new RuntimeException("Need a SRS file for preprocessing!");
        }

		PreProcessor preproc = ModuleFactory.createPreProcessor(mSRSFileName);
		preproc.start();
		return preproc.getPreprocessedFile();
	}

	private List<QuestionCategory> doAnalyze() {
		List<QuestionCategory> processedQuestions = new ArrayList<QuestionCategory>();
		for (AnalysisModule module : getAllAnalysisModules()) {
			processedQuestions.add(module.analyze(mRequirements));
			if (module instanceof Incorrectness) {
				mNumericOcc = ((Incorrectness) module).getNumericOccurrences();
			}
		}
	
		return processedQuestions;
	}

	private List<AnalysisModule> getAllAnalysisModules() {
		return ModuleFactory.createAllAnalysisModules(new ErrorBasedChecklist());
	}

	private OutputFormatter getOutputFormatter(Pair<RequirementList, List<QuestionCategory>> output) {
		return new OutputFormatter(output.first, output.second, mNumericOcc, Utils.getParentDirectory(mPreProcFileName));
	}
}