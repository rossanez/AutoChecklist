package com.autochecklist.ui.modules;

import com.autochecklist.ui.BaseUI;

public class AnalysisUI extends BaseUI {

	private String mPreprocFileName;
	
	public AnalysisUI(String preprocFileName) {
		super();
		mPreprocFileName = preprocFileName;
	}

	@Override
	protected void initUI() {
		mStage.setTitle("Auto Checklist - Analysis");
	}
}
