package com.autochecklist;

import com.autochecklist.modules.Orchestrator;
import com.autochecklist.ui.StartupUI;
import com.autochecklist.utils.Utils;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			StartupUI.initUI(args);
			return;
		} else if (args.length >= 2) {
			if (args[0].equals("--srs")) {
				if ((args.length == 3) && (args[2].equals("--pre-only"))) {
					new Orchestrator(args[1]).preProcessOnly();
					return;
				} else if (args.length == 2) {
					new Orchestrator(args[1]).preProcessThenAnalyze();
				    return;
				}
			} else if (args[0].equals("--pre")) {
				new Orchestrator(args[1], true).analyze();
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
}
