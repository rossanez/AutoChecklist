package com.autochecklist.ui.widgets;

import javafx.stage.Stage;

public abstract class BaseWidget {

	protected Stage mStage;

	private boolean mInitialized = false;

	public BaseWidget() {
		mStage = new Stage();
	}

	protected abstract void initWidget();

	public final void show() {
		if (!mInitialized) {
			initWidget();
			mInitialized = true;
		}

		mStage.show();
	}

	public final void close() {
		if (mStage != null) {
			mStage.close();
		}
	}
}
