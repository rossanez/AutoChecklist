package com.autochecklist.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.autochecklist.base.NumberAndUnitOccurrences;
import com.autochecklist.base.documentsections.DocumentSectionList;
import com.autochecklist.base.documentsections.RequirementsTraceabilityMatrix;
import com.autochecklist.base.questions.QuestionCategory;
import com.autochecklist.base.requirements.RequirementList;
import com.autochecklist.modules.analysis.AnalysisModule;
import com.autochecklist.modules.analysis.AnalysisModuleFactory;
import com.autochecklist.modules.analysis.Incorrectness;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.checklists.ErrorBasedChecklist;

/**
 * This class should guide the execution flows of the tool.
 * @author Anderson Rossanez
 */
public class Orchestrator {
	
	private String mSRSFileName;
	private String mPreProcFileName;
	private RequirementList mRequirements;
	private NumberAndUnitOccurrences mNumericOcc;
	private DocumentSectionList mDocumentSections;
	private RequirementsTraceabilityMatrix mRTMSection;

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
		mDocumentSections = new DocumentSectionList(mPreProcFileName);
		mRTMSection = new RequirementsTraceabilityMatrix(mPreProcFileName);
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
	 * Performs the analysis of a previously pre-processed SRS and starts the OutputGenerator.
	 */
	public void analyze() {
		analyzeOnly().start();
	}

	/**
	 * Performs the analysis of a previously pre-processed SRS and returns the OutputGenerator.
	 * @return The OutputGenerator.
	 */
	public OutputGenerator analyzeOnly() {
		if (mPreProcFileName == null) {
			Utils.printError("No preprocessed file for analysis!");
        	throw new RuntimeException("Need a preprocessed file for analysis!");
        }

		Pair<RequirementList, List<QuestionCategory>> out =
				new Pair<RequirementList, List<QuestionCategory>>(mRequirements, doAnalyze());
		return getOutputGenerator(out);
	}

	private File preProcess() {
		if (mSRSFileName == null) {
			Utils.printError("No SRS file for preprocessing!");
        	throw new RuntimeException("Need a SRS file for preprocessing!");
        }

		PreProcessor preproc = new PreProcessor(mSRSFileName);
		preproc.start();
		return preproc.getPreprocessedFile();
	}

	private List<QuestionCategory> doAnalyze() {
		List<QuestionCategory> processedQuestions = new ArrayList<QuestionCategory>();
		Queue<AnalysisModule> queue = getAllAnalysisModules();
		while (!queue.isEmpty()) {
			AnalysisModule module = queue.poll();
            processedQuestions.add(module.analyze(mRequirements));
			if (module instanceof Incorrectness) {
				mNumericOcc = ((Incorrectness) module).getNumericOccurrences();
			}
		}
	
		return processedQuestions;
	}

	private Queue<AnalysisModule> getAllAnalysisModules() {
		return AnalysisModuleFactory.createErrorBasedAnalysisModules(new ErrorBasedChecklist(), mDocumentSections, mRTMSection);
	}

	private OutputGenerator getOutputGenerator(Pair<RequirementList, List<QuestionCategory>> output) {
		return new OutputGenerator(output.first, output.second, mNumericOcc, Utils.getParentDirectory(mPreProcFileName));
	}
}