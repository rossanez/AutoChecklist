package com.autochecklist.ui.widgets;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

public class SearchDialog extends BaseWidget {

	private ISearchCallable mEventHandler;

	public SearchDialog(ISearchCallable eventHandler) {
		super();
		mEventHandler = eventHandler;
	}

	@Override
	protected void initWidget() {
		mStage.initModality(Modality.APPLICATION_MODAL);
		mStage.setTitle("Search");
		mStage.setResizable(false);

		Label label = new Label();
		label.setText("Find:");
		label.setTextAlignment(TextAlignment.CENTER);
		
		final TextArea searchField = new TextArea("");
		searchField.setMaxHeight(20);
		searchField.setMaxWidth(200);
		searchField.setPrefHeight(20);
		
		HBox findContainer = new HBox(10);
		findContainer.getChildren().addAll(label, searchField);
		findContainer.setAlignment(Pos.CENTER);

		Button searchButton = new Button("Search");
		searchButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				event.consume();
				mEventHandler.onSearch(searchField.getText());
			}
		});
		
		HBox buttonContainer = new HBox(10);
		buttonContainer.getChildren().add(searchButton);
		buttonContainer.setAlignment(Pos.CENTER_RIGHT);

		VBox layout = new VBox(10);
		layout.getChildren().addAll(findContainer, buttonContainer);
		layout.setPadding(new Insets(10, 10, 10, 10));
		layout.setAlignment(Pos.CENTER);

		Scene scene = new Scene(layout, 280, 100);

		mStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(final WindowEvent event) {
				if (mEventHandler != null) {
					mEventHandler.onClose();
				}
			}
		});
		
		mStage.setScene(scene);
	}
}
