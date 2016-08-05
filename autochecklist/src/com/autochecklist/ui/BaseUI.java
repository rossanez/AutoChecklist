package com.autochecklist.ui;

import com.autochecklist.ui.widgets.ChoiceDialog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public abstract class BaseUI implements EventHandler<ActionEvent> {

	protected Stage mStage;
	
	private boolean mInitialized = false;

	protected MenuItem mMenuExit;

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

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == mMenuExit) {
			new ChoiceDialog("Leaving...",
		    		"Do you really want to quit?",
					new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							Platform.exit();
						}
					}, null).show();
		}
	}
}
