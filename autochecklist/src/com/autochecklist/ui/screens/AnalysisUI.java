package com.autochecklist.ui.screens;

import com.autochecklist.modules.Orchestrator;
import com.autochecklist.modules.output.OutputFormatter;
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
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AnalysisUI extends BaseUI implements EventHandler<ActionEvent> {

	private String mPreprocFileName;

	private Button mNextButton;

	private MenuItem mMenuRestartAnalysis;

	// This should be the analysis output.
	private OutputFormatter mOutputFormatter;
	
	public AnalysisUI(String preprocFileName) {
		super();
		setDoWorkUponShowing(true);
		mPreprocFileName = preprocFileName;
	}

	@Override
	protected void initUI() {
	    if (Utils.isTextEmpty(mPreprocFileName)) {
	    	Utils.printError("No preprocessed file!");
	    	throw new RuntimeException("No preprocessed file name passed in to the UI!");
	    }

		mStage.setTitle("Auto Checklist - Analysis");
		mStage.setMinWidth(300);
		mStage.setMinHeight(220);

		mMenuRestart = new MenuItem("Restart from scratch");
		mMenuRestart.setOnAction(this);
		mMenuRestart.setDisable(true);
		mMenuRestartAnalysis = new MenuItem("Restart analysis");
		mMenuRestartAnalysis.setOnAction(this);
		mMenuRestartAnalysis.setDisable(true);
		mMenuExit = new MenuItem("Exit");
		mMenuExit.setOnAction(this);
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		menu.getItems().add(mMenuRestart);
		menu.getItems().add(mMenuRestartAnalysis);
		menu.getItems().add(new SeparatorMenuItem());
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
		mNextButton.setDisable(true);
		nextContent.getChildren().add(mNextButton);

		VBox content = new VBox(10);
        content.setPadding(new Insets(0, 10, 10, 10));
        content.getChildren().addAll(subTitleGroup, bufferGroup, nextContent);
		
		VBox rootGroup = new VBox(10);
		rootGroup.getChildren().addAll(menuBar, content);

		Scene scene = new Scene(rootGroup, 600, 500);
		mStage.setScene(scene);
	}

	@Override
	protected void beforeWork() {
		mMenuRestart.setDisable(true);
		mMenuRestartAnalysis.setDisable(true);
	}

	@Override
	protected void doWork() {
		mOutputFormatter = new Orchestrator(mPreprocFileName, true).analyzeOnly();
	}

	@Override
	protected void workSucceeded() {
		mNextButton.setDisable(false);
		mMenuRestart.setDisable(false);
		mMenuRestartAnalysis.setDisable(false);

		new AlertDialog("Success!",
                "The analysis has finished!\nYou may proceed to the results.").show();
	}

	@Override
	protected void workFailed(boolean cancelled) {
		mMenuRestart.setDisable(false);
		mMenuRestartAnalysis.setDisable(false);

		if (cancelled) {
		    new AlertDialog("Stopped!", "The analysis has been cancelled!").show();
		} else {
			new AlertDialog("Error!", "The analysis has failed!").show();
		}
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == mNextButton) {
			new ResultsUI(mOutputFormatter).show();
			mStage.close();
		} else if (event.getSource() == mMenuRestartAnalysis) {
			restart();
		} else {
    		super.handle(event);
		}
	}

	private void restart() {
		new ChoiceDialog("Restarting...",
	    		"Are you sure you want to restart the analysis?",
				new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						doRestart();
					}
				}, null).show();
	}

	private void doRestart() {
		mBuffer.appendText("\nAnalysis restarted...");

		mNextButton.setDisable(true);
		work();
	}
}
