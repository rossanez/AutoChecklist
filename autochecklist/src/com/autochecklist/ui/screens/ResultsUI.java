package com.autochecklist.ui.screens;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.modules.output.OutputFormatter;
import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;
import com.autochecklist.utils.Pair;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ResultsUI extends BaseUI {

	private OutputFormatter mOutputFormatter;

	private WebViewerUI mResultsViewer;

	private List<Pair<String, String>> mViewerContents;

	private List<CheckBox> mCheckboxes;
	private CheckBox mCheckView;
	private CheckBox mReqView;

	private Button mGenerateButton;

	public ResultsUI(OutputFormatter formatter) {
		super();
		mOutputFormatter = formatter;
	}
	
	@Override
	protected void initUI() {
		if (mOutputFormatter == null) {
			throw new RuntimeException("Need an OutputFormatter object!");
		}

		mStage.setTitle("Auto Checklist - Results");
		mStage.setMinWidth(300);
		mStage.setMinHeight(220);

		mMenuRestart = new MenuItem("Restart from scratch");
		mMenuRestart.setOnAction(this);
		mMenuExit = new MenuItem("Exit");
		mMenuExit.setOnAction(this);

		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		menu.getItems().add(mMenuRestart);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuExit);
		menuBar.getMenus().add(menu);
		menuBar.prefWidthProperty().bind(mStage.widthProperty());

		Label subTitle = new Label();
		subTitle.setText("Please select the types of outputs to be generated:");

		HBox subTitleGroup = new HBox(10);
		subTitleGroup.setAlignment(Pos.TOP_CENTER);
		subTitleGroup.getChildren().add(subTitle);

		HBox checkContent = new HBox(10);
		checkContent.setAlignment(Pos.CENTER);
		checkContent.prefWidthProperty().bind(mStage.widthProperty());
		checkContent.prefHeightProperty().bind(mStage.heightProperty());
		VBox checkBoxes = new VBox(20);
		checkBoxes.setAlignment(Pos.CENTER_LEFT);
		mCheckboxes = new ArrayList<CheckBox>();
		mCheckboxes.add(mCheckView = new CheckBox("Checklist view"));
		mCheckboxes.add(mReqView = new CheckBox("Requirements view"));
		setOnActionForCheckboxes();
		checkBoxes.getChildren().addAll(mCheckboxes);
		checkContent.getChildren().add(checkBoxes);

		HBox nextContent = new HBox(10);
		nextContent.setPadding(new Insets(5, 0, 0, 0));
		nextContent.setAlignment(Pos.BOTTOM_CENTER);
        mGenerateButton = new Button("Generate");
		mGenerateButton.setOnAction(this);
		mGenerateButton.setDisable(true);
		nextContent.getChildren().add(mGenerateButton);
		
		VBox content = new VBox(10);
        content.setPadding(new Insets(0, 10, 10, 10));
        content.getChildren().addAll(subTitleGroup, checkContent, nextContent);
		
		VBox rootGroup = new VBox(10);
		rootGroup.getChildren().addAll(menuBar, content);

		Scene scene = new Scene(rootGroup, 400, 300);
		mStage.setScene(scene);
	}

	@Override
	protected void beforeWork() {
		if (mResultsViewer != null) {
			mResultsViewer.close();
		}

		mMenuRestart.setDisable(true);
		mGenerateButton.setDisable(true);
	}

	@Override
	protected void doWork() {
		mViewerContents = new ArrayList<Pair<String, String>>();
		if (mCheckView.isSelected()) {
			mViewerContents.add(new Pair<String, String>("Checklist View",mOutputFormatter.generateQuestionsViewContent()));
		}
		if (mReqView.isSelected()) {
			mViewerContents.add(new Pair<String, String>("Requirements View", mOutputFormatter.generateRequirementsViewContent()));
		}
	}

	@Override
	protected void workSucceeded() {
		mMenuRestart.setDisable(false);
		mGenerateButton.setDisable(false);

		mResultsViewer = new WebViewerUI(mViewerContents.toArray(new Pair[mViewerContents.size()]));
        mResultsViewer.show();
	}

	@Override
	protected void workFailed(boolean cancelled) {
		mMenuRestart.setDisable(false);
		mGenerateButton.setDisable(false);

		if (cancelled) {
		    new AlertDialog("Stopped!", "The output generation has been cancelled!").show();
		} else {
			new AlertDialog("Error!", "The output generation has failed!").show();
		}
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() instanceof CheckBox) {
			if (isAtLeastOneCheckSelected()) {
				mGenerateButton.setDisable(false);
			} else {
				mGenerateButton.setDisable(true);
			}
		} else if (event.getSource() == mGenerateButton) {
			work();
		} else {
			super.handle(event);
		}
	}

	private void setOnActionForCheckboxes() {
		for (CheckBox c : mCheckboxes) {
			c.setOnAction(this);
			c.setSelected(false);
		}
	}

	private boolean isAtLeastOneCheckSelected() {
		for (CheckBox c : mCheckboxes) {
			if (c.isSelected()) return true;
		}

		return false;
	}
}
