package com.autochecklist.modules.preprocess;

import java.io.File;
import java.security.InvalidParameterException;

import com.autochecklist.modules.Module;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.nlp.DocumentSectionsExtractor;
import com.autochecklist.utils.nlp.IRequirementsInfoOutBuildable;
import com.autochecklist.utils.nlp.RequirementsInfoExtractor;
import com.autochecklist.utils.text.PlainTextConverter;

public class PreProcessor extends Module {

	private String mDocumentFileName;
	private File mResult;

	public PreProcessor(String docFileName) {
		mDocumentFileName = docFileName;
	}

	@Override
	public void start() {
		 mResult = buildPreProcessedFile(getPlainText());
	}

	public File getPreprocessedFile() {
		return mResult;
	}

    private String getPlainText() {
    	if (mDocumentFileName == null) {
    		Utils.printError("No document file to be preprocessed!");
			throw new InvalidParameterException("Invalid document filename!");
		}

		return PlainTextConverter.convertFile(mDocumentFileName);
    }

    private String getOutputFileName() {
		return Utils.getParentDirectory(mDocumentFileName) + "preproc.xml";
	}

    private File buildPreProcessedFile(String text) {
    	if (text == null) {
    		Utils.printError("No plain text content!");
    		throw new InvalidParameterException("Invalid plain text passed in!");
    	}
    	
    	IRequirementsInfoOutBuildable outputBuilder = new XMLOutPreProcBuilder();

    	new DocumentSectionsExtractor(text, outputBuilder).extract();
        new RequirementsInfoExtractor(text, outputBuilder).extract();

        return outputBuilder.generateOutputFile(getOutputFileName());
    }
}
