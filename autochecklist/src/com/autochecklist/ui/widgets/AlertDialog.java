package com.autochecklist.ui.widgets;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class AlertDialog extends BaseWidget {

	private String mTitle;
	private String mMessage;
	
	public AlertDialog(String title, String message, Stage ownerStage) {
		super(ownerStage);
        mTitle = title;
        mMessage = message;
	}

	@Override
	protected void initWidget() {
		mStage.setTitle(mTitle);

		Label label = new Label();
		label.setText(mMessage);
		label.setTextAlignment(TextAlignment.CENTER);

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

		Scene scene = new Scene(layout, 400, 125);
		mStage.setScene(scene);
	}
}
