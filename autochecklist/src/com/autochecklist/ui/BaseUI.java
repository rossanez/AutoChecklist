package com.autochecklist.ui;

import com.autochecklist.ui.widgets.ChoiceDialog;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public abstract class BaseUI implements IUIPrintable, EventHandler<ActionEvent> {

	protected Stage mStage;
	
	private boolean mInitialized = false;

	protected MenuItem mMenuRestart;
	protected MenuItem mMenuExit;

	protected TextArea mBuffer;

	protected Task<Void> mTask;

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
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					work();
				}
			});
			mStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					if (mTask != null) {
						mTask.cancel();
					}
				}
			});
		}

		mStage.show();
	}

	protected final void work() {
		mTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				doWork();

				return null;
			}

			@Override
			protected void cancelled() {
				workFailed(true);
			}

			@Override
			protected void failed() {
				workFailed(false);
			}

			@Override
			protected void succeeded() {
				workSucceeded();
			}

			
		};
		new Thread(mTask).start();
	}

	protected void doWork() { };

	protected void workSucceeded() { };
	
	protected void workFailed(boolean cancelled) { };

	protected final void stopWork() {
		if (mTask != null) {
			mTask.cancel();
		}
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == mMenuExit) {
			new ChoiceDialog("Leaving...",
		    		"Do you really want to quit?",
					new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							if (mTask != null) {
								mTask.cancel();
							}
							Platform.exit();
						}
					}, null).show();
		} else if (event.getSource() == mMenuRestart) {
			new ChoiceDialog("Restarting...",
		    		"The current analysis will be stopped!\nAre you sure about it?",
					new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							if (mTask != null) {
								mTask.cancel();
							}
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
			// Caution: This will change the whole text to red!
			mBuffer.setStyle("-fx-text-inner-color: red;");
			mBuffer.appendText('\n'+ message);
		}
	}
}
