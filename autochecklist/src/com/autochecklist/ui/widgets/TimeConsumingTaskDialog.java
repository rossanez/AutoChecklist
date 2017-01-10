package com.autochecklist.ui.widgets;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TimeConsumingTaskDialog extends BaseWidget {

	private String mTitle;

	private ITimeConsumingTask mCallback;

	private ProgressIndicator mProgressIndicator;

    private Task<Void> mTask;
    private Thread mThread;

	public TimeConsumingTaskDialog(String title, ITimeConsumingTask callback, Stage ownerStage) {
		super(ownerStage);
        mTitle = title;
        mCallback = callback;
	}

	@Override
	protected void initWidget() {
		mStage.setTitle(mTitle);

		VBox progressContent = new VBox(10);
		progressContent.setAlignment(Pos.CENTER);
		mProgressIndicator = new ProgressIndicator();
		mProgressIndicator.setProgress(-1); // Indeterminate.
		progressContent.prefWidthProperty().bind(mStage.widthProperty());
		progressContent.prefHeightProperty().bind(mStage.heightProperty());
		Label label = new Label("Please wait...");
		progressContent.getChildren().addAll(mProgressIndicator, label);

		VBox layout = new VBox(10);
		layout.getChildren().add(progressContent);
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 400, 125);
		mStage.setScene(scene);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				mProgressIndicator.setProgress(-1); // Indeterminate.
				mCallback.doBefore();

				mTask = new Task<Void>() {

					@Override
					protected Void call() throws Exception {
						mCallback.doInBackground();

						return null;
					}

					@Override
					protected void cancelled() {
						mProgressIndicator.setProgress(0);
						mCallback.onFailure(true);
						close();
					}

					@Override
					protected void failed() {
						mProgressIndicator.setProgress(0);
						mCallback.onFailure(false);
						close();
					}

					@Override
					protected void succeeded() {
						mCallback.onSuccess();
						close();
					}
				};

				mThread = new Thread(mTask);
				mThread.setDaemon(true);
				mThread.start();
			}
		});

		mStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				stopWork();
			}
		});
	}

	private void stopWork() {
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

	public interface ITimeConsumingTask {

		void doBefore();

		void doInBackground();

		void onSuccess();

		void onFailure(boolean cancelled);
	}
}
