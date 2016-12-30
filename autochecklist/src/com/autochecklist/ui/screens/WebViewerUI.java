package com.autochecklist.ui.screens;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;
import com.autochecklist.ui.widgets.SearchDialog;
import com.autochecklist.utils.HtmlBuilder;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
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
import javafx.stage.WindowEvent;

public class WebViewerUI extends BaseUI {

	private Pair<String, String>[] mContents;
	
	private MenuBar mMenuBar;
	private MenuItem mMenuSave;
	private MenuItem mMenuClose;
	private MenuItem mMenuFind;

	private TabPane tabPane;

	private WebView[] mWebViews;

	private SearchDialog mSearchDialog;

	private String mPreviousSearchTerm = null;
	private final String mDateTimeCatString;

	public WebViewerUI(Pair<String, String>[] contents) {
		super();
		mContents = contents;

		DateFormat df = new SimpleDateFormat("_yyyyMMdd_hhmmss");
		mDateTimeCatString = df.format(new Date());
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
		content.setPadding(new Insets(0, 1, 1, 1));
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
			mSearchDialog = new SearchDialog(new SearchDialog.ISearchCallable() {

				@Override
				public void onSearch(String text, boolean wrapAround) {
					int index = tabPane.getSelectionModel().getSelectedIndex();
					findAndHighlight(mWebViews[index].getEngine(), text, wrapAround);
				}
				
				@Override
				public void onClose() {
					clearAllHighlights();
				}
			}, mStage);
			mSearchDialog.show();
		} else {
			super.handle(event);
		}
	}

	@Override
	protected void onExternalCloseRequest(WindowEvent windowEvent) {
		// We don't want a confirmation dialog.
		// Calling close() for cleaning up unlikely running background works!
		close();
	}

	private void save() {
		FileChooser fileChooser = new FileChooser();
		int index = tabPane.getSelectionModel().getSelectedIndex();
		fileChooser.setTitle("Save \"" + mContents[index].first + "\" output...");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("HTML Files", "*.html"));
		fileChooser.setInitialFileName(mContents[index].first.trim().replace(" ", "_") + mDateTimeCatString);
		File file = fileChooser.showSaveDialog(mStage);
		if (file == null) return; // User may have cancelled the dialog.

		if(!file.getPath().endsWith(".html")) {
			  file = new File(file.getPath() + ".html");
		}

		try {
			FileUtils.writeStringToFile(file, HtmlBuilder.removeScriptsfromContent(mContents[index].second));
			new AlertDialog("Success!", "File has been saved!\n" + file.getPath(), mStage).show();
		} catch (IOException e) {
			new AlertDialog("Error!", "Unable to save file!", mStage).show();
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

	private void findAndHighlight(WebEngine engine, String text, boolean wrapAround) {
		if ((engine != null) && (engine.getDocument() != null) && !Utils.isTextEmpty(text)) {
			if (!text.equals(mPreviousSearchTerm)) {
                engine.executeScript("$('body').removeHighlight().highlight('" + text + "')");
                mPreviousSearchTerm = text;
			}
            engine.executeScript("window.find('" + text + "', false, false, " + (wrapAround ? "true" : "false") + ")");
		}
    }

    private void clearHighlight(WebEngine engine) {
    	if ((engine != null) && (engine.getDocument() != null)) {
            engine.executeScript("$('body').removeHighlight()");
            mPreviousSearchTerm = null;
    	}
    }

    private void clearAllHighlights() {
    	for (WebView webview : mWebViews) {
    		clearHighlight(webview.getEngine());
    	}
    }
}
