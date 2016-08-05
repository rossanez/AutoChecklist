package com.autochecklist.ui;

import java.io.File;

import com.autochecklist.ui.modules.AnalysisUI;
import com.autochecklist.ui.modules.PreprocUI;
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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class InitialUI extends BaseUI {

	private final static int STATE_SRS = 0;
	private final static int STATE_PREPROC = 1;
	
	private int mState = STATE_SRS;

	private MenuItem mMenuSwitchPreproc;
	private MenuItem mMenuSwitchSRS;

	private Button mNextButton;

	private String mFileName;

	private ChoiceDialog mEmptyFileDialog;

	public InitialUI(Stage primaryStage) {
		super(primaryStage);
	}
	
	@Override
	protected void initUI() {
		mStage.setTitle("Auto Checklist");
		mStage.setMinWidth(300);
		mStage.setMinHeight(220);

		mMenuExit = new MenuItem("Exit");
		mMenuExit.setOnAction(this);
		mMenuSwitchPreproc = new MenuItem("Switch to a pre-processed file");
		mMenuSwitchPreproc.setOnAction(this);
		mMenuSwitchSRS = new MenuItem("Switch to a SRS document file");
		mMenuSwitchSRS.setOnAction(this);
		
		mStage.setScene(createScene(STATE_SRS));
	}

	private Scene createScene(int state) {
		mFileName = null;
		mState = state;
		
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		if (state == STATE_SRS) {
		   menu.getItems().add(mMenuSwitchPreproc);
		} else {
			menu.getItems().add(mMenuSwitchSRS);
		}
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuExit);
		menuBar.getMenus().add(menu);
		menuBar.prefWidthProperty().bind(mStage.widthProperty());

		final TextArea chosenFile = new TextArea("(no file selected)");
        chosenFile.setEditable(false);
        chosenFile.setWrapText(true);
		
		final FileChooser fileChooser = new FileChooser();
		Label label = new Label();
		if (state == STATE_PREPROC) {
			fileChooser.setTitle("Open a pre-processed file");
		    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XML Files", "*.xml"));
		    label.setText("Please choose a pre-processed file:");
		    mEmptyFileDialog = new ChoiceDialog("No pre-processed file chosen!",
		    		"Would you like to switch to a SRS file?",
					new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							mStage.setScene(createScene(STATE_SRS));
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
							mStage.setScene(createScene(STATE_PREPROC));
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
					chosenFile.setText(mFileName);
				}
			}
		});
		chosenFile.prefHeightProperty().bind(openButton.heightProperty());

		mNextButton = new Button("Next >>");
		mNextButton.setOnAction(this);

		HBox captionContent = new HBox(10);
		captionContent.setAlignment(Pos.TOP_CENTER);
		captionContent.getChildren().add(label);

		HBox openFileContent = new HBox(10);
		openFileContent.setPadding(new Insets(20, 0, 0, 0));
		openFileContent.setAlignment(Pos.CENTER);
		openFileContent.getChildren().addAll(chosenFile, openButton);

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
		if (event.getSource() == mMenuSwitchPreproc) {
			mStage.setScene(createScene(STATE_PREPROC));
			
			return;
		} else if (event.getSource() == mMenuSwitchSRS) {
			mStage.setScene(createScene(STATE_SRS));
			
			return;
		} else if (event.getSource() == mNextButton) {
			if (Utils.isTextEmpty(mFileName)) {
				mEmptyFileDialog.show();
			} else {
				if (mState == STATE_SRS) {
					new PreprocUI(mFileName).show();
				} else {
					new AnalysisUI(mFileName).show();
				}
				mStage.close();
			}
			
			return;
		}
	
		super.handle(event);
	}
}
