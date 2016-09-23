package com.autochecklist.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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

	/**
	 * Opens the current platform's file explorer to open the directory where
	 * the passed in file is located. Please call this method from the UI.
	 * @param fileNameWithFullPath The passed in file name with full path.
	 */
	public static void openDirectoryWithPlaformExplorerFromUI(final String fileNameWithFullPath) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				openDirectoryWithPlatformExplorer(fileNameWithFullPath);
			}
		}).start();
	}

	/**
	 * Opens the current platform's file explorer to open the directory where
	 * the passed in file is located. ATTENTION: Do not call this method from
	 * the UI. Call {@code openDirectoryWithPlaformExplorerFromUI} method instead.
	 * 
	 * @param fileNameWithFullPath The passed in file name with full path.
	 */
	public static void openDirectoryWithPlatformExplorer(String fileNameWithFullPath) {
		if (isTextEmpty(fileNameWithFullPath)) {
			Utils.printError("No file name passed in!");
			return;
		}

		File file = new File(fileNameWithFullPath);
		if (!file.exists()) {
			Utils.printError("File does not exists!");
			return;
		}

		if (!file.isDirectory()) {
			String parentPath = file.getParent();
			if (Utils.isTextEmpty(parentPath)) {
				Utils.printError("Unable to get the directory for this file!");
				return;
			}

			file = new File(parentPath);
		}

		if (!Desktop.isDesktopSupported()) {
			Utils.printError("Function not supported in this platform!");
			return;
		}
		
		try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			Utils.printError("Unable to open directory! - " + e.getMessage());
		}
	}

	public static InputStream getResourceAsInputStream(String resourceFileName) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFileName);
	}

	public static URL getResourceAsURL(String resourceFileName) {
		return Thread.currentThread().getContextClassLoader().getResource(resourceFileName);
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
