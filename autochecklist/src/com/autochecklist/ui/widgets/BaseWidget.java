package com.autochecklist.ui.widgets;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class BaseWidget {

	protected Stage mStage;

	private boolean mInitialized = false;

	public BaseWidget(Stage ownerStage) {
		mStage = new Stage();
		mStage.initModality(Modality.APPLICATION_MODAL);
		mStage.setResizable(false);
		mStage.setMaximized(false);
		mStage.initStyle(StageStyle.UTILITY);
		mStage.initOwner(ownerStage);
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
