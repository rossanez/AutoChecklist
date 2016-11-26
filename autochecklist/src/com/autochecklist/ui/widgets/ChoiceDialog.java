package com.autochecklist.ui.widgets;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;

public class ChoiceDialog extends BaseWidget {
	
	private String mTitle;
	private String mMessage;
	private EventHandler<ActionEvent> mPositiveHandler;
	private EventHandler<ActionEvent> mNegativeHandler;
	
	public ChoiceDialog(String title, String message,
			EventHandler<ActionEvent> positiveHandler,
			EventHandler<ActionEvent> negativeHandler) {
		super();
		mTitle = title;
		mMessage = message;
		mPositiveHandler = positiveHandler;
		mNegativeHandler = negativeHandler;
	}
	
	@Override
	protected void initWidget() {
		mStage.initModality(Modality.APPLICATION_MODAL);
		mStage.setTitle(mTitle);

		Label label = new Label();
		label.setText(mMessage);
		label.setTextAlignment(TextAlignment.CENTER);

		Button yesButton = new Button("Yes");
		yesButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (mPositiveHandler != null) {
				    mPositiveHandler.handle(event);
				}
				mStage.close();
			}
		});

		Button noButton = new Button("No");
		noButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (mNegativeHandler != null) {
				    mNegativeHandler.handle(event);
				}
				mStage.close();
			}
		});

		HBox buttonsLayout = new HBox(10);
		buttonsLayout.getChildren().addAll(yesButton, noButton);
		buttonsLayout.setAlignment(Pos.CENTER);

		VBox layout = new VBox(10);
		layout.getChildren().addAll(label, buttonsLayout);
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 350, 100);
		mStage.setScene(scene);
	}
}
