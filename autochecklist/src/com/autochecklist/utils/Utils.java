package com.autochecklist.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.autochecklist.ui.PrintingService;

public class Utils {

	// Where are messages to be printed out?
	public static final int OUTPUT_DEFAULT = 0;
	public static final int OUTPUT_USER_INTERFACE = 1;
	
	public static int outputType = OUTPUT_DEFAULT;

	public static void print(String message) {
		if (outputType == OUTPUT_USER_INTERFACE) {
			PrintingService.getInstance().print(message);
		} else {
			System.out.print(message);
		}
	}

	public static void println(String message) {
		if (outputType == OUTPUT_USER_INTERFACE) {
			PrintingService.getInstance().println(message);
		} else {
			System.out.println(message);
		}
	}

	public static void printError(String message) {
		if (outputType == OUTPUT_USER_INTERFACE) {
			PrintingService.getInstance().printError(message);
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

	public static InputStream getResourceAsInputStream(String resourceFileName) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFileName);
	}

	public static String getResourceAsString(String resourceFileName) {
		InputStream inputStream = getResourceAsInputStream(resourceFileName);
		try {
			return IOUtils.toString(inputStream, "UTF-8");
		} catch (IOException e) {
			Utils.printError("Error getting resource file contents!");
			return null;
		}
	}

	public static String getCompositeResourceAsString(String resourceFileName, String... composingResourcesFileNames) {
		String mainResource = getResourceAsString(resourceFileName);
		for (String compositeResourceFileName : composingResourcesFileNames) {
			String replacingPattern = "$INCLUDE_"
					+ compositeResourceFileName.substring(compositeResourceFileName.indexOf("/") + 1,
							compositeResourceFileName.indexOf(".compose")).toUpperCase();
			mainResource = mainResource.replace(replacingPattern, getResourceAsString(compositeResourceFileName));
		}
		return mainResource;
	}
}
