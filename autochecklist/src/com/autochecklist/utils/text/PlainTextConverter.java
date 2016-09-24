package com.autochecklist.utils.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
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
		ContentHandler handler = new CustomBodyContentHandler();

		AutoDetectParser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();
	    try (InputStream stream = new FileInputStream(new File(fileName))) {
	    	Utils.println("Converting document to plain text...");
			parser.parse(stream, handler, metadata);
			Utils.print("done!");
			return handler.toString();
		} catch (IOException | SAXException | TikaException e) {
			Utils.printError("Unable to convert to plain text!");
			throw new RuntimeException("Error when converting document to plain text! - " + e.getMessage());
		}
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