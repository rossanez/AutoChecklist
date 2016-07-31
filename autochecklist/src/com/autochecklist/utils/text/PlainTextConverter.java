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
			parser.parse(stream, handler, metadata);
			return handler.toString();
		} catch (IOException | SAXException | TikaException e) {
			throw new RuntimeException("Error when converting document to plain text! - " + e.getMessage());
		}
	}
}

/*package*/ class CustomBodyContentHandler extends BodyContentHandler {

	private boolean ignoreWhitespace = false;
	private boolean brokeLine = false;

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		if (ignoreWhitespace) {
			ignoreWhitespace = false;
		} else {
			super.ignorableWhitespace(ch, start, length);
			brokeLine = true;
		}
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
		if ("p".equals(localName) || "div".equals(localName) || "meta".equals(localName)) {
			ignoreWhitespace = true;
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
}