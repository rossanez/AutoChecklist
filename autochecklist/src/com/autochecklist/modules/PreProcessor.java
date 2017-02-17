package com.autochecklist.modules;

import java.io.File;
import java.security.InvalidParameterException;

import com.autochecklist.utils.Utils;
import com.autochecklist.utils.filebuilder.XMLPreProcBuilder;
import com.autochecklist.utils.nlp.NLPTools;
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
		return Utils.getParentDirectory(mDocumentFileName) + Utils.getFileNameWithoutExtension(mDocumentFileName) + "_preproc.xml";
	}

    private File buildPreProcessedFile(String text) {
    	if (text == null) {
    		Utils.printError("No plain text content!");
    		throw new InvalidParameterException("Invalid plain text passed in!");
    	}

    	if (!mDocumentFileName.toLowerCase().endsWith(".txt")) {
    		Utils.createTextFile(Utils.getParentDirectory(mDocumentFileName) + Utils.getFileNameWithoutExtension(mDocumentFileName) + "_text.txt", text);
    	}
    	
    	XMLPreProcBuilder outputBuilder = new XMLPreProcBuilder();

    	NLPTools.extractPreprocInfoAndWrite(text, outputBuilder);

        return outputBuilder.generateOutputFile(getOutputFileName());
    }
}
