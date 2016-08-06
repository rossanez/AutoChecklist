package com.autochecklist.ui;

import javafx.application.Platform;

public class Printer {

	private IUIPrintable mCurrent;
	
	private static Printer mInstance;

	private Printer() {	}

	public static Printer getInstance() {
		if (mInstance == null) {
			mInstance = new Printer();
		}

		return mInstance;
	}

	public void register(IUIPrintable current) {
		mCurrent = current;
	}

	public void print(final String message) {
		if (mCurrent != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					if (mCurrent != null) {
					    mCurrent.print(message);
					}
				}
			});
		}
	}

	public void println(final String message) {
        if (mCurrent != null) {
        	if (mCurrent != null) {
    			Platform.runLater(new Runnable() {

    				@Override
    				public void run() {
    					if (mCurrent != null) {
    					    mCurrent.println(message);
    					}
    				}
    			});
    		}
		}
	}

	public void printError(final String message) {
        if (mCurrent != null) {
        	Platform.runLater(new Runnable() {

				@Override
				public void run() {
					if (mCurrent != null) {
						mCurrent.printError(message);
					}
				}
			});
		}
	}
}
