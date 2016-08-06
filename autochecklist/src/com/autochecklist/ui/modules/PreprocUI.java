package com.autochecklist.ui.modules;

import java.io.File;
import java.io.IOException;

import com.autochecklist.modules.Orchestrator;
import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;
import com.autochecklist.ui.widgets.ChoiceDialog;
import com.autochecklist.utils.Utils;
import com.google.common.io.Files;

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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class PreprocUI extends BaseUI implements EventHandler<ActionEvent> {

	private String mSRSFileName;

	// This should hold the generated file.
	private File mPreprocessedFile;

	private MenuItem mMenuSavePreproc;

	private Button mNextButton;
	private Button mCancelRestartButton;

	public PreprocUI(String srsFileName) {
		super();
		mSRSFileName = srsFileName;
	}

	@Override
	protected void initUI() {
		if (Utils.isTextEmpty(mSRSFileName)) {
			throw new RuntimeException("No SRS file name passed in to the UI!");
		}

		mStage.setTitle("Auto Checklist - Preprocessing");
		mStage.setMinWidth(300);
		mStage.setMinHeight(220);

		mMenuSavePreproc = new MenuItem("Save preprocessed file as...");
		mMenuSavePreproc.setOnAction(this);
		mMenuSavePreproc.setDisable(true);
		mMenuRestart = new MenuItem("Restart");
		mMenuRestart.setOnAction(this);
		mMenuExit = new MenuItem("Exit");
		mMenuExit.setOnAction(this);
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		menu.getItems().add(mMenuSavePreproc);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuRestart);
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
		nextContent.setPadding(new Insets(5, 0, 0, 0));
		nextContent.setAlignment(Pos.CENTER);
        mNextButton = new Button("Next >>");
        mNextButton.setDisable(true);
		mNextButton.setOnAction(this);
		mNextButton.setAlignment(Pos.BOTTOM_RIGHT);
		mCancelRestartButton = new Button("Cancel");
		mCancelRestartButton.setOnAction(this);
		mCancelRestartButton.setAlignment(Pos.BOTTOM_LEFT);
		nextContent.getChildren().addAll(mCancelRestartButton, mNextButton);
        
		VBox content = new VBox(10);
        content.setPadding(new Insets(0, 10, 10, 10));
        content.getChildren().addAll(subTitleGroup, bufferGroup, nextContent);
		
		VBox rootGroup = new VBox(10);
		rootGroup.getChildren().addAll(menuBar, content);

		Scene scene = new Scene(rootGroup, 600, 500);
		mStage.setScene(scene);
	}

	@Override
	protected void doWork() {
		mPreprocessedFile = new Orchestrator(mSRSFileName).preProcessOnly();
	}

	@Override
	protected void workSucceeded() {
		mMenuSavePreproc.setDisable(false);
		mNextButton.setDisable(false);
		mCancelRestartButton.setText("Restart");
		new AlertDialog("Success!",
                "The preprocessing has finished!\nYou may proceed to the analysis.").show();
	}

	@Override
	protected void workFailed(boolean cancelled) {
		mCancelRestartButton.setText("Restart");
		if (cancelled) {
		    new AlertDialog("Stopped!", "The preprocessing has been cancelled!").show();
		} else {
			new AlertDialog("Error!", "The preprocessing has failed!").show();
		}
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == mNextButton) {
			new AnalysisUI(mPreprocessedFile.getPath()).show();
			mStage.close();
		} else if (event.getSource() == mMenuSavePreproc) {
			savePreprocFileAs();
		} else if (event.getSource() == mCancelRestartButton) {
			handleCancelRestart();
		} else {
		    super.handle(event);
		}
	}

	private void savePreprocFileAs() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save a pre-processed file...");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XML Files", "*.xml"));
		File file = fileChooser.showSaveDialog(mStage);
		if(!file.getPath().endsWith(".xml")) {
			  file = new File(file.getPath() + ".xml");
		}
		try {
			Files.move(mPreprocessedFile, file);
			mPreprocessedFile = file;
			Utils.println("Saved the preprocessed file as: " + mPreprocessedFile.getPath());
		} catch (IOException e) {
			Utils.printError("FAILURE: File has not been saved!");
		}
	}

	private void handleCancelRestart() {
		if ("Cancel".equals(mCancelRestartButton.getText())) {
			new ChoiceDialog("Cancelling...",
		    		"Are you sure you want to cancel the preprocessing?",
					new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							mBuffer.appendText("\nPreprocessing canceled!");
							stopWork();
						}
					}, null).show();
		} else {
			// Restart
			if (!mNextButton.isDisabled()) {
				new ChoiceDialog("Restarting...",
			    		"Are you sure you want to restart the preprocessing?",
						new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								restartWork();
							}
						}, null).show();
			} else {
				restartWork();
			}
			
		}
	}

	private void restartWork() {
		mBuffer.appendText("\nPreprocessing restarted...");
		mCancelRestartButton.setText("Cancel");
		mNextButton.setDisable(true);
		work();
	}
}
