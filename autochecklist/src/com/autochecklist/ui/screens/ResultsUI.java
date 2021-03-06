package com.autochecklist.ui.screens;

import java.util.ArrayList;
import java.util.List;

import com.autochecklist.modules.OutputGenerator;
import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

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

	private OutputGenerator mOutputGenerator;

	private ReportsUI mResultsViewer;
	private ReviewUI mFindingsReview;

	private List<Pair<String, ReportsUI.IReportGenerator>> mViewerContents;

	private List<CheckBox> mCheckboxes;
	private CheckBox mCheckView;
	private CheckBox mReqView;
	private CheckBox mNumOccView;

	private MenuItem mMenuReview;

	private Button mGenerateButton;

	public ResultsUI(OutputGenerator outputGenerator) {
		super();
		mOutputGenerator = outputGenerator;
	}
	
	@Override
	protected void initUI() {
		if (mOutputGenerator == null) {
			Utils.printError("No output generator found!");
			throw new RuntimeException("Need an OutputGenerator object!");
		}

		mStage.setTitle("Auto Checklist - Results");
		mStage.setMinWidth(300);
		mStage.setMinHeight(220);
		mStage.setResizable(false);

		mMenuRestart = new MenuItem("Restart from scratch");
		mMenuRestart.setOnAction(this);
		mMenuExit = new MenuItem("Exit");
		mMenuExit.setOnAction(this);
		mMenuReview = new MenuItem("Review findings");
		mMenuReview.setOnAction(this);

		MenuBar menuBar = new MenuBar();
		Menu actionsMenu = new Menu("Actions");
		actionsMenu.getItems().add(mMenuRestart);
		actionsMenu.getItems().add(new SeparatorMenuItem());
		actionsMenu.getItems().add(mMenuExit);
		Menu findingsMenu = new Menu("Findings");
		findingsMenu.getItems().add(mMenuReview);
		menuBar.getMenus().addAll(actionsMenu, findingsMenu);
		menuBar.prefWidthProperty().bind(mStage.widthProperty());

		Label subTitle = new Label();
		subTitle.setText("Please select the types of reports to be generated:");

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
		mCheckboxes.add(mNumOccView = new CheckBox("Numbers and units"));
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
		if (mFindingsReview != null) {
			mFindingsReview.close();
		}

		mMenuRestart.setDisable(true);
		mMenuReview.setDisable(true);
		mGenerateButton.setDisable(true);
	}

	@Override
	protected void doWork() {
		mOutputGenerator.runConsistencyCheck();
		mViewerContents = new ArrayList<Pair<String, ReportsUI.IReportGenerator>>();
		if (mCheckView.isSelected()) {
			mViewerContents.add(new Pair<String, ReportsUI.IReportGenerator>("Checklist View",
					            new ReportsUI.IReportGenerator() {
				
				@Override
				public String generateContent() {
					return mOutputGenerator.generateQuestionsViewContent(true);
				}
			}));
		}
		if (mReqView.isSelected()) {
			mViewerContents.add(new Pair<String, ReportsUI.IReportGenerator>("Requirements View",
                                new ReportsUI.IReportGenerator() {

				@Override
				public String generateContent() {
					return mOutputGenerator.generateRequirementsViewContent(true);
				}
			}));
		}
		if (mNumOccView.isSelected()) {
			mViewerContents.add(new Pair<String, ReportsUI.IReportGenerator>("Numeric Occurrences View",
					new ReportsUI.IReportGenerator() {

				@Override
				public String generateContent() {
					return mOutputGenerator.generateNumericOccurrencesContent(true);
				}
			}));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void workSucceeded() {
		mMenuRestart.setDisable(false);
		mMenuReview.setDisable(false);
		mGenerateButton.setDisable(false);

		mResultsViewer = new ReportsUI(mViewerContents.toArray(new Pair[mViewerContents.size()]));
        mResultsViewer.show();
	}

	@Override
	protected void workFailed(boolean cancelled) {
		mMenuRestart.setDisable(false);
		mMenuReview.setDisable(false);
		mGenerateButton.setDisable(false);

		if (cancelled) {
		    new AlertDialog("Stopped!", "The output generation has been cancelled!", mStage).show();
		} else {
			new AlertDialog("Error!", "The output generation has failed!", mStage).show();
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
		} else if (event.getSource() == mMenuRestart) {
			if (mResultsViewer != null) {
				mResultsViewer.close();
			}
			if (mFindingsReview != null) {
				mFindingsReview.close();
			}
			super.handle(event);
		} else if (event.getSource() == mMenuReview) {
			if (mResultsViewer != null) {
				mResultsViewer.close();
			}
			if (mFindingsReview != null) {
				mFindingsReview.close();
			}
			performReview();
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

	private void performReview() {
		mFindingsReview = new ReviewUI(mOutputGenerator);
		mFindingsReview.show();
	}
}
