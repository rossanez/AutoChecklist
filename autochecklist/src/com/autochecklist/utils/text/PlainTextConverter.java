package com.autochecklist.utils.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.xml.sax.Attributes;
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

		if (fileName.endsWith(".txt") || fileName.endsWith(".TXT")) {
			return convertFileToPlainText(fileName);
		}

		String xhtml = parseToXHTML(fileName);
		if (Utils.isTextEmpty(xhtml)) {
			Utils.printError("Error when generating XHTML content!");
			return null;
		}

		return getPlainTextFromXHTML(xhtml);
	}

	public static String convertFileToPlainText(String fileName) {
		ContentHandler handler = new CustomBodyContentHandler();

		AutoDetectParser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();
	    try (InputStream stream = new FileInputStream(new File(fileName))) {
	    	Utils.println("Handling plain text document...");
			parser.parse(stream, handler, metadata);
			Utils.print("done!");
			return handler.toString();
		} catch (IOException | SAXException | TikaException e) {
			Utils.printError("Unable to convert to plain text!");
			throw new RuntimeException("Error when converting document to plain text! - " + e.getMessage());
		}
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
					if (!Utils.isTextEmpty(itemStr)) itemStr = itemStr.trim();
					if (Utils.isTextEmpty(itemStr)) return;

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
		if (Utils.isTextEmpty(current) || Utils.isTextEmpty(previous)) return false;

		String[] previousArray = previous.split(" ");
		String previousLast = previousArray[previousArray.length - 1];
		char previousLastChar = previousLast.charAt(previousLast.length() - 1);
		String[] currentArray = current.split(" ");
		String currentFirst = currentArray[0];
		char currentFirstChar = currentFirst.charAt(0);

		if ((previousLastChar == ',')
			|| (previousLastChar == ':')
			|| (previousLastChar == ';')) return false;

		if (Utils.containsUnbalancedBrackets(current)) return false;
		if (Character.isUpperCase(currentFirstChar)) return true;

		return false;
	}
}

/*package*/ class CustomBodyContentHandler extends BodyContentHandler {

	private boolean ignoreLineBreak = false;
	private boolean brokeLine = false;

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		if (ignoreLineBreak && isLineBreak(ch, start, length)) {
			ignoreLineBreak = false;
		} else {
			if (isLineBreak(ch, start, length)) {
				brokeLine = true;
			}
			super.ignorableWhitespace(ch, start, length);
		}
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		if ("p".equals(localName) || "div".equals(localName) || "meta".equals(localName)) {
			ignoreLineBreak = true;
		}
		super.startElement(uri, localName, name, atts);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (brokeLine) {
			brokeLine = false;

			// TODO Weak logic. It should be improved.
			if ((length > 1) && Character.isUpperCase(ch[0]) && !Character.isUpperCase(ch[1])) {
				char[] newChar = new char[ch.length + 1];
				newChar[0] = '\n';
				System.arraycopy(ch, 0, newChar, 1, ch.length);
				super.characters(newChar, start, length + 1);
				return;
			}
		}
		super.characters(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		super.endElement(uri, localName, name);
	}

	private boolean isLineBreak(char[] ch, int start, int length) {
		String str = String.valueOf(ch, start, length);
		if (!Utils.isTextEmpty(str)) {
			if ("\n".equals(str) || "\r\n".equals(str) || "\n\r".equals(str)) {
				return true;
			}
		}

		return false;
	}
}