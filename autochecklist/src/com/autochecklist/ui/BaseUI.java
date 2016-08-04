package com.autochecklist.ui;

import javafx.stage.Stage;

public abstract class BaseUI {

	protected Stage mStage;
	
	private boolean mInitialized = false;

	public BaseUI() {
		mStage = new Stage();
	}

	public BaseUI(Stage primaryStage) {
		mStage = primaryStage;
	}

	protected abstract void initUI();
	
	public final void show() {
		if (!mInitialized) {
			initUI();
			mInitialized = true;
		}

		mStage.show();
	}
}
