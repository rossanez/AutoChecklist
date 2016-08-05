package com.autochecklist.utils;

import java.io.File;

import com.autochecklist.ui.Printer;

public class Utils {

	// Where are messages to be printed out?
	public static final int OUTPUT_DEFAULT = 0;
	public static final int OUTPUT_USER_INTERFACE = 1;
	
	public static int outputType = OUTPUT_DEFAULT;

	public static void print(String message) {
		if (outputType == OUTPUT_USER_INTERFACE) {
			Printer.getInstance().print(message);
		} else {
			System.out.print(message);
		}
	}

	public static void println(String message) {
		if (outputType == OUTPUT_USER_INTERFACE) {
			Printer.getInstance().println(message);
		} else {
			System.out.println(message);
		}
	}

	public static void printError(String message) {
		if (outputType == OUTPUT_USER_INTERFACE) {
			Printer.getInstance().printError(message);
		} else {
			System.err.println(message);
		}
	}

	public static boolean isTextEmpty(String text) {
		return (text == null) || (text.length() <= 0);
	}

	public static String getParentDirectory(String fileName) {
	    int index = fileName.lastIndexOf(File.separatorChar);
	    return fileName.substring(0, index + 1);
	}

	public static File createDirectory(String strDir) {
		File dir = new File(strDir);
		if (dir.exists()) {
		   deleteDir(dir);
		}

		if (dir.mkdir()) {
		    return dir;
		} else {
			return null;
		}
	}

	private static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
}
