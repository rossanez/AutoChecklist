package com.autochecklist.ui;

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

	public void print(String message) {
		if (mCurrent != null) {
			mCurrent.print(message);
		}
	}

	public void println(String message) {
        if (mCurrent != null) {
			mCurrent.println(message);
		}
	}

	public void printError(String message) {
        if (mCurrent != null) {
			mCurrent.printError(message);
		}
	}
}
