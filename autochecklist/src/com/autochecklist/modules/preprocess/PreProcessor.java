package com.autochecklist.modules.preprocess;

import java.io.File;
import java.security.InvalidParameterException;

import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.IRequirementsInfoOutBuildable;
import com.autochecklist.utils.nlp.RequirementsInfoExtractor;
import com.autochecklist.utils.text.PlainTextConverter;

public class PreProcessor {

	private String mDocumentFileName;

	public PreProcessor(String docFileName) {
		mDocumentFileName = docFileName;
	}

	public File preProcess() {
		return buildPreProcessedFile(getPlainText());
	}

    private String getPlainText() {
    	if (mDocumentFileName == null) {
			throw new InvalidParameterException("Invalid document filename!");
		}

		return PlainTextConverter.convertFile(mDocumentFileName);
    }

    private String getOutputFileName() {
		return Utils.getParentDirectory(mDocumentFileName) + "preproc.xml";
	}

    private File buildPreProcessedFile(String text) {
    	if (text == null) {
    		throw new InvalidParameterException("Invalid plain text passed in!");
    	}

        RequirementsInfoExtractor extractor = new RequirementsInfoExtractor(text, new XMLOutPreProcBuilder());
        return extractor.extract().generateOutputFile(getOutputFileName());
    }
}
