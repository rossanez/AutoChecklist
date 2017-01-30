package com.autochecklist.ui.screens;

import java.io.File;

import com.autochecklist.modules.Orchestrator;
import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;
import com.autochecklist.ui.widgets.ChoiceDialog;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PreprocUI extends BaseUI implements EventHandler<ActionEvent> {

	private String mSRSFileName;

	// This should hold the generated file.
	private File mPreprocessedFile;

	private MenuItem mMenuOpenPreprocDir;
	private MenuItem mMenuRestartPreproc;

	private Button mNextButton;

	private ProgressBar mProgressBar;

	public PreprocUI(String srsFileName) {
		super();
		setDoWorkUponShowing(true);
		mSRSFileName = srsFileName;
	}

	@Override
	protected void initUI() {
		if (Utils.isTextEmpty(mSRSFileName)) {
			Utils.printError("No SRS file found!");
			throw new RuntimeException("No SRS file name passed in to the UI!");
		}

		mStage.setTitle("Auto Checklist - Preprocessing");
		mStage.setMinWidth(300);
		mStage.setMinHeight(220);

		mMenuOpenPreprocDir = new MenuItem("Open preprocessed file's folder");
		mMenuOpenPreprocDir.setOnAction(this);
		mMenuOpenPreprocDir.setDisable(true);
		mMenuRestartPreproc = new MenuItem("Restart preprocessing");
		mMenuRestartPreproc.setOnAction(this);
		mMenuRestartPreproc.setDisable(true);
		mMenuRestart = new MenuItem("Restart from scratch");
		mMenuRestart.setOnAction(this);
		mMenuRestart.setDisable(true);
		mMenuExit = new MenuItem("Exit");
		mMenuExit.setOnAction(this);
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		menu.getItems().add(mMenuOpenPreprocDir);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuRestart);
		menu.getItems().add(mMenuRestartPreproc);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuExit);
		menuBar.getMenus().add(menu);
		menuBar.prefWidthProperty().bind(mStage.widthProperty());

		HBox subTitleGroup = new HBox(10);
		subTitleGroup.setAlignment(Pos.TOP_CENTER);
		Label subTitle = new Label();
		subTitle.setText("SRS document file: " + mSRSFileName);
		subTitleGroup.getChildren().add(subTitle);

		HBox bufferGroup = new HBox(10);
        bufferGroup.setAlignment(Pos.CENTER);
		mBuffer = new TextArea("Preprocessing started...");
        mBuffer.setEditable(false);
        mBuffer.setWrapText(true);
        mBuffer.prefWidthProperty().bind(mStage.widthProperty());
        mBuffer.prefHeightProperty().bind(mStage.heightProperty());
        bufferGroup.getChildren().add(mBuffer);

        HBox nextContent = new HBox(10);
		nextContent.setAlignment(Pos.BOTTOM_RIGHT);
        mNextButton = new Button("Next >>");
        mNextButton.setDisable(true);
		mNextButton.setOnAction(this);
		mNextButton.setMinWidth(100);
		nextContent.getChildren().add(mNextButton);

		HBox progressContent = new HBox(10);
		progressContent.setAlignment(Pos.BOTTOM_LEFT);
		mProgressBar = new ProgressBar();
		mProgressBar.setProgress(0);
		progressContent.prefWidthProperty().bind(mStage.widthProperty());
		progressContent.getChildren().add(mProgressBar);

		HBox bottomContent = new HBox(10);
		bottomContent.setPadding(new Insets(5, 0, 0, 0));
        bottomContent.getChildren().addAll(progressContent, nextContent);
        
		VBox content = new VBox(10);
        content.setPadding(new Insets(0, 10, 10, 10));
        content.getChildren().addAll(subTitleGroup, bufferGroup, bottomContent);
		
		VBox rootGroup = new VBox(10);
		rootGroup.getChildren().addAll(menuBar, content);

		Scene scene = new Scene(rootGroup, 600, 500);
		mStage.setScene(scene);
	}

	@Override
	protected void beforeWork() {
		mMenuRestart.setDisable(true);
		mMenuRestartPreproc.setDisable(true);
		mMenuOpenPreprocDir.setDisable(true);
		mProgressBar.setProgress(-1); // Indeterminate.
	}

	@Override
	protected void doWork() {
		mPreprocessedFile = new Orchestrator(mSRSFileName).preProcessOnly();
	}

	@Override
	protected void workSucceeded() {
		mMenuRestart.setDisable(false);
		mMenuRestartPreproc.setDisable(false);
		mMenuOpenPreprocDir.setDisable(false);
		mNextButton.setDisable(false);
		mProgressBar.setProgress(1);

		new AlertDialog("Success: The preprocessing has finished!",
		        "Please review the generated preprocessed file."
				+ "\n(Actions -> Open preprocessed file's folder)", mStage).show();
	}

	@Override
	protected void workFailed(boolean cancelled) {
		mMenuRestart.setDisable(false);
		mMenuRestartPreproc.setDisable(false);
		mProgressBar.setProgress(0);

		if (cancelled) {
		    new AlertDialog("Stopped!", "The preprocessing has been cancelled!", mStage).show();
		} else {
			new AlertDialog("Error!", "The preprocessing has failed!", mStage).show();
		}
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == mNextButton) {
			new AnalysisUI(mPreprocessedFile.getPath()).show();
			mStage.close();
		} else if (event.getSource() == mMenuOpenPreprocDir) {
			Utils.openDirectoryWithPlaformExplorerFromUI(mPreprocessedFile.getPath());
		} else if (event.getSource() == mMenuRestartPreproc) {
			restart();
		} else {
		    super.handle(event);
		}
	}

	private void restart() {
		new ChoiceDialog("Restarting...",
	    		"Are you sure you want to restart the preprocessing?",
				new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						doRestart();
					}
				}, null, mStage).show();
	}

	private void doRestart() {
		mBuffer.appendText("\nPreprocessing restarted...");
		mNextButton.setDisable(true);

		work();
	}
}
