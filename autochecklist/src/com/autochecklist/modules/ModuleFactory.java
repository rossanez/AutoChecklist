package com.autochecklist.modules;

import com.autochecklist.model.ErrorBasedChecklist;
import com.autochecklist.modules.incompleteness.Incompleteness;
import com.autochecklist.modules.inconsistency.Inconsistency;
import com.autochecklist.modules.incorrectness.Incorrectness;
import com.autochecklist.modules.traceability.Traceability;

public class ModuleFactory {

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
}
