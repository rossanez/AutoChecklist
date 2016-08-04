package com.autochecklist.ui.widgets;

import javafx.stage.Stage;

public abstract class BaseWidget {

	protected Stage mStage;

	public BaseWidget() {
		mStage = new Stage();
		initWidget();
	}

	protected abstract void initWidget();

	public final void show() {
		mStage.show();
	}
}
