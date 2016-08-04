package com.autochecklist.ui.modules;

import com.autochecklist.ui.BaseUI;
import com.autochecklist.utils.Utils;

public class PreprocUI extends BaseUI {

	private String mSRSFileName;

	public PreprocUI(String srsFileName) {
		super();
		mSRSFileName = srsFileName;
	}

	@Override
	protected void initUI() {
		if (Utils.isTextEmpty(mSRSFileName)) {
			throw new RuntimeException("No SRS file name passed in to the UI!");
		}

		mStage.setTitle("Auto Checklist - Preprocessing");
	}
}
