package com.autochecklist.ui.modules;

import com.autochecklist.modules.output.OutputFormatter;
import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ResultsUI extends BaseUI {

	private OutputFormatter mOutputFormatter;

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

		VBox content = new VBox(10);
        content.setPadding(new Insets(0, 10, 10, 10));
        content.getChildren().addAll(subTitleGroup);
		
		VBox rootGroup = new VBox(10);
		rootGroup.getChildren().addAll(menuBar, content);

		Scene scene = new Scene(rootGroup, 600, 500);
		mStage.setScene(scene);
	}

	@Override
	protected void beforeWork() {
		mMenuRestart.setDisable(true);
	}

	@Override
	protected void doWork() {
		mOutputFormatter.start();
	}

	@Override
	protected void workSucceeded() {
		mMenuRestart.setDisable(false);

		new AlertDialog("Success!",
                "The output has been generated!").show();
	}

	@Override
	protected void workFailed(boolean cancelled) {
		mMenuRestart.setDisable(false);

		if (cancelled) {
		    new AlertDialog("Stopped!", "The output generation has been cancelled!").show();
		} else {
			new AlertDialog("Error!", "The output generation has failed!").show();
		}
	}
}
