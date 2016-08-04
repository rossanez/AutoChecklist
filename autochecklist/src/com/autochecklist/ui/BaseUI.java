package com.autochecklist.ui;

import javafx.stage.Stage;

public abstract class BaseUI {

	protected Stage mStage;

	public BaseUI() {
		mStage = new Stage();
		initUI();
	}

	public BaseUI(Stage primaryStage) {
		mStage = primaryStage;
		initUI();
	}

	protected abstract void initUI();
	
	public final void show() {
		mStage.show();
	}
}
