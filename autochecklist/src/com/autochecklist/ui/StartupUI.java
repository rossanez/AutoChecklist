package com.autochecklist.ui;

import com.autochecklist.ui.screens.InitialUI;
import com.autochecklist.utils.Utils;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This class represents the initial GUI screen.
 * 
 * @author Anderson Rossanez
 */
public class StartupUI extends Application {

	/**
	 * This method will start up the GUI.
	 * 
	 * @param args Unused arguments.
	 */
	public static void initUI(String[] args) {
		Utils.outputType = Utils.OUTPUT_USER_INTERFACE;
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		new InitialUI(primaryStage).show();		
	}
}
