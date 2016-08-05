package com.autochecklist.ui.modules;

import com.autochecklist.ui.BaseUI;
import com.autochecklist.utils.Utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AnalysisUI extends BaseUI implements EventHandler<ActionEvent> {

	private String mPreprocFileName;

	private Button mNextButton;
	
	public AnalysisUI(String preprocFileName) {
		super();
		mPreprocFileName = preprocFileName;
	}

	@Override
	protected void initUI() {
	    if (Utils.isTextEmpty(mPreprocFileName)) {
	    	throw new RuntimeException("No preprocessed file name passed in to the UI!");
	    }

		mStage.setTitle("Auto Checklist - Analysis");
		mStage.setMinWidth(300);
		mStage.setMinHeight(220);

		mMenuRestart = new MenuItem("Restart");
		mMenuRestart.setOnAction(this);
		mMenuExit = new MenuItem("Exit");
		mMenuExit.setOnAction(this);
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuRestart);
		menu.getItems().add(mMenuExit);
		menuBar.getMenus().add(menu);
		menuBar.prefWidthProperty().bind(mStage.widthProperty());

		Label subTitle = new Label();
		subTitle.setText("Preprocessed file: " + mPreprocFileName);

		HBox subTitleGroup = new HBox(10);
		subTitleGroup.setAlignment(Pos.TOP_CENTER);
		subTitleGroup.getChildren().add(subTitle);

		HBox bufferGroup = new HBox(10);
        bufferGroup.setAlignment(Pos.CENTER);
		mBuffer = new TextArea("Started the analysis...");
        mBuffer.setEditable(false);
        mBuffer.setWrapText(true);
        mBuffer.prefWidthProperty().bind(mStage.widthProperty());
        mBuffer.prefHeightProperty().bind(mStage.heightProperty());
        bufferGroup.getChildren().add(mBuffer);

        HBox nextContent = new HBox(10);
		nextContent.setPadding(new Insets(5, 0, 0, 0));
		nextContent.setAlignment(Pos.BOTTOM_RIGHT);
        mNextButton = new Button("Results >>");
		mNextButton.setOnAction(this);
		nextContent.getChildren().add(mNextButton);

		VBox content = new VBox(10);
        content.setPadding(new Insets(0, 10, 10, 10));
        content.getChildren().addAll(subTitleGroup, bufferGroup, nextContent);
		
		VBox rootGroup = new VBox(10);
		rootGroup.getChildren().addAll(menuBar, content);

		Scene scene = new Scene(rootGroup, 300, 220);
		mStage.setScene(scene);
	}

	@Override
	public void handle(ActionEvent event) {
		super.handle(event);
	}
}
