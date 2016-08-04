package com.autochecklist.ui;

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
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		new InitialUI(primaryStage).show();		
	}
}
