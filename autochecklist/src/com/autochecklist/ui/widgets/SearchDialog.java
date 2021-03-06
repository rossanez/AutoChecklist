package com.autochecklist.ui.widgets;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SearchDialog extends BaseWidget {

	private ISearchCallable mEventHandler;

	private CheckBox mWrapAround;

	public SearchDialog(ISearchCallable eventHandler, Stage ownerStage) {
		super(ownerStage);
		mEventHandler = eventHandler;
	}

	@Override
	protected void initWidget() {
		mStage.setTitle("Search");

		Label label = new Label();
		label.setText("Find:");
		label.setTextAlignment(TextAlignment.CENTER);
		label.setMinWidth(40);

		final TextField searchField = new TextField("");
		searchField.prefWidthProperty().bind(mStage.widthProperty());
		
		HBox findContainer = new HBox(10);
		findContainer.prefWidthProperty().bind(mStage.widthProperty());
		findContainer.setAlignment(Pos.CENTER_LEFT);
		findContainer.getChildren().addAll(label, searchField);

		Button searchButton = new Button("Search");
		searchButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				event.consume();
				mEventHandler.onSearch(searchField.getText().replace("'","\\'"), mWrapAround.isSelected());
			}
		});
		searchButton.setMinWidth(70);

		HBox buttonContainer = new HBox(10);
		buttonContainer.getChildren().add(searchButton);
		buttonContainer.setAlignment(Pos.BOTTOM_RIGHT);

		mWrapAround = new CheckBox("Wrap around");
		mWrapAround.setSelected(true);
		mWrapAround.prefWidthProperty().bind(mStage.widthProperty());

		HBox wrapSearchContainer = new HBox(10);
		wrapSearchContainer.getChildren().add(mWrapAround);
		wrapSearchContainer.setAlignment(Pos.BOTTOM_LEFT);

		HBox bottomContainer = new HBox(10);
		bottomContainer.setAlignment(Pos.BOTTOM_CENTER);
		bottomContainer.prefWidthProperty().bind(mStage.widthProperty());
        bottomContainer.getChildren().addAll(wrapSearchContainer, buttonContainer);

		VBox layout = new VBox(10);
		layout.getChildren().addAll(findContainer, bottomContainer);
		layout.setPadding(new Insets(10, 10, 10, 10));
		layout.setAlignment(Pos.CENTER);
		layout.prefHeightProperty().bind(mStage.heightProperty());
		layout.prefWidthProperty().bind(mStage.widthProperty());

		Scene scene = new Scene(layout, 240, 100);
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					if (mEventHandler != null) {
						mEventHandler.onSearch(searchField.getText().replace("'","\\'"), mWrapAround.isSelected());
					}
				} else if (event.getCode().equals(KeyCode.ESCAPE)) {
					if (mEventHandler != null) {
						mEventHandler.onClose();
					}
					close();
				}
			}
		});

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

	public interface ISearchCallable {

		void onSearch(String text, boolean wrapAround);
		
		void onClose();
	}
}
