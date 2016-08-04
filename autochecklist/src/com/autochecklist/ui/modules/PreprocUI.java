package com.autochecklist.ui.modules;

import com.autochecklist.ui.BaseUI;

public class PreprocUI extends BaseUI {

	private String mSRSFileName;

	public PreprocUI(String srsFileName) {
		super();
		mSRSFileName = srsFileName;
	}

	@Override
	protected void initUI() {
		mStage.setTitle("Auto Checklist - Preprocessing");
	}
}
