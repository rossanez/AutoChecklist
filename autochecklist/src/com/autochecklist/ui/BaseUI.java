package com.autochecklist.ui;

import com.autochecklist.ui.screens.InitialUI;
import com.autochecklist.ui.widgets.ChoiceDialog;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public abstract class BaseUI implements PrintingService.IUIPrintable, EventHandler<ActionEvent> {

	protected Stage mStage;
	
	private boolean mInitialized = false;
    private boolean mDoWorkUponShowing = false;

	protected MenuItem mMenuRestart;
	protected MenuItem mMenuExit;

	protected TextArea mBuffer;

	protected Task<Void> mTask;
	protected Thread mThread;

	public BaseUI() {
		mStage = new Stage();
	}

	public BaseUI(Stage primaryStage) {
		mStage = primaryStage;
	}

	protected abstract void initUI();

	/**
	 * Should be called on the constructor for starting the work on showing the UI.
	 * @param value Tells if the work should or not be started.
	 */
	protected final void setDoWorkUponShowing(boolean value) {
		mDoWorkUponShowing = value;
	}

	public final void show() {
		if (!mInitialized) {
			initUI();
			PrintingService.getInstance().register(this);
			mInitialized = true;

			if (mDoWorkUponShowing) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						work();
					}
				});
			}

			mStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					onExternalCloseRequest(event);
				}
			});
		}

		mStage.show();
	}

	protected final void work() {
		beforeWork();

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
		mThread = new Thread(mTask);
		mThread.setDaemon(true);
		mThread.start();
	}

	protected void beforeWork() { };

	protected void doWork() { };

	protected void workSucceeded() { };
	
	protected void workFailed(boolean cancelled) { };

	protected final void stopWork() {
		if (mTask != null) {
			if (!mTask.cancel()) {
				// Try once again as a workaround
				mTask.cancel(true);
			}
		}
		if (mThread != null) {
			mThread.interrupt();
		}
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == mMenuExit) {
			showExitDialog();
		} else if (event.getSource() == mMenuRestart) {
			showRestartDialog();
		}
	}

	private void showRestartDialog() {
		new ChoiceDialog("Restarting...",
				"The current analysis will be dropped!\nAre you sure about it?",
				new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						stopWork();
						mStage.close();
						new InitialUI().show();
					}
				}, null).show();
	}

	private void showExitDialog() {
		new ChoiceDialog("Leaving...",
				"Do you really want to quit?",
				new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						stopWork();
						Platform.exit();
					}
				}, null).show();
	}

	protected void onExternalCloseRequest(WindowEvent windowEvent) {
		windowEvent.consume();
		showExitDialog();
	}

	public final void close() {
		if (mStage != null) {
			stopWork();
			mStage.close();
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
