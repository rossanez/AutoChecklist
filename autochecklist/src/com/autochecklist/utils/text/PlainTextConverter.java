package com.autochecklist.utils.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.autochecklist.utils.Utils;

/**
 * This class encapsulates the text handling methods.
 * 
 * @author Anderson Rossanez
 */
public class PlainTextConverter {

	public static String convertFile(String fileName) {
		if (Utils.isTextEmpty(fileName)) {
			Utils.printError("Invalid file name!");
			return null;
		}

		if (fileName.toLowerCase().endsWith(".txt")) {
			// Assuming it is already formatted!
			// Simply returning the raw contents.
			return handlePlainTextFile(fileName);
		}

		String xhtml = parseToXHTML(fileName);
		if (Utils.isTextEmpty(xhtml)) {
			Utils.printError("Error when generating XHTML content!");
			return null;
		}

		return getPlainTextFromXHTML(xhtml);
	}

	public static String handlePlainTextFile(String fileName) {
		Utils.println("Handling plain text document...");

		StringBuilder sb = new StringBuilder();
		try (InputStream stream = new FileInputStream(fileName)) {
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(stream));
			String line = null;
			while ((line = bufReader.readLine()) != null) {
				sb.append('\n').append(line);
			}
		} catch (IOException e) {
			Utils.printError("Unable to handle with plain text document!");
		}

		Utils.print("done!");
		return sb.toString();
	}

	public static String parseToXHTML(String fileName) {
		ContentHandler handler = new ToXMLContentHandler();

		AutoDetectParser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();
		try (InputStream stream = new FileInputStream(new File(fileName))) {
			Utils.println("Converting document to XHTML...");
			parser.parse(stream, handler, metadata);
			Utils.print("done!");
			return handler.toString();
		} catch (IOException | SAXException | TikaException e) {
			Utils.printError("Unable to convert to XHTML!");
			throw new RuntimeException("Error when converting document to XHTML! - " + e.getMessage());
		}
	}

	private static String getPlainTextFromXHTML(String xhtml) {
		Utils.println("Converting XHTML to plain-text...");
		final StringBuilder sb = new StringBuilder();

		Document doc = Jsoup.parse(xhtml);
		Element body = doc.body();
		body.traverse(new NodeVisitor() {

			private String mPreviousText = null;

			@Override
			public void head(Node node, int depth) {
				if ("div".equals(node.nodeName())) {
					if ("page".equals(node.attr("class"))) {
						// This is the beginning of a new page.
					}
				} else if (node instanceof TextNode) {
					String itemStr = ((TextNode) node).text();
					if (!Utils.isTextEmpty(itemStr))
						itemStr = itemStr.trim();
					if (Utils.isTextEmpty(itemStr))
						return;

					if (!Utils.isTextEmpty(mPreviousText)) {
						if ((node.parent() != null) && isFontModifierNode(node.parent().nodeName())) {
							sb.append(' ').append(itemStr);
							mPreviousText += ' ' + itemStr;
							return;
						} else if (shouldAddExtraLineBreak(itemStr, mPreviousText)) {
							sb.append('\n');
						}
					}

					sb.append('\n').append(itemStr);
					mPreviousText = itemStr;
				}
			}

			@Override
			public void tail(Node node, int depth) {
				// NOP
			}
		});

		Utils.print("done!");
		return sb.toString();
	}

	private static boolean isFontModifierNode(String nodeName) {
		if ("b".equals(nodeName) || "i".equals(nodeName) || "u".equals(nodeName)) {
			return true;
		}

		return false;
	}

	private static boolean shouldAddExtraLineBreak(String current, String previous) {
		if (Utils.isTextEmpty(current) || Utils.isTextEmpty(previous))
			return false;

		String[] previousArray = previous.split(" ");
		String previousLast = previousArray[previousArray.length - 1];
		char previousLastChar = previousLast.charAt(previousLast.length() - 1);
		String[] currentArray = current.split(" ");
		String currentFirst = currentArray[0];
		char currentFirstChar = currentFirst.charAt(0);

		if ((previousLastChar == ',') || (previousLastChar == ':') || (previousLastChar == ';')
				|| (previousLastChar == '-'))
			return false;

		if (Utils.containsUnbalancedBrackets(current))
			return false;
		if (Character.isUpperCase(currentFirstChar))
			return true;

		return false;
	}
}
