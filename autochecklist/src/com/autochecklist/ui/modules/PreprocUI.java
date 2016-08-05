package com.autochecklist.ui.modules;

import com.autochecklist.ui.BaseUI;
import com.autochecklist.utils.Utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;

public class PreprocUI extends BaseUI implements EventHandler<ActionEvent> {

	private String mSRSFileName;

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

		mMenuExit = new MenuItem("Exit");
		mMenuExit.setOnAction(this);
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuExit);
		menuBar.getMenus().add(menu);
		menuBar.prefWidthProperty().bind(mStage.widthProperty());

		VBox rootGroup = new VBox(10);
		rootGroup.getChildren().addAll(menuBar);

		Scene scene = new Scene(rootGroup, 400, 220);
		mStage.setScene(scene);
	}

	@Override
	public void handle(ActionEvent event) {
		super.handle(event);
	}
}
