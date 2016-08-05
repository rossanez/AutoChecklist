package com.autochecklist.ui;

import com.autochecklist.ui.widgets.ChoiceDialog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public abstract class BaseUI implements IUIPrintable, EventHandler<ActionEvent> {

	protected Stage mStage;
	
	private boolean mInitialized = false;

	protected MenuItem mMenuRestart;
	protected MenuItem mMenuExit;

	protected TextArea mBuffer;

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
			Printer.getInstance().register(this);
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
		} else if (event.getSource() == mMenuRestart) {
			new ChoiceDialog("Restarting...",
		    		"The current analysis will be dropped!\nAre you sure about it?",
					new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							mStage.close();
							new InitialUI().show();
						}
					}, null).show();
		}
	}

	@Override
	public void print(String message) {
		if (mBuffer != null) {
			mBuffer.appendText(message);
		}
	}

	@Override
	public void println(String message) {
		if (mBuffer != null) {
			mBuffer.appendText('\n'+ message);
		}
	}

	@Override
	public void printError(String message) {
		if (mBuffer != null) {
			mBuffer.appendText('\n'+ message);
		}
	}
}
