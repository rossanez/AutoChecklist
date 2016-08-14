package com.autochecklist.ui.screens;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;
import com.autochecklist.ui.widgets.ISearchCallable;
import com.autochecklist.ui.widgets.SearchDialog;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class WebViewerUI extends BaseUI {

	private Pair<String, String>[] mContents;
	
	private MenuBar mMenuBar;
	private MenuItem mMenuSave;
	private MenuItem mMenuClose;
	private MenuItem mMenuFind;

	private TabPane tabPane;

	private WebView[] mWebViews;

	private SearchDialog mSearchDialog;

	public WebViewerUI(Pair<String, String>[] contents) {
		super();
		mContents = contents;
	}

	@Override
	protected void initUI() {
		mStage.setTitle("Auto Checklist - Results Viewer");
		mStage.setMinWidth(400);
		mStage.setMinHeight(500);

		mMenuSave = new MenuItem("Save as...");
		mMenuSave.setOnAction(this);
		mMenuClose = new MenuItem("Close");
		mMenuClose.setOnAction(this);
		mMenuFind = new MenuItem("Find...");
		mMenuFind.setOnAction(this);
		mMenuFind.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));

		mMenuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		menu.getItems().add(mMenuSave);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuClose);
		mMenuBar.getMenus().add(menu);
		Menu searchMenu = new Menu("Search");
		searchMenu.getItems().add(mMenuFind);
		mMenuBar.getMenus().add(searchMenu);
		mMenuBar.prefWidthProperty().bind(mStage.widthProperty());

		tabPane = new TabPane();

        BorderPane borderPane = new BorderPane();
        mWebViews = new WebView[mContents.length];
        int webViewsIndex = 0;
        for (Pair<String, String> content : mContents) {
            Tab tab = new Tab();
            tab.setText(content.first);
            mWebViews[webViewsIndex] = getWebViewContent(content.second);
            tab.setContent(mWebViews[webViewsIndex]);
            tab.setClosable(false);
            tabPane.getTabs().add(tab);
            webViewsIndex++;
        }
        borderPane.prefHeightProperty().bind(mStage.heightProperty());
        borderPane.prefWidthProperty().bind(mStage.widthProperty());
        borderPane.setCenter(tabPane);

		VBox content = new VBox();
        content.getChildren().addAll(borderPane);
		
		VBox rootGroup = new VBox();
		rootGroup.getChildren().addAll(mMenuBar, content);

		Scene scene = new Scene(rootGroup, 800, 600);
		mStage.setScene(scene);
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == mMenuClose) {
			close();
		} else if (event.getSource() == mMenuSave) {
			save();
		} else if (event.getSource() == mMenuFind) {
			if (mSearchDialog != null) mSearchDialog.close();
			mSearchDialog = new SearchDialog(new ISearchCallable() {

				@Override
				public void onSearch(String text) {
					int index = tabPane.getSelectionModel().getSelectedIndex();
					highlight(mWebViews[index].getEngine(), text);
				}
				
				@Override
				public void onClose() {
					int index = tabPane.getSelectionModel().getSelectedIndex();
					clearHighlight(mWebViews[index].getEngine());
				}
			});
			mSearchDialog.show();
		} else {
			super.handle(event);
		}
	}

	public void close() {
		if (mStage != null) {
			mStage.close();
		}
	}

	private void save() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save output file...");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("HTML Files", "*.html"));
		File file = fileChooser.showSaveDialog(mStage);
		if (file == null) return;

		if(!file.getPath().endsWith(".html")) {
			  file = new File(file.getPath() + ".html");
		}

		try {
			int index = tabPane.getSelectionModel().getSelectedIndex();
			FileUtils.writeStringToFile(file, mContents[index].second);
			new AlertDialog("Success!", "File " + file.getPath() + " saved!").show();
		} catch (IOException e) {
			new AlertDialog("Error!", "Unable to save file!").show();
		}
	}

	public WebView getWebViewContent(String content) {
		final WebView browser = new WebView();
        browser.prefHeightProperty().bind(mStage.heightProperty());
        browser.prefWidthProperty().bind(mStage.widthProperty());
        WebEngine webEngine = browser.getEngine();
        webEngine.loadContent(content);

        return browser;
	}

	private void highlight(WebEngine engine, String text) {
		if ((engine != null) && (engine.getDocument() != null) && !Utils.isTextEmpty(text)) {
            engine.executeScript("$('body').removeHighlight().highlight('" + text + "')");
            engine.executeScript("window.find('" + text + "')");
		}
    }

    private void clearHighlight(WebEngine engine) {
    	if ((engine != null) && (engine.getDocument() != null)) {
            engine.executeScript("$('body').removeHighlight()");
    	}
    }
}
