package com.autochecklist;

import java.io.File;

import com.autochecklist.modules.Orchestrator;
import com.autochecklist.modules.preprocess.PreProcessor;
import com.autochecklist.utils.Utils;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			Utils.outputType = Utils.OUTPUT_USER_INTERFACE;
			// TODO: Start up UI at this point.
			return;
		} else if (args.length >= 2) {
			if (args[0].equals("--srs")) {
				if ((args.length == 3) && (args[2].equals("--pre-only"))) {
					File preOut = preProcess(args[1]);
					Utils.println("Generated pre-processed file: " + preOut.getPath());
					return;
				} else if (args.length == 2) {
				    preProcessThenAnalyze(args[1]);
				    return;
				}
			} else if (args[0].equals("--pre")) {
				analyze(args[1]);
				return;
			}
		}
		
		Utils.printError("Wrong parameters!");
		Utils.println("\nUSAGE:");
		Utils.println("UI mode: no parameters");
		Utils.println("to pre-process a SRS document and analyze it: --srs <SRS file>");
		Utils.println("to pre-process a SRS document only: --srs <SRS file> --pre-only");
		Utils.println("to analyze a previously pre-processed file: --pre <pre-processed file (XML)>");
	}

	private static File preProcess(String srsFile) {
		return new PreProcessor(srsFile).preProcess();
	}

    private static void preProcessThenAnalyze(String srsFile) {
    	File preOut = preProcess(srsFile);
	    analyze(preOut.getPath());
    }
	
	private static void analyze(String preProcFile) {
		new Orchestrator(preProcFile).start();
	}
}
