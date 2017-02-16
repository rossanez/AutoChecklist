package com.autochecklist.modules.analysis;

import java.util.LinkedList;
import java.util.Queue;

import com.autochecklist.base.documentsections.DocumentSectionList;
import com.autochecklist.base.documentsections.RequirementsTraceabilityMatrix;
import com.autochecklist.utils.checklists.ErrorBasedChecklist;

public class AnalysisModuleFactory {

	public static Queue<AnalysisModule> createErrorBasedAnalysisModules(ErrorBasedChecklist ebChecklist,
			 DocumentSectionList sections,
			 RequirementsTraceabilityMatrix rtmSection) {
		Queue<AnalysisModule> queue = new LinkedList<AnalysisModule>();
        queue.add(createTraceability(ebChecklist, sections, rtmSection));
        queue.add(createIncompleteness(ebChecklist));
        queue.add(createIncorrectness(ebChecklist));
        queue.add(createInconsistency(ebChecklist));
        return queue;
	}

	private static Traceability createTraceability(ErrorBasedChecklist check,
			                                      DocumentSectionList sections,
			                                      RequirementsTraceabilityMatrix rtmSection) {
		return new Traceability(check.getTraceabilityQuestions(), sections, rtmSection);
	}

	private static Incompleteness createIncompleteness(ErrorBasedChecklist check) {
		return new Incompleteness(check.getIncompletenessQuestions());
	}

	private static Incorrectness createIncorrectness(ErrorBasedChecklist check) {
		return new Incorrectness(check.getIncorrectnessQuestions());
	}

	private static Inconsistency createInconsistency(ErrorBasedChecklist check) {
		return new Inconsistency(check.getInconsistencyQuestions());
	}
}
