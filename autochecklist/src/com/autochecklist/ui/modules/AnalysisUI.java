package com.autochecklist.ui.modules;

import com.autochecklist.ui.BaseUI;
import com.autochecklist.utils.Utils;

public class AnalysisUI extends BaseUI {

	private String mPreprocFileName;
	
	public AnalysisUI(String preprocFileName) {
		super();
		mPreprocFileName = preprocFileName;
	}

	@Override
	protected void initUI() {
	    if (Utils.isTextEmpty(mPreprocFileName)) {
	    	throw new RuntimeException("No preprocessed file name passed in to the UI!");
	    }

		mStage.setTitle("Auto Checklist - Analysis");
	}
}
