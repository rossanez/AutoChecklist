package com.autochecklist.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
		try (JarFile jarFile = new JarFile(Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath())) {
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
}
