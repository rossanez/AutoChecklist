package com.autochecklist.ui;

import java.io.File;

import com.autochecklist.ui.widgets.ChoiceDialog;
import com.autochecklist.utils.Utils;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.Stage;

/**
 * This class represents the initial GUI screen.
 * 
 * @author Anderson Rossanez
 */
public class StartupUI extends Application implements EventHandler<ActionEvent> {

	private MenuItem mMenuExit;
	private MenuItem mMenuSwitchPreproc;
	private MenuItem mMenuSwitchSRS;

	private Stage mStage;

	private Button mNextButton;

	private String mFileName;

	private ChoiceDialog mEmptyFileDialog; 

	/**
	 * This method will start up the GUI.
	 * 
	 * @param args Unused arguments.
	 */
	public static void initUI(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		mStage = primaryStage;
		mStage.setTitle("Auto Checklist");
		mStage.setMinWidth(300);
		mStage.setMinHeight(220);

		mMenuExit = new MenuItem("Exit");
		mMenuExit.setOnAction(this);
		mMenuSwitchPreproc = new MenuItem("Switch to a pre-processed file");
		mMenuSwitchPreproc.setOnAction(this);
		mMenuSwitchSRS = new MenuItem("Switch to a SRS document file");
		mMenuSwitchSRS.setOnAction(this);
		
		mStage.setScene(createScene(mMenuSwitchPreproc));
		mStage.show();
	}

	private Scene createScene(MenuItem fileTypeItem) {
		mFileName = null;
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		menu.getItems().add(fileTypeItem);
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuExit);
		menuBar.getMenus().add(menu);
		menuBar.prefWidthProperty().bind(mStage.widthProperty());

		final TextArea textArea = new TextArea("(no file selected)");
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(290);
        textArea.setMaxHeight(10);
		
		final FileChooser fileChooser = new FileChooser();
		Label label = new Label();
		if (fileTypeItem == mMenuSwitchSRS) {
			fileChooser.setTitle("Open a pre-processed file");
		    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XML Files", "*.xml"));
		    label.setText("Please choose a pre-processed file:");
		    mEmptyFileDialog = new ChoiceDialog("No pre-processed file chosen!",
		    		"Would you like to switch to a SRS file?",
					new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							mStage.setScene(createScene(mMenuSwitchPreproc));
						}
					}, null);
		} else {
		    fileChooser.setTitle("Open a SRS document file");
		    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PDF Files", "*.pdf"));
		    label.setText("Please choose a SRS document file:");
		    mEmptyFileDialog = new ChoiceDialog("No SRS file chosen!",
		    		"Would you like to switch to a pre-processed file?",
					new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							mStage.setScene(createScene(mMenuSwitchSRS));
						}
					}, null);
		}

		Button openButton = new Button("...");
		openButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				File file = fileChooser.showOpenDialog(mStage);
				if (file != null) {
					mFileName = file.getPath();
					textArea.setText(mFileName);
				}
			}
		});

		mNextButton = new Button("Next >>");
		mNextButton.setOnAction(this);

		HBox captionContent = new HBox(10);
		captionContent.setAlignment(Pos.TOP_CENTER);
		captionContent.getChildren().add(label);

		HBox openFileContent = new HBox(10);
		openFileContent.setPadding(new Insets(20, 0, 0, 0));
		openFileContent.setAlignment(Pos.CENTER);
		openFileContent.getChildren().addAll(textArea, openButton);

		HBox nextContent = new HBox(10);
		nextContent.setPadding(new Insets(20, 0, 0, 0));
		nextContent.setAlignment(Pos.BOTTOM_RIGHT);
		nextContent.getChildren().add(mNextButton);

        VBox content = new VBox(10);
        content.setPadding(new Insets(0, 20, 20, 20));
        content.getChildren().addAll(captionContent, openFileContent, nextContent);

		VBox rootGroup = new VBox(10);
		rootGroup.getChildren().addAll(menuBar, content);

		Scene scene = new Scene(rootGroup, 400, 220);
		return scene;
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == mMenuExit) {
		    Platform.exit();
		} else if (event.getSource() == mMenuSwitchPreproc) {
			mStage.setScene(createScene(mMenuSwitchSRS));
		} else if (event.getSource() == mMenuSwitchSRS) {
			mStage.setScene(createScene(mMenuSwitchPreproc));
		} else if (event.getSource() == mNextButton) {
			if (Utils.isTextEmpty(mFileName)) {
				mEmptyFileDialog.show();
			}
		}
	}
}
