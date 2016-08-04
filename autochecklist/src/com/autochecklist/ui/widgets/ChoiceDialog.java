package com.autochecklist.ui.widgets;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChoiceDialog {

	private Stage mWindow;
	
	public ChoiceDialog(String title, String message,
			final EventHandler<ActionEvent> positiveHandler,
			final EventHandler<ActionEvent> negativeHandler) {
		mWindow = new Stage();

		mWindow.initModality(Modality.APPLICATION_MODAL);
		mWindow.setTitle(title);
		mWindow.setMinWidth(350);
		mWindow.setMinHeight(100);

		Label label = new Label();
		label.setText(message);

		Button yesButton = new Button("Yes");
		yesButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (positiveHandler != null) {
				    positiveHandler.handle(event);
				}
				mWindow.close();
			}
		});

		Button noButton = new Button("No");
		noButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (negativeHandler != null) {
				    negativeHandler.handle(event);
				}
				mWindow.close();
			}
		});

		HBox buttonsLayout = new HBox(10);
		buttonsLayout.getChildren().addAll(yesButton, noButton);
		buttonsLayout.setAlignment(Pos.CENTER);

		VBox layout = new VBox(10);
		layout.getChildren().addAll(label, buttonsLayout);
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout);
		mWindow.setScene(scene);
	}

	public void show() {
		mWindow.show();
	}
}
