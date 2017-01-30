package com.autochecklist.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
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

	public static String getFileNameWithoutExtension(String fileName) {
		int firstIndex = fileName.lastIndexOf(File.separatorChar);
		int lastIndex = fileName.lastIndexOf('.');

		return fileName.substring((firstIndex < 0) ? 0 : firstIndex + 1,
				                  (lastIndex < 0) ? fileName.length() : lastIndex);
	}

	public static File createTextFile(String fileName, String text) {
		File resultFile = new File(fileName);
        if (resultFile.exists() & !resultFile.isDirectory()) {
        	resultFile.delete();
        }

        try {
		    FileUtils.writeStringToFile(new File(fileName), text);
	    } catch (IOException e) {
		    Utils.printError("Unable to save text file!");
	    }

        return resultFile;
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

	public static URL getResourceAsURL(String resourceFileName) throws MalformedURLException {
		URL res = Thread.currentThread().getContextClassLoader().getResource(resourceFileName);
		if ("jar".equals(res.getProtocol())) {
			try {
				res = new URL("file", null, exportResource(resourceFileName).getPath() + File.separatorChar + resourceFileName);
			} catch (URISyntaxException | IOException e) {
				Utils.printError("Unable to get resource from within a jar file!");
			}
		}

		return res;
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

	private static File exportResource(String resourceFileName) throws URISyntaxException, IOException {
		try (JarFile jarFile = new JarFile(new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("%20", " ")))) {
            String tempDirString = System.getProperty("java.io.tmpdir");
            if(tempDirString==null) {
            	throw new IOException("java.io.tmpdir not set");
            }

            File tempDir = new File(tempDirString);
            if (!tempDir.exists()) {
            	throw new IOException("temporary directory does not exist");
            }
            if (!tempDir.isDirectory()) {
            	throw new IOException("temporary directory is a file, not a directory ");
            }

            File wordNetDir = new File(tempDirString + File.separatorChar + "wordnet20160924152707");
            wordNetDir.mkdir();

            copyResourcesToDirectory(jarFile, resourceFileName, wordNetDir.getAbsolutePath());

            return wordNetDir;
        }
    }

	private static void copyResourcesToDirectory(JarFile fromJar, String jarDir, String destDir) throws IOException {
		for (Enumeration<JarEntry> entries = fromJar.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();
			if (entry.getName().startsWith(jarDir) && !entry.isDirectory()) {
				File dest = new File(destDir + File.separatorChar + entry.getName());
				File parent = dest.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}

				FileOutputStream out = new FileOutputStream(dest);
				InputStream in = fromJar.getInputStream(entry);

				try {
					byte[] buffer = new byte[8 * 1024];

					int s = 0;
					while ((s = in.read(buffer)) > 0) {
						out.write(buffer, 0, s);
					}
				} catch (IOException e) {
					throw new IOException("Could not copy asset from jar file", e);
				} finally {
					try {
						in.close();
					} catch (IOException ignored) {
					}
					try {
						out.close();
					} catch (IOException ignored) {
					}
				}
			}
		}
	}

	public static String[][] getMatrixFromCommaSeparatedValueString(String contents) {
		if (isTextEmpty(contents)) return null;

		BufferedReader bufReader = new BufferedReader(new StringReader(contents));

		List<String[]> matrix = new ArrayList<String[]>();
		int numLines = 0;
		try {
			String line = null;
			int numRows = -1;
			while ((line = bufReader.readLine()) != null) {
				if (isTextEmpty(line)) continue;

				String[] rows = line.split(",");
				if (numRows < 0) {
					numRows = rows.length;
				} else {
					if (numRows != rows.length) return null;
				}
				matrix.add(rows);
				numLines++;
			}
		} catch (IOException e) {
			Utils.printError("Error when trying to check for CSV content!");
			return null;
		}

		if (numLines < 2) return null;
		
		String[][] array = matrix.toArray(new String[matrix.size()][]);
		return array;
	}

	public static String getDateAndTimeApdStr() {
		DateFormat df = new SimpleDateFormat("_yyyyMMdd_hhmmss");
		return df.format(new Date());
	}

	public static boolean containsUnbalancedBrackets(String str) {
		if (isTextEmpty(str)) return false;

		int count = 0;
		for (char c : str.toCharArray()) {
			if (isOpeningBracket(c)) count++;
			if (isClosingBracket(c)) count--;

			if (count < 0) return true;
		}

		return false;
	}

	private static boolean isOpeningBracket(char c) {
		if (c == '{' || c == '[' || c == '(') return true;

		return false;
	}

	private static boolean isClosingBracket(char c) {
		if (c == '}' || c == ']' || c == ')') return true;

		return false;
	}
}
