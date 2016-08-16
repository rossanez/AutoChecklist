package com.autochecklist.ui;

import javafx.application.Platform;

public class PrintingService {

	private IUIPrintable mCurrent;
	
	private static PrintingService mInstance;

	private PrintingService() {	}

	public static PrintingService getInstance() {
		if (mInstance == null) {
			mInstance = new PrintingService();
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

	public interface IUIPrintable {

		void print(String message);

		void println(String message);

		void printError(String message);
	}
}
