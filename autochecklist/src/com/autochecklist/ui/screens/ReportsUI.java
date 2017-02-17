package com.autochecklist.ui.screens;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;
import com.autochecklist.ui.widgets.SearchDialog;
import com.autochecklist.utils.Pair;
import com.autochecklist.utils.Utils;
import com.autochecklist.utils.filebuilder.HtmlBuilder;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
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

public class ReportsUI extends BaseUI {

	private Pair<String, IReportGenerator>[] mContents;
	private String[] mHtmlContents;
	
	private MenuBar mMenuBar;
	private MenuItem mMenuSave;
	private MenuItem mMenuClose;
	private MenuItem mMenuFind;

	private ProgressIndicator mProgressIndicator;

	BorderPane mBorderPane;

	private TabPane mTabPane;

	private WebView[] mWebViews;

	private SearchDialog mSearchDialog;

	private String mPreviousSearchTerm = null;
	private final String mDateTimeCatString;

	public ReportsUI(Pair<String, IReportGenerator>[] contents) {
		super();
		mContents = contents;
		mHtmlContents = new String[contents.length];

		mDateTimeCatString = Utils.getDateAndTimeApdStr();

		setDoWorkUponShowing(true);
	}

	@Override
	protected void initUI() {
		mStage.setTitle("Auto Checklist - Reports Viewer");
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

		mTabPane = new TabPane();

		VBox progressContent = new VBox(10);
		progressContent.setAlignment(Pos.CENTER);
		mProgressIndicator = new ProgressIndicator();
		mProgressIndicator.setProgress(0);
		progressContent.prefWidthProperty().bind(mStage.widthProperty());
		progressContent.prefHeightProperty().bind(mStage.heightProperty());
		Label loadingLabel = new Label("Loading...");
		progressContent.getChildren().addAll(mProgressIndicator, loadingLabel);

        mBorderPane = new BorderPane();
        mBorderPane.setCenter(progressContent);

		VBox content = new VBox();
		content.setPadding(new Insets(0, 1, 1, 1));
        content.getChildren().addAll(mBorderPane);
		
		VBox rootGroup = new VBox();
		rootGroup.getChildren().addAll(mMenuBar, content);

		Scene scene = new Scene(rootGroup, 800, 600);
		mStage.setScene(scene);
	}

	@Override
	protected void beforeWork() {
		mMenuSave.setDisable(true);
		mMenuFind.setDisable(true);

		mProgressIndicator.setProgress(-1); // Indeterminate.

		// This code must run on UI thread.
		mWebViews = new WebView[mContents.length];
		for (int i = 0; i < mWebViews.length; i++){
			mWebViews[i] = new WebView();
			mWebViews[i].prefHeightProperty().bind(mStage.heightProperty());
            mWebViews[i].prefWidthProperty().bind(mStage.widthProperty());
		}
	}

	@Override
	protected void doWork() {
        int webViewsIndex = 0;
        for (Pair<String, IReportGenerator> content : mContents) {
            Tab tab = new Tab();
            tab.setText(content.first);
            mHtmlContents[webViewsIndex] = content.second.generateContent();
            tab.setContent(mWebViews[webViewsIndex]);
            tab.setClosable(false);
            mTabPane.getTabs().add(tab);
            webViewsIndex++;
        }
        mBorderPane.prefHeightProperty().bind(mStage.heightProperty());
        mBorderPane.prefWidthProperty().bind(mStage.widthProperty());
	}

	@Override
	protected void workSucceeded() {
		mProgressIndicator.setProgress(1);
		mMenuFind.setDisable(false);
		mMenuSave.setDisable(false);

		// This loop must run on UI thread.
		for (int i = 0; i < mWebViews.length; i++) {
		    loadWebViewContent(mHtmlContents[i], mWebViews[i]);
		}

		mBorderPane.setCenter(mTabPane);
	}

	@Override
	protected void workFailed(boolean cancelled) {
		mProgressIndicator.setProgress(0);

		new AlertDialog("Error!", "The output generation has failed!", mStage).show();
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
					int index = mTabPane.getSelectionModel().getSelectedIndex();
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
		int index = mTabPane.getSelectionModel().getSelectedIndex();
		fileChooser.setTitle("Save \"" + mContents[index].first + "\" output...");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("HTML Files", "*.html"));
		fileChooser.setInitialFileName(mContents[index].first.trim().replace(" ", "_") + mDateTimeCatString);
		File file = fileChooser.showSaveDialog(mStage);
		if (file == null) return; // User may have cancelled the dialog.

		if(!file.getPath().endsWith(".html")) {
			  file = new File(file.getPath() + ".html");
		}

		try {
			FileUtils.writeStringToFile(file, HtmlBuilder.removeScriptsfromContent(mHtmlContents[index]));
			new AlertDialog("Success!", "File has been saved!\n" + file.getPath(), mStage).show();
		} catch (IOException e) {
			new AlertDialog("Error!", "Unable to save file!", mStage).show();
		}
	}

	private void loadWebViewContent(String content, WebView browser) {
        WebEngine webEngine = browser.getEngine();
        webEngine.loadContent(content);
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

    interface IReportGenerator {
    	String generateContent();
    }
}
