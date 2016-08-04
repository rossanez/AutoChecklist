package com.autochecklist.ui.widgets;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class AlertDialog extends BaseWidget {

	private String mTitle;
	private String mMessage;
	
	public AlertDialog(String title, String message) {
		super();
        mTitle = title;
        mMessage = message;
	}

	@Override
	protected void initWidget() {
		mStage.initModality(Modality.APPLICATION_MODAL);
		mStage.setTitle(mTitle);
		mStage.setMinWidth(350);
		mStage.setMinHeight(100);

		Label label = new Label();
		label.setText(mMessage);

		Button okButton = new Button("Ok");
		okButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				mStage.close();
			}
		});

		VBox layout = new VBox(10);
		layout.getChildren().addAll(label, okButton);
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout);
		mStage.setScene(scene);
	}
}
