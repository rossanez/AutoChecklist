package com.autochecklist.ui;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
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
		} else {
		    fileChooser.setTitle("Open a SRS document file");
		    fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PDF Files", "*.pdf"));
		    label.setText("Please choose a SRS document file:");
		}

		Button openButton = new Button("...");
		openButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				File file = fileChooser.showOpenDialog(mStage);
				if (file != null) {
					textArea.setText(file.getPath());
				}
			}
		});

		final GridPane rootGridPane = new GridPane();
		GridPane.setConstraints(menuBar, 0, 0);
		GridPane.setConstraints(textArea, 1, 1);
		GridPane.setConstraints(openButton, 2, 1);
		rootGridPane.setHgap(6);
		rootGridPane.setVgap(6);
		rootGridPane.getChildren().addAll(textArea, openButton);

		final Pane rootGroup = new VBox(12);
		rootGroup.getChildren().addAll(menuBar, label, rootGridPane);

		Scene scene = new Scene(rootGroup, 300, 250);
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
		}
	}
}
